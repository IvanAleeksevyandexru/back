package ru.gosuslugi.pgu.fs.component.time;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.TimeSlotDoctorInput;
import ru.gosuslugi.pgu.dto.TimeSlotRequestAttr;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.*;

@Component
@RequiredArgsConstructor
public class TimeSlotDoctorComponent extends AbstractComponent<TimeSlotDoctorInput> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final UserPersonalData userPersonalData;
    private final CalculatedAttributesHelper calculatedAttributesHelper;
    private final ParseAttrValuesHelper parseAttrValuesHelper;

    @Override
    public ComponentType getType() {
        return ComponentType.TimeSlotDoctor;
    }

    @Override
    public ComponentResponse<TimeSlotDoctorInput> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.nonNull(component.getAttrs())) {
            val presetValues = calculatedAttributesHelper.getAllCalculatedValues(
                    CALCULATIONS_ATTR, component, scenarioDto
            );
            presetValues.putAll(component.getArguments());

            return ComponentResponse.of(toTimeSlotDoctorInput(component, scenarioDto, presetValues));
        }
        return ComponentResponse.empty();
    }

    private TimeSlotDoctorInput toTimeSlotDoctorInput(
            FieldComponent component, ScenarioDto scenarioDto, Map<String, Object> presetValues
    ) {
        val orderId = Optional.ofNullable(scenarioDto.getOrderId())
                .orElse(userPersonalData.generateUniqueUserValue());

        return TimeSlotDoctorInput.builder()
                .orderId(orderId)
                .eserviceId(getStringValueWithNull(presetValues, ESERVICE_ID))
                .serviceId(getStringValueWithNull(presetValues, SERVICE_ID_ATTR))
                .serviceCode(getStringValueWithNull(presetValues, SERVICE_CODE_ATTR))
                .department(getDepartment(component, scenarioDto))
                .timeSlotRequestAttrs(createTimeSlotRequestAttrs(presetValues))
                .bookingRequestAttrs(createBookingRequestAttrs(presetValues))
                .organizationId(getStringValueWithNull(presetValues, ORGANIZATION_ID_ARG_KEY))
                .bookAttributes(getStringValueWithNull(presetValues, BOOK_ATTRS_ATTR))
                .userSelectedRegion(getStringValueWithNull(presetValues,USER_SELECTED_REGION_ATTR))
                .build();
    }

    private String getDepartment(FieldComponent component, ScenarioDto scenarioDto) {
        return Optional.ofNullable(component.getAttrs().get(TIME_SLOT_ATTR))
                .map(attr -> (Map<String, Object>) attr)
                .map(attr -> (Map<String, String>) attr.get(DEPARTMENT_ATTR))
                .map(attr -> parseAttrValuesHelper.getAttributeValue(attr, scenarioDto))
                .orElse(null);
    }

    private List<TimeSlotRequestAttr> createTimeSlotRequestAttrs(Map<String, Object> presetValues) {
        LocalDate currentDate = LocalDate.now();
        String defaultDayOffset = "14";
        String currentDayOffset = getStringValueOrDefault(presetValues, END_DATE_ATTR, defaultDayOffset);

        return List.of(
                createTimeSlotRequestAttr(START_DATE_ATTR, DATE_TIME_FORMATTER.format(currentDate)),
                createTimeSlotRequestAttr(TS_START_TIME_ATTR, getStringValueWithEmpty(presetValues, TS_START_TIME_ATTR)),
                createTimeSlotRequestAttr(END_DATE_ATTR, DATE_TIME_FORMATTER.format(currentDate.plusDays(Long.parseLong(currentDayOffset)))),
                createTimeSlotRequestAttr(TS_END_TIME_ATTR, getStringValueWithEmpty(presetValues, TS_END_TIME_ATTR)),
                createTimeSlotRequestAttr(SESSION_ID, getStringValueWithEmpty(presetValues, SESSION_ID)),
                createTimeSlotRequestAttr(TS_SERVICE_ID_ATTR, getStringValueWithEmpty(presetValues, TS_SERVICE_ID_ATTR)),
                createTimeSlotRequestAttr(SERVICE_SPEC_ID_ATTR, getStringValueWithEmpty(presetValues, SERVICE_SPEC_ID_ATTR)),
                createTimeSlotRequestAttr(MO_ID, getStringValueWithEmpty(presetValues, MO_ID))
        );
    }

    private List<TimeSlotRequestAttr> createBookingRequestAttrs(Map<String, Object> presetValues) {
        return List.of(
                createTimeSlotRequestAttr(DOCTOR_ATTR, getStringValueWithEmpty(presetValues, DOCTOR_ATTR)),
                createTimeSlotRequestAttr(ANOTHER_PERSON_ATTR, getStringValueWithEmpty(presetValues, ANOTHER_PERSON_ATTR)),
                createTimeSlotRequestAttr(GENDER_PERSON_ATTR, getStringValueWithEmpty(presetValues, GENDER_PERSON_ATTR)),
                createTimeSlotRequestAttr(AGE_PERSON_ATTR, getStringValueWithEmpty(presetValues, AGE_PERSON_ATTR)),
                createTimeSlotRequestAttr(PATIENT_NAME_ATTR, getStringValueWithEmpty(presetValues, PATIENT_NAME_ATTR))
        );
    }

    private TimeSlotRequestAttr createTimeSlotRequestAttr(String name, String value) {
        return TimeSlotRequestAttr.builder()
                .name(name)
                .value(value)
                .build();
    }

    private String getStringValueWithNull(Map<String, Object> presetValues, String key) {
        return (String) presetValues.getOrDefault(key, null);
    }

    private String getStringValueWithEmpty(Map<String, Object> presetValues, String key) {
        return (String) presetValues.getOrDefault(key, "");
    }

    private String getStringValueOrDefault(Map<String, Object> presetValues, String key, String defaultValue) {
        return (String) presetValues.getOrDefault(key, defaultValue);
    }
}
