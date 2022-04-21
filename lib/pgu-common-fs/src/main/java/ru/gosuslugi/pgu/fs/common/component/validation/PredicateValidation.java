package ru.gosuslugi.pgu.fs.common.component.validation;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ConditionFieldType;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.condition.ConditionCheckerHelper;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PredicateValidation implements ValidationRule {

    private static final String PREDICATE_TYPE = "Predicate";
    private static final String FIELD_ATTR = "field";
    private static final String FIELD_TYPE_ATTR = "fieldType";
    private static final String PREDICATE_ATTR = "predicate";
    private static final String AGRS_ATTR = "args";
    private static final String ERROR_MSG_ATTR = "errorMsg";

    private final JsonProcessingService jsonProcessingService;
    private final ConditionCheckerHelper conditionCheckerHelper;

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        List<Map<Object, Object>> predicateValidations = getPredicateValidationList(fieldComponent);
        if (CollectionUtils.isEmpty(predicateValidations)) {
            return null;
        }
        String entryValue = AnswerUtil.getValue(entry);
        List<DocumentContext> contexts = getContexts(entryValue, scenarioDto.getCurrentValue());
        List<String> errors = predicateValidations.stream()
                .map(predicateMap -> {
                    boolean result = testPredicate(ConditionFieldType.valueOf((String) predicateMap.get(FIELD_TYPE_ATTR)),
                            (String) predicateMap.get(PREDICATE_ATTR),
                            (List<String>) predicateMap.get(AGRS_ATTR),
                            (String) predicateMap.get(FIELD_ATTR),
                            contexts);
                    return result ? null : (String) predicateMap.get(ERROR_MSG_ATTR);
                })
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (!errors.isEmpty()) {
            return Map.entry(fieldComponent.getId(), String.join(", ", errors));
        }
        return null;
    }

    private List<DocumentContext> getContexts( String entryValue, Map<String, ApplicantAnswer> currentValue) {
        DocumentContext answerContext = JsonPath.parse(jsonProcessingService.toJson(entryValue));
        return List.of(answerContext);
    }

    private boolean testPredicate(ConditionFieldType fieldType, String predicate, List<String> args, String field, List<DocumentContext> documentContexts) {
        Object object = conditionCheckerHelper.getFirstFromContexts(field, documentContexts, fieldType);
        return conditionCheckerHelper.checkPredicate(object, predicate, fieldType, args);
    }

    private static List<Map<Object, Object>> getPredicateValidationList(FieldComponent fieldComponent) {
        return FieldComponentUtil.getList(fieldComponent, FieldComponentUtil.VALIDATION_ARRAY_KEY, true)
                .stream()
                .filter(
                        validationRule ->
                                PREDICATE_TYPE.equalsIgnoreCase((String) validationRule.get(ValidationUtil.VALIDATION_TYPE))
                                        && StringUtils.hasText((String) validationRule.get(PREDICATE_ATTR))
                )
                .collect(Collectors.toList());
    }

}
