package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.DisclaimerDto;
import ru.gosuslugi.pgu.dto.DisclaimerLevel;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.order.OrderInfoDto;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.exception.CreateOrderException;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;
import ru.gosuslugi.pgu.fs.exception.DuplicateOrderException;
import ru.gosuslugi.pgu.fs.exception.OrderIncompatibleServiceException;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.DeliriumService;
import ru.gosuslugi.pgu.fs.service.DisclaimersService;
import ru.gosuslugi.pgu.fs.service.OrderInfoService;
import ru.gosuslugi.pgu.fs.service.ScenarioInitializerService;
import ru.gosuslugi.pgu.fs.service.TransformService;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;
import ru.gosuslugi.pgu.fs.utils.OrderBehaviourTypeUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioInitializerServiceImpl implements ScenarioInitializerService {

    private final PguOrderService pguOrderService;
    private final DraftClient draftClient;
    private final UserPersonalData userPersonalData;
    private final TransformService transformService;
    private final DeliriumService deliriumService;
    private final OrderInfoService orderInfoService;
    private final MainDescriptorService mainDescriptorService;
    private final FormScenarioDtoServiceImpl scenarioDtoService;
    private final ErrorModalDescriptorService errorModalDescriptorService;
    private final DisclaimersService disclaimersService;

    @Override
    public ScenarioResponse getInitScreen(InitServiceDto initServiceDto, String serviceId) {
        Order order = pguOrderService.findLastOrder(serviceId, initServiceDto.getTargetId());
        if(nonNull(order)) {
            pguOrderService.deleteOrderById(order.getId());
        }
        return getInitScreen(serviceId, initServiceDto, initServiceDto.getTargetId());
    }

    @Override
    public ScenarioResponse getInvitedScenario(InitServiceDto initServiceDto, String serviceId) {
        Order order = new Order();
        order.setId(Long.valueOf(initServiceDto.getOrderId()));
        ScenarioResponse scenarioResponse = getInitScreenWithExistedOrderId(serviceId, order, initServiceDto);
        scenarioResponse.setIsInviteScenario(true);
        return scenarioResponse;
    }

    @Override
    public ScenarioResponse getInitScreenWithExistedOrderId(String serviceId, Order order, InitServiceDto initServiceDto) {
        // случай когда был отрыт order по ссылке с другим serviceId, для обжалования проверяется targetId
        if (!(Objects.equals(serviceId, order.getEserviceId())
                || Objects.equals(initServiceDto.getTargetId(), order.getServiceTargetId()))) {
            throw new OrderIncompatibleServiceException(errorModalDescriptorService.getErrorModal(ErrorModalView.DRAFT_NOT_FOUND));
        }
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(serviceId);

        DraftHolderDto draftHolderDto = draftClient.getDraftById(order.getId(), userPersonalData.getUserId(), userPersonalData.getOrgId());

        // случай когда был создан order, но не было ни одного сохранения в черновик
        if (isNull(draftHolderDto) ||
            isNull(draftHolderDto.getBody()) ||
            OrderBehaviourTypeUtil.getSmevOrderDraftFlag(serviceDescriptor, order, true)
        ) {
            return getInitScreen(serviceId, initServiceDto, order.getServiceTargetId());
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

        // Получаем роль пользователя, стадию заполнения заявки и состояние черновика
        ApplicantRole role = deliriumService.getUserRole(draftHolderDto);
        DeliriumStageDto deliriumStageDto = getDeliriumStageDto(serviceId, order, role);
        OrderInfoDto orderInfoDto = orderInfoService.getOrderInfo(order, role, deliriumStageDto, draftHolderDto, serviceDescriptor.getAlwaysContinueScenario());

        // Подготовка сценария
        ScenarioResponse scenarioResponse = new ScenarioResponse();
        scenarioResponse.setIsInviteScenario(orderInfoDto.getIsInviteScenario());
        scenarioResponse.setCanStartNew(orderInfoDto.getCanStartNew());
        scenarioResponse.setScenarioDto(draftHolderDto.getBody());

        List<DisclaimerDto> disclaimers = disclaimersService.getDisclaimers(serviceId, initServiceDto.getTargetId());
        scenarioResponse.getScenarioDto().setDisclaimers(disclaimers);
        Optional<DisclaimerDto> criticalDisclaimer = disclaimers.stream().filter(disclaimerDto -> DisclaimerLevel.CRITICAL.equals(disclaimerDto.getLevel())).findFirst();
        if (criticalDisclaimer.isPresent()) {
            scenarioResponse.getScenarioDto().setDisclaimers(Collections.emptyList());
            // меняем экран на экран с сообщением о критической ошибке
            scenarioResponse.getScenarioDto().setDisplay(disclaimersService.getDisplayForCriticalDisclaimer(criticalDisclaimer.get(), scenarioResponse.getScenarioDto()));
            return scenarioResponse;
        }

        scenarioDtoService.prepareScenarioDto(scenarioResponse.getScenarioDto(), serviceDescriptor,
                serviceId, deliriumStageDto, role);

        return scenarioResponse;
    }

    @Override
    public ScenarioResponse getExistingScenario(InitServiceDto initServiceDto, String serviceId) {
        /* В случае если у ордера нет serviceID (Это возможно в кейсе умного поиска) */
        Order order = pguOrderService.findOrderByIdCached(Long.valueOf(initServiceDto.getOrderId()));
        if(Strings.isEmpty(order.getEserviceId())) {
            order.setEserviceId(serviceId);
        }
        return getInitScreenWithExistedOrderId(serviceId,order, initServiceDto);
    }

    private void setStatusId(ScenarioDto scenarioDto, Long statusId) {
        scenarioDto.getServiceInfo().setStatusId(statusId);
    }

    private ScenarioResponse getInitScreen(String serviceId, InitServiceDto initServiceDto, String targetId) {
        ScenarioResponse scenarioResponse = new ScenarioResponse();

        ServiceDescriptor descriptor = mainDescriptorService.getServiceDescriptor(serviceId);
        if (!descriptor.isMultipleOrders() && !pguOrderService.allTerminated(serviceId, targetId))
            throw new DuplicateOrderException();

        if (nonNull(initServiceDto.getGepsId())) {
            List<Long> ignoreStatuses = List.of(OrderStatuses.DRAFT.getStatusId(), OrderStatuses.ERROR_SEND_REQUEST.getStatusId());
            List<Order> executedOrders = pguOrderService.findOrdersWithoutStatuses(serviceId, targetId, ignoreStatuses);
            Long gepsId = initServiceDto.getGepsId();
            executedOrders.forEach(order -> {
                DraftHolderDto draft = draftClient.getDraftById(order.getId(), userPersonalData.getUserId(), userPersonalData.getOrgId());
                if (nonNull(draft) && nonNull(draft.getBody())
                        && gepsId.equals(draft.getBody().getGepsId()))
                    throw new CreateOrderException(errorModalDescriptorService.getErrorModal(ErrorModalView.GEPS),
                            String.format("GepsId: %s использован в заявке: %s", gepsId, order.getId()));
            });
        }

        List<DisclaimerDto> disclaimers = disclaimersService.getDisclaimers(serviceId, targetId);
        ScenarioDto scenarioDto = scenarioDtoService.createInitScenario(descriptor, initServiceDto);
        Optional<DisclaimerDto> criticalDisclaimer = disclaimers.stream().filter(disclaimerDto -> DisclaimerLevel.CRITICAL.equals(disclaimerDto.getLevel())).findFirst();
        if (criticalDisclaimer.isPresent()) {
            // если есть критичиески дисклеймер меняем экран на экран с сообщением о критической ошибке
            disclaimers = Collections.emptyList();
            scenarioDto.setDisplay(disclaimersService.getDisplayForCriticalDisclaimer(criticalDisclaimer.get(), scenarioDto));
        }
        scenarioResponse.setScenarioDto(scenarioDto);
        scenarioDto.setDisclaimers(disclaimers);

        // устанавливаем единожды для передачи в делириум,
        // предполагаем, что при клонировании черновика атрибуты останутся
        scenarioDto.setServiceCode(serviceId);
        scenarioDto.setServiceDescriptorId(serviceId);
        scenarioDto.setTargetCode(targetId);

        return scenarioResponse;
    }

    private DeliriumStageDto getDeliriumStageDto(String serviceId, Order order, ApplicantRole role) {
        boolean useDefaultStage = mainDescriptorService.getServiceDescriptor(serviceId).checkOnlyOneApplicantAndStage();
        return deliriumService.getStage(role, order.getId(), useDefaultStage);
    }
}
