package ru.gosuslugi.pgu.fs.common.component.input;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.*;
import ru.gosuslugi.pgu.fs.common.service.EvaluationExpressionService;
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService;
import ru.gosuslugi.pgu.fs.common.service.condition.ConditionCheckerHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class StringInputComponent extends EsiaDataCycledComponent<String> {

    private final InitialValueFromService initialValueFromService;
    private final ConditionCheckerHelper conditionCheckerHelper;
    private final EvaluationExpressionService evaluationExpressionService;

    @Override
    public ComponentType getType() {
        return ComponentType.StringInput;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        String presetValue = initialValueFromService.getValue(component, scenarioDto);
        if (!isBlank(presetValue)) {
            return ComponentResponse.of(presetValue);
        }
        return ComponentResponse.of(component.getValue());
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new CheckBoxEnabledValidation(new RequiredNotBlankValidation("Значение не задано")),
                new CheckBoxEnabledValidation(new EmptyOr(new RegExpValidation())),
                new CheckBoxEnabledValidation(new EmptyOr(new CalculationPredicate(jsonProcessingService, conditionCheckerHelper, evaluationExpressionService)))
        );
    }

    @Override
    public ComponentResponse<String> getCycledInitialValue(FieldComponent component, Map<String, Object> externalData) {
        Set<String> fieldNames = BasicComponentUtil.getPreSetFields(component);
        return ComponentResponse.of(
                fieldNames.stream()
                        .filter(externalData::containsKey)
                        .map(externalData::get)
                        .map(Object::toString)
                        .findAny()
                        .orElse("")
        );
    }
}
