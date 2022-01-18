package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService;
import ru.gosuslugi.pgu.fs.utils.AttributeValueTypes;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitialValueFromImpl implements InitialValueFromService {

    private static final String PRESET_STRUCTURE_TYPE = "type";
    private static final String PRESET_STRUCTURE_VALUE = "value";

    private final ParseAttrValuesHelper parseAttrValuesHelper;
    private final CalculatedAttributesHelper calculatedAttributesHelper;

    @Override
    public String getValue(FieldComponent component, ScenarioDto scenarioDto) {
        Optional<Map<String, Object>> presetStructureMap = Optional.ofNullable(component.getAttrs())
            .map(attrs -> attrs.get(PRESET_ATTRIBUTE_NAME))
            .map(obj -> obj instanceof Map ? (Map<String, Object>) obj : null);

        if (presetStructureMap.isPresent()) {
            return getValue(component, scenarioDto, presetStructureMap.get());
        }
        return "";
    }

    /**
     * Извлекает адрес взависимости от типа атрибута
     * @param component компонент извлечения
     * @param scenarioDto - сценарий услуги
     * @param presetStructureMap структура атрубутов
     * @return строка, предположительно. Не точно, так как REF или CALC могут указывать на любое значение
     */
    @Override
    public String getValue(FieldComponent component, ScenarioDto scenarioDto, Map<String, Object> presetStructureMap) {
        Optional<String> type = Optional.ofNullable(presetStructureMap.get(PRESET_STRUCTURE_TYPE))
            .map(Object::toString)
            .map(String::toUpperCase);
        if (type.isEmpty()) {
            return "";
        }

        // CALC
        if (AttributeValueTypes.CALC.toString().equals(type.get())) {
            return calculatedAttributesHelper.getCalculatedValue(presetStructureMap, component, scenarioDto, PRESET_STRUCTURE_VALUE);
        }

        // REF
        if (AttributeValueTypes.REF.toString().equals(type.get())) {
            return parseAttrValuesHelper.getAttributeValue(FieldComponentUtil.toStringMap(presetStructureMap), scenarioDto);
        }

        // PROTECTED
        if (AttributeValueTypes.PROTECTED.toString().equals(type.get())) {
            Object attrName = presetStructureMap.get(PRESET_STRUCTURE_VALUE);
            if (attrName != null) {
                return parseAttrValuesHelper.getProtectedField(attrName.toString());
            }
        }
        return "";
    }
}
