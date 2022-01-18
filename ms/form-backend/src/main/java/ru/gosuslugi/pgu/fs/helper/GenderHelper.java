package ru.gosuslugi.pgu.fs.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.util.Strings;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.descriptor.ComponentField;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;

import java.util.List;

public interface GenderHelper {

    String MAN_GENDER = "M";

    /**
     * Метод преобразует гендерный компонент в гендеронезависимый компонент исходя из пола полученного из ЕСИА по токену из scenarioDto
     *
     * @param component преобразуемый компонент
     * @param scenarioDto сценарий
     * @return измененный компонент
     */
    default void processGender(FieldComponent component, ScenarioDto scenarioDto) {
    }

    default String getHeader(String gender, ScreenDescriptor screenDescriptor) {
        return screenDescriptor.getHeader() != null ? getGenderValue(gender, screenDescriptor.getHeader()) : null;
    }

    default String getLabel(String gender, FieldComponent component) {
        return component.getLabel() != null ? getGenderValue(gender, component.getLabel()) : null;
    }

    default String getLabel(String gender, ComponentField component) {
        return component.getLabel() != null ? getGenderValue(gender, component.getLabel()) : null;
    }

    default String getValue(String gender, ComponentField component) {
        return component.getValue() != null ? getGenderValue(gender, component.getValue()) : null;
    }

    private String getGenderValue(String gender, String value) {
        try {
            List<String> values = JsonProcessingUtil.fromJson(value, new TypeReference<>() {
            });
            if (Strings.isNotBlank(gender) && values != null && values.size() == 2) {
                return gender.equals(MAN_GENDER) ? values.get(0) : values.get(1);
            }
        } catch (JsonParsingException ex) {
            // Нормальная ситуация, значение поля не зависит от Гендера
        }
        return value;
    }
}
