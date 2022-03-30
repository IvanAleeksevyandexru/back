package ru.gosuslugi.pgu.fs.service.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.atc.carcass.common.exception.FaultException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitOverHeadDto;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.exception.NoRightToCreateOrderException;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitOverHeadDto;
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitRequest;
import ru.gosuslugi.pgu.fs.common.exception.NoRightToCreateOrderException;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService;
import ru.gosuslugi.pgu.fs.exception.DuplicateOrderException;
import ru.gosuslugi.pgu.fs.pgu.dto.PguServiceCodes;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.CreateOrderService;
import ru.gosuslugi.pgu.fs.service.EmpowermentService;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.fs.service.ratelimit.RateLimitAnalyticProducer;
import ru.gosuslugi.pgu.ratelimit.client.RateLimitService;
import ru.gosuslugi.pgu.ratelimit.client.exception.RateLimitServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderServiceImpl implements CreateOrderService {

    private static final String KEY_FOR_CHECKING_AND_SAVE_VALUES = "checkValues";
    private static final String VALUES_FOR_SAVE_ATTR_ACTION = "valuesForSave";

    private final PguOrderService pguOrderService;
    private final JsonProcessingService jsonProcessingService;
    private final UserCookiesService userCookiesService;
    private final EmpowermentService empowermentService;
    private final LkNotifierService lkNotifierService;
    private final RateLimitService rateLimitService;
    private final UserPersonalData userPersonalData;
    private final RateLimitAnalyticProducer rateLimitAnalyticProducer;
    /**
     * Проверяет нужно ли создавать order на данным этапе и если нужно создает его
     * @param serviceId         Идентификатор услуги
     * @param scenarioDto       Сценарий
     * @param serviceDescriptor Дескриптор сервиса
     * @return                  Идентификатор ордера
     */
    @Override
    public Long tryToCreateOrderId(String serviceId, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        Long orderId = scenarioDto.getOrderId();
        Boolean serviceHasCreateOrderFlag = serviceDescriptor.hasOrderCreateCustomParameter();
        if (!serviceHasCreateOrderFlag && orderId != null) {
            return orderId;
        }

        String userRegionOkato = null;
        if (Objects.nonNull(scenarioDto.getServiceInfo())
                && Objects.nonNull(scenarioDto.getServiceInfo().getUserRegion())
                && scenarioDto.getServiceInfo().getUserRegion().getCodes().size() > 0) {
            userRegionOkato = scenarioDto.getServiceInfo().getUserRegion().getCodes().get(0);
            userCookiesService.setUserSelectedRegion(userRegionOkato);
        }

        // Тут добавить проверки на возможность создания заявления
        if(!empowermentService.checkServiceAvailableForUser(scenarioDto.getTargetCode())){
            var empowermentList = serviceDescriptor.getEmpowerments();
            if(empowermentList.isEmpty() || !empowermentService.hasEmpowerment(empowermentList)){
                throw new NoRightToCreateOrderException();
            }
        }

        // Поддержка обратной совместимости. Если в услуге нет компонента с флагом createOrder - то создаем ордер на втором шаге
        if (!serviceHasCreateOrderFlag && scenarioDto.getFinishedAndCurrentScreens().size() == 1) {
            checkLimit(serviceDescriptor, serviceId);
            orderId = pguOrderService.createOrderId(serviceId,
                    scenarioDto.getTargetCode(),
                    serviceDescriptor.getOrderType(),
                    serviceDescriptor.getHighloadParameters()
            );
            if(serviceDescriptor.getAnalyticsTags().isEmpty()){
                lkNotifierService.updateOrderRegion(orderId, userRegionOkato);
            }
            return orderId;
        }

        // если в компоненте присутствует флаг createOrder, то создаем ордер
        if (shouldCreateOrder(scenarioDto, serviceDescriptor, FieldComponent::getCreateOrder)) {
            checkLimit(serviceDescriptor, serviceId);
            orderId = pguOrderService.createOrderId(serviceId,
                    scenarioDto.getTargetCode(),
                    serviceDescriptor.getOrderType(),
                    serviceDescriptor.getHighloadParameters()
            );
            if(serviceDescriptor.getAnalyticsTags().isEmpty()){
                lkNotifierService.updateOrderRegion(orderId, userRegionOkato);
            }
        }
        return orderId;
    }

    /**
     * Сохраняет ответы пользователя по услуге
     * @param descriptor    Дескриптор сервиса
     * @param scenarioDto   Сценарий
     * @return              Признак сохранения
     */
    @Override
    public Boolean saveValuesForOrder(ServiceDescriptor descriptor, ScenarioDto scenarioDto) {
        List<FieldComponent> answersForSaving = scenarioDto.getApplicantAnswers().keySet().stream()
                .map(descriptor::getFieldComponentById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(fieldComponent -> fieldComponent.getCreateOrder() || fieldComponent.getCheckForDuplicate())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(answersForSaving)) {
            return true;
        }
        answersForSaving.stream()
                .filter(FieldComponent::isCycled)
                .forEach(component -> {
                    String cycledComponent = component.getId() + ".value";
                    List<String> cycledAnswers = getCycledFieldsForCheck(component, cycledComponent);
                    List<String> valuesForCycledComponent = getValuesForCheckForCycledComponent(cycledAnswers, scenarioDto.getApplicantAnswers(), cycledComponent);
                    for (String valueForCheck: valuesForCycledComponent) {
                        pguOrderService.saveChoosenValuesForOrder(scenarioDto.getServiceCode(), scenarioDto.getTargetCode(), scenarioDto.getOrderId(), Map.of(KEY_FOR_CHECKING_AND_SAVE_VALUES, Map.of(cycledComponent, valueForCheck)));
                    }
                });
        Map<String, String> valuesToSave = new HashMap<>();
        if (!CollectionUtils.isEmpty(answersForSaving)) {
            for (FieldComponent component: answersForSaving) {
                if (component.isCycled()) {
                    continue;
                }
                valuesToSave.putAll(getMapValuesForCheck(component.getId(), scenarioDto.getApplicantAnswers(), scenarioDto.getCurrentValue(), component.getFieldsForCheck(), descriptor));
                saveAdditionalValues(component, valuesToSave, scenarioDto.getServiceCode(), scenarioDto.getTargetCode(), scenarioDto.getOrderId());
            }
        }
        return pguOrderService.saveChoosenValuesForOrder(scenarioDto.getServiceCode(), scenarioDto.getTargetCode(), scenarioDto.getOrderId(), Map.of(KEY_FOR_CHECKING_AND_SAVE_VALUES, valuesToSave));
    }

    @Override
    public Boolean checkForDuplicate(ScenarioDto scenarioDto, Optional<FieldComponent> fieldComponent, ServiceDescriptor serviceDescriptor) {
        if (fieldComponent.isEmpty()) {
            return true;
        }

        FieldComponent component = fieldComponent.get();
        ApplicantAnswer applicantAnswer = scenarioDto.getCurrentValue().get(component.getId());
        if (Objects.isNull(applicantAnswer) || Objects.isNull(applicantAnswer.getValue())) {
            throw new DuplicateOrderException("Ошибка при проверке наличия дубликатов - applicantAnswer или его значение пусто");
        }

        String serviceCode = scenarioDto.getServiceCode();
        String targetCode = scenarioDto.getTargetCode();
        try {
            PguServiceCodes pguServiceCodes = pguOrderService.getPguServiceCodes(serviceCode, targetCode);
            if (Objects.nonNull(pguServiceCodes)
                    && StringUtils.hasText(pguServiceCodes.getPassport())
                    && StringUtils.hasText(pguServiceCodes.getTarget())) {
                serviceCode = pguServiceCodes.getPassport();
                targetCode = pguServiceCodes.getTarget();
            }
        } catch (FaultException e) {
            log.warn("Не найдены passport и target коды по запросу в ЛК для serviceCode = {} и targetCode = {}. {}",
                    serviceCode, targetCode, e.getMessage());
        }

        Map<String, Object> userAnswer;
        List<String> valuesForCycledComponent;
        List<String> fieldsForCheck = CollectionUtils.isEmpty(component.getFieldsForCheck())
                ? new ArrayList<>()
                : new ArrayList<>(component.getFieldsForCheck());

        if (component.isCycled() && !fieldsForCheck.isEmpty()) {
            String cycledComponent = component.getId() + ".value";
            List<String> cycledAnswers = getCycledFieldsForCheck(component, cycledComponent);
            valuesForCycledComponent = getValuesForCheckForCycledComponent(cycledAnswers, scenarioDto.getCurrentValue(), cycledComponent);
            for (String valueForCheck: valuesForCycledComponent) {
                pguOrderService.hasDuplicatesForOrder(
                        serviceCode,
                        targetCode,
                        Map.of(KEY_FOR_CHECKING_AND_SAVE_VALUES, Map.of(cycledComponent, valueForCheck))
                );
            }
            fieldsForCheck.removeAll(cycledAnswers);
            if (fieldsForCheck.isEmpty()) {
                return false;
            }
        }
        Map<String, String> valuesForCheck = getMapValuesForCheck(component, scenarioDto.getApplicantAnswers(), scenarioDto.getCurrentValue(), fieldsForCheck, serviceDescriptor);
        userAnswer = Map.of(KEY_FOR_CHECKING_AND_SAVE_VALUES, valuesForCheck);
        return pguOrderService.hasDuplicatesForOrder(serviceCode, targetCode, userAnswer);
    }

    private List<String> getCycledFieldsForCheck(FieldComponent component, String cycledComponent) {
        if (component.getFieldsForCheck() == null) {
            return new ArrayList<>();
        }
        return component.getFieldsForCheck().stream()
                .filter(Objects::nonNull)
                .filter(field -> field.startsWith(cycledComponent))
                .collect(Collectors.toList());
    }

    private List<String> getValuesForCheckForCycledComponent(List<String> cycledAnswers, Map<String, ApplicantAnswer> applicantAnswer, String cycledValues) {
        if (CollectionUtils.isEmpty(cycledAnswers) || applicantAnswer == null) {
            return new ArrayList<>();
        }
        jsonProcessingService.releaseThreadCache();
        DocumentContext currentAnswerContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(applicantAnswer));
        List<String> result = new ArrayList<>();
        List<Map<String, String>> cycles = jsonProcessingService.getFieldFromContext(cycledValues, currentAnswerContext, List.class);
        String prefix = cycledValues + ".";
        for (Map<String, String> cycle : cycles) {
            String value = cycledAnswers.stream()
                    .map(field -> field.replaceFirst(prefix, ""))
                    .map(cycle::get)
                    .collect(Collectors.joining(" "));
            result.add(value);
        }
        return result;
    }

    /**
     * Заполняем карту значений проверок
     */
    private Map<String, String> getMapValuesForCheck(FieldComponent component, Map<String, ApplicantAnswer> applicantAnswers,
                                                     Map<String, ApplicantAnswer> currentAnswer, List<String> fieldsForCheck,
                                                     ServiceDescriptor serviceDescriptor) {
        // сначала смотрим атрибут valuesForSave в answers - если он есть, берем значение оттуда
        if (component.getAttrs().containsKey("answers")) {
            List<Map<String, Object>> answers = (List) component.getAttrs().get("answers");
            String componentValue = currentAnswer.get(component.getId()).getValue();
            Map<String, String> result = new HashMap<>();

            answers.stream().filter(answer -> StringUtils.hasText((String) answer.get("value"))
                    && componentValue.equalsIgnoreCase((String) answer.get("value"))
                    && answer.get(VALUES_FOR_SAVE_ATTR_ACTION) instanceof List)
                    .findFirst()
                    .map(answer -> (List<Map<String, String>>) answer.get(VALUES_FOR_SAVE_ATTR_ACTION))
                    .ifPresent(valuesForSave -> valuesForSave.forEach(value -> result.put(value.get("key"), value.get("value"))));
            return result;
        }

        return getMapValuesForCheck(component.getId(), applicantAnswers, currentAnswer, fieldsForCheck, serviceDescriptor);
    }

    private Map<String, String> getMapValuesForCheck(String componentId, Map<String, ApplicantAnswer> applicantAnswers,
                                                     Map<String, ApplicantAnswer> currentAnswer, List<String> fieldsForCheck,
                                                     ServiceDescriptor serviceDescriptor) {
        Map<String, String> result = new HashMap<>();
        addValuesFromApplicantAnswers(componentId, result, fieldsForCheck, currentAnswer, serviceDescriptor);
        addValuesFromApplicantAnswers(componentId, result, fieldsForCheck, applicantAnswers, serviceDescriptor);
        return result;
    }

    private void addValuesFromApplicantAnswers(String componentId, Map<String, String> result,
                                               List<String> fieldsForCheck, Map<String, ApplicantAnswer> applicantAnswers,
                                               ServiceDescriptor serviceDescriptor) {
        if (CollectionUtils.isEmpty(applicantAnswers)) {
            return;
        }
        if (CollectionUtils.isEmpty(fieldsForCheck)) {
            String value = applicantAnswers.get(componentId) == null ? "" : applicantAnswers.get(componentId).getValue();
            if (StringUtils.hasText(value)) {
                result.put(componentId, value);
            }

            return;
        }
        DocumentContext currentAnswerContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(applicantAnswers));
        for (String field : fieldsForCheck) {
            if (Objects.nonNull(field) && field.contains(VALUES_FOR_SAVE_ATTR_ACTION)) {
                calculateValuesForSaveLink(field, applicantAnswers, result, serviceDescriptor);
            } else {
                String value = jsonProcessingService.getFieldFromContext(field, currentAnswerContext, String.class);
                if (value != null) {
                    result.put(field, value);
                }
            }
        }
    }

    private void calculateValuesForSaveLink(String fieldForCheck, Map<String, ApplicantAnswer> applicantAnswers,
                                            Map<String, String> result, ServiceDescriptor serviceDescriptor) {
        int index = fieldForCheck.indexOf(VALUES_FOR_SAVE_ATTR_ACTION);
        String componentId = fieldForCheck.substring(0, index - 1);
        FieldComponent fieldComponent = serviceDescriptor.getFieldComponentById(componentId).orElse(null);

        if (Objects.nonNull(fieldComponent) && Objects.nonNull(applicantAnswers.get(componentId)) && fieldComponent.getAttrs().containsKey("answers")) {
            List<Map<String, Object>> answers = (List) fieldComponent.getAttrs().get("answers");
            String componentValue = applicantAnswers.get(componentId).getValue();

            answers.stream().filter(answer -> StringUtils.hasText((String) answer.get("value"))
                    && componentValue.equalsIgnoreCase((String) answer.get("value"))
                    && answer.get(VALUES_FOR_SAVE_ATTR_ACTION) instanceof List)
                    .findFirst()
                    .map(answer -> (List<Map<String, String>>) answer.get(VALUES_FOR_SAVE_ATTR_ACTION))
                    .ifPresent(valuesForSave -> valuesForSave.forEach(value -> result.put(value.get("key"), value.get("value"))));
        }
    }

    private void saveAdditionalValues(FieldComponent component, Map<String, String> valuesToSave, String serviceCode, String targetCode, Long orderId) {
        String componentValue = valuesToSave.get(component.getId());
        if (!StringUtils.hasText(componentValue)) {
            return;
        }
        if (CollectionUtils.isEmpty(component.getAttrs())) {
            return;
        }
        Map<String, Object> attrs = component.getAttrs();
        if (attrs.get("answers") == null) {
            return;
        }
        List<Map<String, Object>> answers = (List) attrs.get("answers");
        for (Map<String, Object> answer : answers) {
            if (!StringUtils.hasText((String) answer.get("value"))
                    || !componentValue.equalsIgnoreCase((String) answer.get("value"))
                    || !(answer.get(VALUES_FOR_SAVE_ATTR_ACTION) instanceof List)) {
                continue;
            }
            if (CollectionUtils.isEmpty((List<Map>) answer.get(VALUES_FOR_SAVE_ATTR_ACTION))) {
                valuesToSave.remove(component.getId());
            }
            ((List<Map>) answer.get(VALUES_FOR_SAVE_ATTR_ACTION))
                    .forEach(value -> pguOrderService.saveChoosenValuesForOrder(serviceCode, targetCode, orderId, Map.of(KEY_FOR_CHECKING_AND_SAVE_VALUES, Map.of(value.get("key"), value.get("value")))));
        }
    }

    private Boolean shouldCreateOrder(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor, Predicate<FieldComponent> predicate) {
        if (Objects.nonNull(scenarioDto.getOrderId())) {
            return false;
        }
        return scenarioDto.getDisplay().getComponents().stream()
                .map(component -> serviceDescriptor.getFieldComponentById(component.getId()))
                .filter(Objects::nonNull)
                .map(Optional::get)
                .anyMatch(predicate);
    }

    private void checkLimit(ServiceDescriptor serviceDescriptor, String serviceCode){

        var rateLimitRequest = new RateLimitRequest();
        var rateLimitDescriptor = serviceDescriptor.getRateLimits();
        String key = userPersonalData.getUserId() + "-" + serviceCode;
        if (Objects.nonNull(rateLimitDescriptor)){
            rateLimitRequest.setLimit(rateLimitDescriptor.getLimit());
            rateLimitRequest.setTtl(rateLimitDescriptor.getTtl());
        }

        try {
            rateLimitService.apiCheck(rateLimitRequest,key);
        } catch (RateLimitServiceException e){
            var overhead = new RateLimitOverHeadDto();
            overhead.setRateLimitRequest(rateLimitRequest);
            overhead.setUserId(String.valueOf(userPersonalData.getUserId()));
            overhead.setOrgId(String.valueOf(userPersonalData.getOrgId()));
            overhead.setServiceId(serviceCode);
            rateLimitAnalyticProducer.send(overhead);
            /**
             * Тут был красивый эксепшен
             * Но теперь нужен не красивый
             * https://jira.egovdev.ru/browse/EPGUCORE-84549
             */
            throw new FormBaseException("Не сработало");
        }
    }
}
