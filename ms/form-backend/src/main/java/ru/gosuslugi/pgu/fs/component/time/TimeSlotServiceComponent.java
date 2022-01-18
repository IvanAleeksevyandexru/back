package ru.gosuslugi.pgu.fs.component.time;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.time.dto.ConfirmTimeSlotDto;
import ru.gosuslugi.pgu.fs.exception.BookingUnavailableException;
import ru.gosuslugi.pgu.fs.exception.InconsistentDataException;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeSlotServiceComponent extends AbstractComponent<String> {

    private static final String TIME_PERIOD_IN_MINUTES = "timePeriodInMinutes";
    private static final String RESOURCE_ID_ATTR = "Resource_Id";
    private static final String PATIENT_NAME_ATTR = "pacientname";
    private static final String DOCTOR_NAME_ATTR = "doctorname";
    private static final String DOCTOR_ATTR = "doctor";
    private static final String ANOTHER_PERSON_ATTR = "anotherperson";
    private static final String DOCTOR_ID_ATTR = "doctorid";
    private static final String AGE_PERSON_ATTR = "ageperson";
    private static final String GENDER_PERSON_ATTR = "genderperson";
    private static final String ORGANIZATION_ID_ATTR = "organizationId";
    private static final String BOOKING_UNAVAILABLE_EMPTY_ORG_ID = "BOOKING_UNAVAILABLE_EMPTY_ORG_ID";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private static final DateTimeFormatter PARAMS_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ParseAttrValuesHelper parseAttrValuesHelper;
    private final UserPersonalData personalData;
    private final CalculatedAttributesHelper calculatedAttributesHelper;
    private final MainDescriptorService mainDescriptorService;

    @Override
    public ComponentType getType() {
        return ComponentType.TimeSlot;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.isNull(component.getAttrs())) {
            return ComponentResponse.empty();
        }

        Map<String, Object> presetValues = getPresetValues(component, scenarioDto);
        var timeSlotPayload = calculateTimeSlotPayload(scenarioDto, component, presetValues);

        if (BOOKING_UNAVAILABLE_EMPTY_ORG_ID.equals(timeSlotPayload.get(ORGANIZATION_ID_ATTR))) {
            String address = Objects.toString(presetValues.get(ADDRESS));
            throw new BookingUnavailableException("Запись в данное подразделение невозможна", address);
        }
        return ComponentResponse.of(JsonProcessingUtil.toJson(timeSlotPayload));
    }

    private Map<String, Object> calculateTimeSlotPayload(ScenarioDto scenarioDto, FieldComponent component, Map<String, Object> presetValues) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(TIME_SLOT_TYPE_ATTR, getFromPresetOrResolve(TIME_SLOT_TYPE_ATTR, presetValues, component, scenarioDto));
        payload.put(DEPARTMENT_ATTR, getDepartment(component, scenarioDto, presetValues));
        payload.put(DEPARTMENT_REGION_ATTR, getFromPresetOrResolve(DEPARTMENT_REGION_ATTR, presetValues, component, scenarioDto));
        payload.put(SOLEMN_ATTR, getFromPresetOrResolve(SOLEMN_ATTR, presetValues, component, scenarioDto));
        payload.put(SLOTS_PERIOD_ATTR, getFromPresetOrResolve(SLOTS_PERIOD_ATTR, presetValues, component, scenarioDto));
        payload.put(BOOK_ATTRS_ATTR, getFromPresetOrResolve(BOOK_ATTRS_ATTR, presetValues, component, scenarioDto));

        var orderId = (Objects.isNull(scenarioDto.getOrderId()))
                ? personalData.generateUniqueUserValue()
                : scenarioDto.getOrderId();

        var serviceId = (StringUtils.isEmpty(presetValues.get(SERVICE_ID_ATTR)))
                ? resolveAttribute(SERVICE_ID_ATTR, component, scenarioDto)
                : String.valueOf(presetValues.get(SERVICE_ID_ATTR));

        payload.put("orderId", orderId);
        payload.put(SERVICE_ID_ATTR, serviceId);
        payload.put("parentOrderId", scenarioDto.getMasterOrderId());
        payload.put("userId", personalData.getUserId().toString());
        payload.put("waitingTimeExpired", isWaitingTimeExpired(component, scenarioDto));

        if ("medicalInfo".equals(component.getAttrs().get("externalIntegration"))) {
            var timeSlotRequestAttrs = timeSlotAttrs(presetValues, component, scenarioDto);
            var bookingRequestParams = bookingAttrs(presetValues, component, scenarioDto);
            payload.put("timeSlotRequestAttrs", timeSlotRequestAttrs);
            payload.put("bookingRequestParams", bookingRequestParams);
        }

        payload.putAll(calculatedAttributesHelper.getAllCalculatedValues(DICTIONARY_FILTER_NAME_ATTR, component, scenarioDto));
        payload.putAll(presetValues);
        return payload;
    }

    protected String getDepartment(FieldComponent component, ScenarioDto scenarioDto, Map<String, Object> presetValues) {
        final String department = (String) presetValues.get(DEPARTMENT_ATTR);
        if (StringUtils.isEmpty(department))
            return resolveAttribute(DEPARTMENT_ATTR, component, scenarioDto);

        return department;
    }

    private Map<String, Object> getPresetValues(FieldComponent component, ScenarioDto scenarioDto) {
        var allCalculatedValues = calculatedAttributesHelper.getAllCalculatedValues(CALCULATIONS_ATTR, component, scenarioDto);

        if (Objects.nonNull(component.getArguments())) {
            allCalculatedValues.putAll(component.getArguments());
        }
        if (allCalculatedValues.isEmpty()) {
            return Collections.emptyMap();
        }
        return allCalculatedValues;
    }

    private List<Map<String, Object>> bookingAttrs(Map<String, Object> presetValues, FieldComponent component, ScenarioDto scenarioDto) {
        var patientNameAttr = buildBookingAttrHolder(PATIENT_NAME_ATTR, presetValues, component, scenarioDto);
        var doctorNameAttr = buildBookingAttrHolder(DOCTOR_NAME_ATTR, presetValues, component, scenarioDto);
        var doctorAttr = buildBookingAttrHolder(DOCTOR_ATTR, presetValues, component, scenarioDto);
        var anotherPersonAttr = buildBookingAttrHolder(ANOTHER_PERSON_ATTR, presetValues, component, scenarioDto);
        var doctorIdAttr = buildBookingAttrHolder(DOCTOR_ID_ATTR, presetValues, component, scenarioDto);
        var agePersonAttr = buildBookingAttrHolder(AGE_PERSON_ATTR, presetValues, component, scenarioDto);
        var genderPersonAttr = buildBookingAttrHolder(GENDER_PERSON_ATTR, presetValues, component, scenarioDto);

        return List.of(
                patientNameAttr,
                doctorNameAttr,
                doctorAttr,
                anotherPersonAttr,
                doctorIdAttr,
                agePersonAttr,
                genderPersonAttr
        );
    }

    private List<Map<String, Object>> timeSlotAttrs(Map<String, Object> presetValues, FieldComponent component, ScenarioDto scenarioDto) {
        var sessionIdAttr = buildTimeSlotAttrHolder(SESSION_ID, presetValues, component, scenarioDto);
        var resourceIdAttr = buildTimeSlotAttrHolder(RESOURCE_ID_ATTR, presetValues, component, scenarioDto);
        var moIdAttr = buildTimeSlotAttrHolder(MO_ID, presetValues, component, scenarioDto);

        LocalDate startDate = LocalDate.now();
        var startDateAttr = buildNameValuePair(START_DATE_ATTR, PARAMS_DATE_TIME_FORMATTER.format(startDate));

        String defaultDayOffset = "14";
        String currentDayOffset = Objects.toString(presetValues.get(END_DATE_ATTR), defaultDayOffset);
        LocalDate endDate = startDate.plusDays(Long.parseLong(currentDayOffset));
        var endDateAttr = buildNameValuePair(END_DATE_ATTR, PARAMS_DATE_TIME_FORMATTER.format(endDate));

        var tsStartTimeAttr = buildTimeSlotAttrHolder(TS_START_TIME_ATTR, presetValues, component, scenarioDto);
        var tsEndDateAttr = buildTimeSlotAttrHolder(TS_END_TIME_ATTR, presetValues, component, scenarioDto);
        var tsServiceIdAttr = buildTimeSlotAttrHolder(TS_SERVICE_ID_ATTR, presetValues, component, scenarioDto);
        var serviceSpecIdAttr = buildTimeSlotAttrHolder(SERVICE_SPEC_ID_ATTR, presetValues, component, scenarioDto);

        return List.of(
                sessionIdAttr,
                resourceIdAttr,
                moIdAttr,
                startDateAttr,
                endDateAttr,
                tsStartTimeAttr,
                tsEndDateAttr,
                tsServiceIdAttr,
                serviceSpecIdAttr
        );
    }

    private Map<String, Object> buildNameValuePair(String name, String value) {
        return new HashMap<>() {{
            put("name", name);
            put("value", value);
        }};
    }

    protected String resolveAttribute(String attrName, FieldComponent component, ScenarioDto scenarioDto) {
        return Optional.ofNullable(component.getAttrs().get(attrName))
                .map(attr -> (Map<String, String>) attr)
                .map(attr -> parseAttrValuesHelper.getAttributeValue(attr, scenarioDto))
                .orElse(null);
    }

    private String getFromPresetOrResolve(String attribute, Map<String, Object> presetValues, FieldComponent component, ScenarioDto scenarioDto) {
        return (String) presetValues.getOrDefault(attribute, resolveAttribute(attribute, component, scenarioDto));
    }

    private Map<String, Object> buildTimeSlotAttrHolder(String attribute, Map<String, Object> presetValues, FieldComponent component, ScenarioDto scenarioDto) {
        String value = getFromPresetOrResolve(attribute, presetValues, component, scenarioDto);
        return buildNameValuePair(attribute, Objects.nonNull(value) ? value : "");
    }

    private Map<String, Object> buildBookingAttrHolder(String attribute, Map<String, Object> presetValues, FieldComponent component, ScenarioDto scenarioDto) {
        return buildNameValuePair(attribute, getFromPresetOrResolve(attribute, presetValues, component, scenarioDto));
    }

    private boolean isWaitingTimeExpired(FieldComponent component, ScenarioDto scenarioDto) {
        ApplicantAnswer previousSelectedSlot = Optional.ofNullable(scenarioDto.getCachedAnswers().get(component.getId()))
                .orElse(scenarioDto.getApplicantAnswers().get(component.getId()));

        if (previousSelectedSlot != null) {
            // слот уже был забронирован, проверяем не истекло ли время бронирования
            // берём время бронирования из тайм слота
            Map<String, Object> timeSlotMap = jsonProcessingService.fromJson(previousSelectedSlot.getValue(), new TypeReference<>() {});
            if (timeSlotMap.containsKey(TIME_FINISH_ATTR)) {
                ZonedDateTime expirationTime = ZonedDateTime.parse(timeSlotMap.get(TIME_FINISH_ATTR).toString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
                return ZonedDateTime.now().isAfter(expirationTime);
            }
        }
        return false;
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers,
                                       Map.Entry<String, ApplicantAnswer> entry,
                                       ScenarioDto scenarioDto,
                                       FieldComponent fieldComponent) {
        scenarioDto.getDisplay().getComponents()
                .stream()
                .filter(el -> el.getId().equals(fieldComponent.getId()))
                .findFirst()
                .ifPresent(el -> checkTimeSlotOrgIdAndSelectedOrgId(el, entry.getValue().getValue()));

        super.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent);
        boolean applicantRoleOnly = mainDescriptorService.getServiceDescriptor(scenarioDto.getServiceDescriptorId()).checkOnlyOneApplicantAndStage();
        if (incorrectAnswers.isEmpty()) addCurrentTimeAndFinishTimeToAnswer(entry, fieldComponent, applicantRoleOnly);
    }

    private void addCurrentTimeAndFinishTimeToAnswer(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent, boolean applicantRoleOnly) {
        Map<String, Object> answerMap = jsonProcessingService.fromJson(entry.getValue().getValue(), new TypeReference<>() {});
        ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        answerMap.put("currentTime", currentDateTime.format(DATE_TIME_FORMATTER));
        answerMap.put(TIME_START_ATTR, currentDateTime.format(DATE_TIME_FORMATTER));
        if (!applicantRoleOnly && answerMap.containsKey(TIME_FINISH_ATTR)) {
            ZonedDateTime timeFinish = ZonedDateTime.parse(answerMap.get(TIME_FINISH_ATTR).toString(), DATE_TIME_FORMATTER);
            if (currentDateTime.isBefore(timeFinish)) {
                entry.getValue().setValue(jsonProcessingService.toJson(answerMap));
                return;
            }
        }

        if (fieldComponent.getAttrs().containsKey(TIME_PERIOD_IN_MINUTES)) {
            String timePeriodInMinutes = fieldComponent.getAttrs().get(TIME_PERIOD_IN_MINUTES).toString();
            ZonedDateTime finishDate = currentDateTime.plusMinutes(Long.parseLong(timePeriodInMinutes));
            answerMap.put(TIME_FINISH_ATTR, finishDate.format(DATE_TIME_FORMATTER));
            entry.getValue().setValue(jsonProcessingService.toJson(answerMap));
            return;
        }
        throw new FormBaseException("Компонент Timeslot не содержит аттрибута['timePeriodInMinutes'] указание временного периода для таймера");
    }

    private void checkTimeSlotOrgIdAndSelectedOrgId(FieldComponent fieldComponent, String timeSlotRawValue) {
        String selectedOrgId = fieldComponent.getArgument("selectedOrgId");
        if (StringUtils.isEmpty(selectedOrgId)) return;
        try {
            var confirmedTimeSlot = jsonProcessingService.fromJson(timeSlotRawValue, ConfirmTimeSlotDto.class);
            String timesSlotOrgId = confirmedTimeSlot.getTimeSlot().getOrganizationId();
            if (!selectedOrgId.equals(timesSlotOrgId))
                throw new InconsistentDataException("Выбранный временной слот и подразделение не совпадают");
        } catch (JsonParsingException ignored) {
            log.error("Ошибка JSON преобразования: timeSlot: {}", timeSlotRawValue);
        }
    }
}
