package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioFromExternal;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.OrderBehaviourType;
import ru.gosuslugi.pgu.dto.order.OrderListInfoDto;
import ru.gosuslugi.pgu.dto.order.ShortOrderData;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.service.AbstractScreenService;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.exception.OrderNotFoundException;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.DeliriumService;
import ru.gosuslugi.pgu.fs.service.MainScreenService;
import ru.gosuslugi.pgu.fs.service.OrderInfoService;
import ru.gosuslugi.pgu.fs.service.ScenarioInitializerService;
import ru.gosuslugi.pgu.fs.service.TransformService;
import ru.gosuslugi.pgu.fs.service.process.ExternalScreenProcess;
import ru.gosuslugi.pgu.fs.service.process.NextScreenProcess;
import ru.gosuslugi.pgu.fs.service.process.PrevScreenProcess;
import ru.gosuslugi.pgu.fs.service.process.impl.screen.ExternalScreenProcessImpl;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainScreenServiceImpl extends AbstractScreenService implements MainScreenService {

    private final MainDescriptorService mainDescriptorService;
    private final DeliriumService deliriumService;
    private final UserPersonalData userPersonalData;
    private final PguOrderService pguOrderService;
    private final DraftClient draftClient;
    private final OrderInfoService orderInfoService;
    private final TransformService transformService;
    private final PrevScreenProcess prevScreenProcess;
    private final NextScreenProcess nextScreenProcess;
    private final ExternalScreenProcess externalScreenProcess;
    private final ScenarioInitializerService scenarioInitializerService;

    @Override
    protected DescriptorService getDescriptorService() {
        return mainDescriptorService;
    }

    @Override
    public ScenarioResponse getInitScreen(String serviceId, InitServiceDto initServiceDto) {
        return scenarioInitializerService.getInitScreen(initServiceDto, serviceId);
    }

    @Override
    public ScenarioResponse getExistingScenario(InitServiceDto initServiceDto, String serviceId) {
        /* В случае если у ордера нет serviceID (Это возможно в кейсе умного поиска) */
        Order order = pguOrderService.findOrderByIdCached(Long.valueOf(initServiceDto.getOrderId()));
        if (Strings.isEmpty(order.getEserviceId())) {
            order.setEserviceId(serviceId);
        }
        return scenarioInitializerService.getInitScreenWithExistedOrderId(serviceId, order, initServiceDto);
    }

    @Override
    public ScenarioResponse getNextScreen(ScenarioRequest request, String serviceId) {
        try {
            return getNextStep(jsonProcessingService.clone(request), serviceId, true);
        } catch (ExternalServiceException e) {
            if (Objects.nonNull(request.getScenarioDto().getOrderId())) {
                ServiceDescriptor serviceDescriptor = getDescriptorService().getServiceDescriptor(serviceId);
                draftClient.saveDraft(request.getScenarioDto(), serviceId, userPersonalData.getUserId(), userPersonalData.getOrgId(), serviceDescriptor.getDraftTtl(), serviceDescriptor.getOrderTtl());
            }
            throw e;
        }
    }

    @Override
    public ScenarioResponse skipStep(ScenarioRequest request, String serviceId) {
        return getNextStep(request, serviceId, false);
    }

    @Override
    public ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId) {
        return prevScreenProcess.of(serviceId, request)
                .completeIf(PrevScreenProcess::onlyInitScreenWasShow, PrevScreenProcess::getInitScreen)
                .executeIf(PrevScreenProcess::isCycledScreen, PrevScreenProcess::setResponseIfPrevScreenCycled)
                // если экран не цикличный ответ еще не установлен
                .executeIf(PrevScreenProcess::checkResponseIsNull,
                        List.of(
                                PrevScreenProcess::putAnswersToCache,
                                PrevScreenProcess::removeScreenFromFinished,
                                PrevScreenProcess::removeAnswersFromDto,
                                PrevScreenProcess::removeCycledAnswers,
                                PrevScreenProcess::setResponseIfScreenHasCycledComponent))
                // если экран не содержит цикличных компонентов ответ еще не установлен
                .executeIf(PrevScreenProcess::checkResponseIsNull, PrevScreenProcess::calculateNextScreen)
                .completeIf(PrevScreenProcess::needReInitScreen, PrevScreenProcess::reInitScenario)
                .execute(PrevScreenProcess::checkEditable)
                .execute(PrevScreenProcess::saveDraft)
                .start();
    }

    @Override
    public OrderListInfoDto getOrderInfo(InitServiceDto initServiceDto, String serviceId) {

        List<Order> availableOrders = new ArrayList<>();
        var serviceDescriptor = this.getDescriptorService().getServiceDescriptor(serviceId);
        var ordersLimit = serviceDescriptor.getOrdersLimit();
        if(ordersLimit == 1){
            var lastOrder = pguOrderService.findLastOrder(serviceId, initServiceDto.getTargetId());
            if(Objects.isNull(lastOrder)){
                OrderListInfoDto result = new OrderListInfoDto();
                result.setLimitOrders(ordersLimit);
                result.setServiceName(serviceDescriptor.getServiceName());
                result.setCompareRegions(serviceDescriptor.getCompareRegions());
                return result;
            }
            availableOrders.add(lastOrder);
        }
        if(ordersLimit > 1){
            availableOrders.addAll(pguOrderService.findOrders(serviceId, initServiceDto.getTargetId()));
        }
        if (availableOrders.isEmpty()) {
            OrderListInfoDto result = new OrderListInfoDto();
            result.setLimitOrders(ordersLimit);
            result.setServiceName(serviceDescriptor.getServiceName());
            result.setCompareRegions(serviceDescriptor.getCompareRegions());
            return result;
        }
        var result = fillOrderListStatus(availableOrders, serviceId);
        result.setServiceName(serviceDescriptor.getServiceName());
        result.setCompareRegions(serviceDescriptor.getCompareRegions());
        return result;
    }

    /**
     * Статусы для ордеров услуг
     *
     * @see ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses
     */
    @Override
    public OrderListInfoDto getOrderInfoById(InitServiceDto initServiceDto, String serviceId) {
        List<Long> availableStatusesList = ServiceDescriptor.DEFAULT_AVAILABLE_ORDER_STATUSES;
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(serviceId);
        if (nonNull(serviceDescriptor)) {
            availableStatusesList = serviceDescriptor.getAvailableOrderStatuses();
        }

        Order order = pguOrderService.findOrderByIdCached(Long.valueOf(initServiceDto.getOrderId()));
        // Необходимо проверить что это все еще черновик, не заявление
        if (isNull(order)) {
            throw new OrderNotFoundException(String.format("Заявление %s не найдено", initServiceDto.getOrderId()));
        }
        if (!availableStatusesList.contains(order.getOrderStatusId()) && !transformService.isAcceptedCode(order.getOrderStatusId(), serviceId)) {
            throw new OrderNotFoundException(String.format("Заявление %s не является черновиком", initServiceDto.getOrderId()));
        }
        OrderListInfoDto orderInfo = fillOrderListStatus(Arrays.asList(order), serviceId);
        orderInfo.setStartNewBlockedByOrderId(order.getId());
        return orderInfo;
    }

    @Override
    public void setStatusId(ScenarioDto scenarioDto) {
        if (nonNull(scenarioDto.getOrderId())) {
            Order order = pguOrderService.findOrderByIdCached(scenarioDto.getOrderId());
            if (isNull(order)) {
                throw new OrderNotFoundException(String.format("Заявление %s не найдено", scenarioDto.getOrderId()));
            }
            setStatusId(scenarioDto, order.getOrderStatusId());
        }
    }

    private ScenarioResponse getNextStep(ScenarioRequest request, String serviceId, Boolean validate) {
        return nextScreenProcess.of(serviceId, request)
                .execute(NextScreenProcess::buildResponse)
                .executeIf(NextScreenProcess::hasCheckForDuplicate, NextScreenProcess::checkForDuplicate)
                .execute(NextScreenProcess::checkHighLoadOrderExists)
                .executeIf(NextScreenProcess::orderShouldExists, NextScreenProcess::tryToCreateOrderId)
                .execute(NextScreenProcess::clearCacheForComponents)
                .execute(NextScreenProcess::removeOldAnswers)
                .execute(NextScreenProcess::removeDisclaimers)
                .execute(NextScreenProcess::clearValidateErrors)
                .executeIf(validate, NextScreenProcess::validate)
                .executeIf(NextScreenProcess::hasValidateErrors, NextScreenProcess::prepareDisplayAfterValidationErrors)
                .completeIf(NextScreenProcess::hasValidateErrors)
                .executeIf(validate, NextScreenProcess::preloadComponents)
                .executeIf(NextScreenProcess::replaceResponseForCycledScreen, null, NextScreenProcess::calculateNextScreen)
                .execute(NextScreenProcess::fillUserData)
                .completeIf(NextScreenProcess::needReInitScreen, NextScreenProcess::reInitScenario)
                .executeIf(NextScreenProcess::isTerminalAndForceSendToSuggest, NextScreenProcess::forceSendToSuggestion)
                .completeIf(NextScreenProcess::isTerminalAndImpasse, NextScreenProcess::deleteOrder)
                .executeIf(NextScreenProcess::hasOrderCreateCustomParameter, NextScreenProcess::saveChosenValuesForOrder)
                .executeIf(NextScreenProcess::isNeedToUpdateAdditionalParameters, NextScreenProcess::updateAdditionalAttributes)
                .execute(NextScreenProcess::checkPermissions)
                .execute(NextScreenProcess::checkEditable)
                .executeIf(NextScreenProcess::draftShouldExist,NextScreenProcess::saveDraft)
                .executeIf(NextScreenProcess::isTerminalAndNotImpasse, NextScreenProcess::mergePdfDocuments)
                .execute(NextScreenProcess::performIntegrationSteps)
                .start();
    }

    private OrderListInfoDto fillOrderListStatus(List<Order> orders, String serviceId) {
        var orderListInfoDto = new OrderListInfoDto();
        var sd = this.getDescriptorService().getServiceDescriptor(serviceId);
        orderListInfoDto.setCompareRegions(sd.getCompareRegions());
        orderListInfoDto.setLimitOrders(sd.getOrdersLimit());
        var shortOrdersData = orders.stream().map(order -> {
            var shortOrderData = new ShortOrderData();
            shortOrderData.setCreatedAt(order.getOrderDate());
            shortOrderData.setRegion(order.getUserSelectedRegion());
            shortOrderData.setOrderId(order.getId());
            return shortOrderData;
        }).collect(Collectors.toList());

        orderListInfoDto.setOrders(shortOrdersData);

        orders.forEach(order -> {
            DraftHolderDto draftHolderDto = draftClient.getDraftById(order.getId(), userPersonalData.getUserId(), userPersonalData.getOrgId());
            if(OrderBehaviourType.canDraftExists(sd.getOrderBehaviourType())){
                if (isNull(draftHolderDto) || isNull(draftHolderDto.getBody()) || order.getOrderStatusId() == 14L) {
                    return;
                }

                TransformationResult transformationResult = transformService.checkAndTransform(order, serviceId, draftHolderDto);
                if (transformationResult.isTransformed()) {
                    if (log.isInfoEnabled()) {
                        log.info("Произведена трансформация черновика id = {} согласно ЛК код статуса {} для услуги = {}", order.getId(), order.getOrderStatusId(), serviceId);
                    }
                    draftHolderDto = transformationResult.getDraftHolderDto();
                    order = transformationResult.getOrder();
                }
                // Set/update statusId to DTO
                setStatusId(draftHolderDto.getBody(), order.getOrderStatusId());
            }

            ApplicantRole role = deliriumService.getUserRole(draftHolderDto);
            DeliriumStageDto deliriumStageDto = getDeliriumStageDto(serviceId, order, role);
            var orderInfo = orderInfoService.getOrderInfo(order, role, deliriumStageDto, draftHolderDto, sd.getAlwaysContinueScenario());
            if(!orderInfo.getCanStartNew()){
                orderListInfoDto.setStartNewBlockedByOrderId(order.getId());
            }
            if(orderInfo.getIsInviteScenario()){
                orderListInfoDto.setInviteByOrderId(order.getId());
            }
        });

        return orderListInfoDto;
    }

    private DeliriumStageDto getDeliriumStageDto(String serviceId, Order order, ApplicantRole role) {
        boolean useDefaultStage = getDescriptorService().getServiceDescriptor(serviceId).checkOnlyOneApplicantAndStage();
        return deliriumService.getStage(role, order.getId(), useDefaultStage);
    }

    private void setStatusId(ScenarioDto scenarioDto, Long statusId) {
        scenarioDto.getServiceInfo().setStatusId(statusId);
    }

    @Override
    public ScenarioResponse prepareScenarioFromExternal(ScenarioFromExternal scenarioFromExternal) {
        var serviceDescriptor = this.mainDescriptorService.getServiceDescriptor(scenarioFromExternal.getServiceId());
        if(!serviceDescriptor.getExternalScreenIds().contains(scenarioFromExternal.getScreenId())
                || serviceDescriptor.getScreens().stream().noneMatch(scr -> scr.getId().equals(scenarioFromExternal.getScreenId()))){
            throw new FormBaseException("Экран не доступен");
        }
        return externalScreenProcess
                .of(scenarioFromExternal, true)
                .execute(ExternalScreenProcessImpl::createOrder)
                .execute(ExternalScreenProcessImpl::prepareApplicantAnswer)
                .execute(ExternalScreenProcessImpl::prepareDisplay)
                .execute(ExternalScreenProcessImpl::saveDraft)
                .start();
    }

    @Override
    public void saveCacheToDraft(String serviceId, ScenarioRequest request) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        scenarioDto.getCachedAnswers().putAll(scenarioDto.getCurrentValue());
        ServiceDescriptor serviceDescriptor = getDescriptorService().getServiceDescriptor(serviceId);
        draftClient.saveDraft(scenarioDto, serviceId, userPersonalData.getUserId(), userPersonalData.getOrgId(), serviceDescriptor.getDraftTtl(), serviceDescriptor.getOrderTtl());
    }
}
