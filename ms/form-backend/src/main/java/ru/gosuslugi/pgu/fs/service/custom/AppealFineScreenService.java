package ru.gosuslugi.pgu.fs.service.custom;


import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.atc.carcass.security.rest.model.ResponseCode;
import ru.gosuslugi.pgu.fs.exception.ErrorScreenException;
import ru.gosuslugi.pgu.common.core.exception.dto.ExternalErrorInfo;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.payment.BillData;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.*;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.OrderType;
import ru.gosuslugi.pgu.dto.order.OrderListInfoDto;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.generator.ScenarioGeneratorClient;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.EmpowermentService;
import ru.gosuslugi.pgu.fs.service.ScenarioInitializerService;
import ru.gosuslugi.pgu.fs.service.TransformService;
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl;
import ru.gosuslugi.pgu.fs.service.impl.MainScreenServiceImpl;
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService;
import ru.gosuslugi.pgu.core.lk.model.order.Order;

import java.util.*;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.warn;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppealFineScreenService extends AbstractCustomScreenService {

    private static final int DUPLICATE_ORDERS_ERROR_CODE = 2;
    private static final String SERVICE_DESCRIPTOR_ID_PATTERN = "%s_%s";
    private static final String PREVIOUS_DRAFT_DATA_KEY = "previousData";

    private final MainScreenServiceImpl mainScreenService;
    private final ScenarioGeneratorClient scenarioGeneratorClient;
    private final MainDescriptorService mainDescriptorService;
    private final FormScenarioDtoServiceImpl scenarioDtoService;
    private final EmpowermentService empowermentService;
    private final PguOrderService pguOrderService;
    private final BillingService billingService;
    private final UserPersonalData userPersonalData;
    private final ScenarioInitializerService scenarioInitializerService;
    private final TransformService transformService;
    private final DraftClient draftClient;

    @Override
    public String getTargetId() {
        return "-10000000305";
    }


    @Override
    public ScenarioResponse getExistingScenario(InitServiceDto initServiceDto, String serviceId) {
        Long orderId = Long.parseLong(initServiceDto.getOrderId());
        String generatedServiceDescriptorId = String.format(SERVICE_DESCRIPTOR_ID_PATTERN, serviceId, orderId);
        scenarioGeneratorClient.generateAdditionalSteps(generatedServiceDescriptorId);
        mainDescriptorService.clearCachedDescriptor(generatedServiceDescriptorId);
        return getNewScenarioResponse(initServiceDto, serviceId, orderId, generatedServiceDescriptorId);
    }

    @Override
    public ScenarioResponse getInitScreen(String serviceId, InitServiceDto initServiceDto) {
        Long orderId = pguOrderService.createOrderId(serviceId,
                initServiceDto.getTargetId(),
                OrderType.valueOf(initServiceDto.getServiceInfo().getOrderType().toUpperCase()),
                null);
        BillData billData = billingService.setBillToOrder(
                userPersonalData.getToken(),
                serviceId,
                orderId,
                initServiceDto.getServiceInfo().getBillNumber());
        if (billData.getErrorCode() == DUPLICATE_ORDERS_ERROR_CODE && !CollectionUtils.isEmpty(billData.getOrderDuplicates())) {
            Long oldOrderId = billData.getOrderDuplicates().get(0).getId();
            Optional<ScenarioResponse> existingOrderResponse = getExistingOrderScenarioResponse(serviceId, initServiceDto, billData, oldOrderId);
            if (existingOrderResponse.isPresent()) {
                return existingOrderResponse.get();
            }
        }
        if (billData.getErrorCode() != ResponseCode.OK_RESULT.getCode()) {
            warn(log, () -> String.format("Ошибка вызова внешнего сервиса, код = %s, сообшение = \"%s\"", billData.getErrorCode(), billData.getErrorMessage()));
            throw new ErrorScreenException(billData.getErrorCode(), new ExternalErrorInfo("setBillToOrder", "", HttpMethod.POST, billData.getErrorMessage(), null));
        }
        String generatedServiceDescriptorId = String.format(SERVICE_DESCRIPTOR_ID_PATTERN, serviceId, orderId);
        ScenarioGeneratorDto scenarioGeneratorDto = ScenarioGeneratorDto.builder()
                .serviceId(generatedServiceDescriptorId)
                .routeNumber(initServiceDto.getServiceInfo().getRouteNumber())
                .billNumber(billData.getBillNumber())
                .billDate(billData.getBillDate())
                .token(userPersonalData.getToken())
                .build();
        scenarioGeneratorClient.generateScenario(scenarioGeneratorDto);
        return getNewScenarioResponse(initServiceDto, serviceId, orderId, generatedServiceDescriptorId);
    }

    private Optional<ScenarioResponse> getExistingOrderScenarioResponse(String serviceId, InitServiceDto initServiceDto, BillData billData, Long orderId) {
        Order order = pguOrderService.findOrderByIdCached(orderId);
        String generatedServiceDescriptorId = String.format(SERVICE_DESCRIPTOR_ID_PATTERN, serviceId, order.getId());
        ScenarioGeneratorDto scenarioGeneratorDto = ScenarioGeneratorDto.builder()
                .serviceId(generatedServiceDescriptorId)
                .routeNumber(initServiceDto.getServiceInfo().getRouteNumber())
                .billNumber(billData.getBillNumber())
                .billDate(billData.getBillDate())
                .token(userPersonalData.getToken())
                .build();
        scenarioGeneratorClient.generateScenario(scenarioGeneratorDto);
        if (!transformService.isAcceptedCode(order.getOrderStatusId(), generatedServiceDescriptorId)) {
            warn(log, () -> String.format("Завление %s со статусом %s не может быть переиспользовано", order.getId(), order.getOrderStatusId()));
            return Optional.empty();
        }
        initServiceDto.setOrderId(String.valueOf(order.getId()));
        return Optional.of(scenarioInitializerService.getInitScreenWithExistedOrderId(generatedServiceDescriptorId, order, initServiceDto));
    }

    private ScenarioResponse getNewScenarioResponse(InitServiceDto initServiceDto, String serviceId, Long orderId, String generatedServiceDescriptorId) {
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(generatedServiceDescriptorId);
        List<String> empowermentList = serviceDescriptor.getEmpowerments();
        if (!empowermentList.isEmpty() && !empowermentService.hasEmpowerment(empowermentList)) {
            throw new ErrorScreenException(ErrorScreenException.NO_EMPOWERMENT_CODE,
                    ExternalErrorInfo.builder()
                            .id("generateScenario")
                            .message("Нет доверенности для прохождения услуги")
                            .build());
        }
        ApplicantAnswer previous = getPreviousDataAsApplicantAnswer(orderId);
        ScenarioResponse scenarioResponse = new ScenarioResponse();
        ScenarioDto scenarioDto = scenarioDtoService.createInitScenario(serviceDescriptor, initServiceDto);
        scenarioDto.setTargetCode(initServiceDto.getTargetId());
        scenarioDto.setServiceCode(serviceId);
        scenarioDto.setServiceDescriptorId(generatedServiceDescriptorId);
        scenarioDto.setOrderId(orderId);
        if (Objects.nonNull(previous)) {
            scenarioDto.getApplicantAnswers().put(PREVIOUS_DRAFT_DATA_KEY, previous);
        }
        scenarioResponse.setScenarioDto(scenarioDto);
        return scenarioResponse;
    }

    @Override
    public OrderListInfoDto getOrderInfo(InitServiceDto initServiceDto, String serviceId) {
        return new OrderListInfoDto();
    }

    @Override
    public OrderListInfoDto getOrderInfoById(InitServiceDto initServiceDto, String serviceId) {
        serviceId = String.format(SERVICE_DESCRIPTOR_ID_PATTERN, serviceId, initServiceDto.getOrderId());
        return mainScreenService.getOrderInfoById(initServiceDto, serviceId);
    }

    @Override
    public void setStatusId(ScenarioDto scenarioDto) {
        mainScreenService.setStatusId(scenarioDto);
    }

    @Override
    public ScenarioResponse prepareScenarioFromExternal(ScenarioFromExternal scenarioFromExternal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScenarioResponse getNextScreen(ScenarioRequest request, String serviceId) {
        serviceId = request.getScenarioDto().getServiceDescriptorId();
        return mainScreenService.getNextScreen(request, serviceId);
    }

    @Override
    public ScenarioResponse skipStep(ScenarioRequest request, String serviceId) {
        serviceId = request.getScenarioDto().getServiceDescriptorId();
        return mainScreenService.skipStep(request, serviceId);
    }

    @Override
    public ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId) {
        serviceId = request.getScenarioDto().getServiceDescriptorId();
        return mainScreenService.getPrevScreen(request, serviceId);
    }

    @Override
    public ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId, Integer stepsBack) {
        serviceId = request.getScenarioDto().getServiceDescriptorId();
        return mainScreenService.getPrevScreen(request, serviceId, stepsBack);
    }

    private ApplicantAnswer getPreviousDataAsApplicantAnswer(Long orderId) {
        ApplicantAnswer applicantAnswer = new ApplicantAnswer();
        DraftHolderDto draftHolderDto = draftClient.getDraftById(orderId, userPersonalData.getUserId(), userPersonalData.getOrgId());
        if (Objects.isNull(draftHolderDto) || Objects.isNull(draftHolderDto.getBody())
                && Objects.isNull(draftHolderDto.getBody().getApplicantAnswers())) {
            return null;
        }
        Map<String, ApplicantAnswer> previousAnswers = draftHolderDto.getBody().getApplicantAnswers();
        Map<String, String> previousValues = previousAnswers.entrySet()
                .stream().filter(e -> !e.getKey().equals(PREVIOUS_DRAFT_DATA_KEY) && Objects.nonNull(e.getValue()))
                .collect(HashMap::new, (map, entry)-> map.put(entry.getKey(), entry.getValue().getValue()), HashMap::putAll);        if (previousAnswers.containsKey(PREVIOUS_DRAFT_DATA_KEY) && Objects.nonNull(previousAnswers.get(PREVIOUS_DRAFT_DATA_KEY))) {
            previousValues.putAll(
                    JsonProcessingUtil.fromJson(
                            previousAnswers.get(PREVIOUS_DRAFT_DATA_KEY).getValue(), new TypeReference<Map<String, String>>() {}
                    )
            );
        }
        applicantAnswer.setValue(JsonProcessingUtil.toJson(previousValues));
        applicantAnswer.setVisited(true);
        return applicantAnswer;
    }
}
