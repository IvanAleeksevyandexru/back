package ru.gosuslugi.pgu.fs.component.logic;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.CALCULATIONS_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REFS_ATTR;

@Component
@RequiredArgsConstructor
public class ValueCalculatorComponent extends AbstractComponent<String> {
    private static final String ARGUMENTS_ATTR = "arguments";
    private static final String PLACEHOLDER_FORMAT = "${%s}";

    private final CalculatedAttributesHelper calculatedAttributesHelper;
    private final ComponentReferenceService componentReferenceService;

    @Override
    public ComponentType getType() {
        return ComponentType.ValueCalculator;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> value = new HashMap<>();

        if (Objects.nonNull(component.getAttrs())) {
            val calc = calculatedAttributesHelper.getAllCalculatedValues(
                CALCULATIONS_ATTR, component, scenarioDto
            );
            if (!CollectionUtils.isEmpty(calc)) {
                value.put(CALCULATIONS_ATTR, calc);
            }

            val refs = (Map<String, String>) component.getAttrs().get(REFS_ATTR);
            if (!CollectionUtils.isEmpty(refs)) {
                value.put(REFS_ATTR, calculateRefs(component, scenarioDto, refs));
            }
        }
        val args = component.getArguments();
        if (!CollectionUtils.isEmpty(args)) {
            value.put(ARGUMENTS_ATTR, args);
        }

        if (component.isCycled()) {
            var cycledApplicantAnswers = scenarioDto.getCycledApplicantAnswers();
            var currentAnswerId = cycledApplicantAnswers.getCurrentAnswerId();
            if (currentAnswerId == null) {
                return ComponentResponse.empty();
            }

            cycledApplicantAnswers.addAnswerIfAbsent(currentAnswerId, new CycledApplicantAnswer(currentAnswerId));
            cycledApplicantAnswers.getAnswer(currentAnswerId).ifPresent(answer -> {
                answer.addItemIfAbsent(component.getId(), new CycledApplicantAnswerItem(component.getId()));

                answer.getItem(component.getId()).ifPresent(item -> {
                    Map<String, ApplicantAnswer> itemAnswers = item.getItemAnswers();
                    if (value.containsKey(ARGUMENTS_ATTR)) {
                        itemAnswers.put(ARGUMENTS_ATTR, new ApplicantAnswer(true, jsonProcessingService.toJson(value.get(ARGUMENTS_ATTR))));
                    }
                    if (value.containsKey(CALCULATIONS_ATTR)) {
                        itemAnswers.put(CALCULATIONS_ATTR, new ApplicantAnswer(true, jsonProcessingService.toJson(value.get(CALCULATIONS_ATTR))));
                    }
                    if (value.containsKey(REFS_ATTR)) {
                        itemAnswers.put(REFS_ATTR, new ApplicantAnswer(true, jsonProcessingService.toJson(value.get(REFS_ATTR))));
                    }
                });
            });
        } else {
            scenarioDto.getApplicantAnswers().put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(value)));
        }

        return ComponentResponse.empty();
    }

    private Map<String, String> calculateRefs(FieldComponent component, ScenarioDto scenarioDto, Map<String, String> refs) {
        val context = componentReferenceService.buildPlaceholderContext(component, scenarioDto);
        val answers = scenarioDto.getApplicantAnswers();
        return refs.keySet().stream().collect(Collectors.toMap(
                Function.identity(),
                key -> componentReferenceService.getValueByContext(String.format(PLACEHOLDER_FORMAT, key), context, answers)
        ));
    }
}
