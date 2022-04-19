package ru.gosuslugi.pgu.fs.common.service.condition;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.RuleCondition;
import ru.gosuslugi.pgu.dto.descriptor.types.ConditionFieldType;
import ru.gosuslugi.pgu.dto.descriptor.types.PredicateArgument;
import ru.gosuslugi.pgu.dto.descriptor.types.PredicateArgumentType;
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService;
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry;
import ru.gosuslugi.pgu.fs.common.variable.VariableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConditionCheckerHelper {

    private final StringPredicateFactory stringPredicateFactory;
    private final IntegerPredicateFactory integerPredicateFactory;
    private final BooleanPredicateFactory booleanPredicateFactory;
    private final DatePredicateFactory datePredicateFactory;
    private final ArrayPredicateFactory arrayPredicateFactory;
    private final ApplicantAnswerPredicateFactory applicantAnswerPredicateFactory;

    private final ProtectedFieldService protectedFieldService;
    private final VariableRegistry variableRegistry;

    private static final String IS_NULL_PREDICATE = "isNull";
    private static final String NON_NULL_PREDICATE = "nonNull";

    /**
     * Check if request context satisfies provided condition
     * @param ruleCondition {@link RuleCondition} to use
     * @param documentContexts  {@link List} of {@link DocumentContext} for map of {@link ru.gosuslugi.pgu.dto.ApplicantAnswer} to check condition
     * @return true if satisfies condition false otherwise
     */
    public boolean check(RuleCondition ruleCondition, List<DocumentContext> documentContexts, ScenarioDto scenarioDto) {
        List<String> predicateArgs = getPredicateArgs(ruleCondition.getArgs(), documentContexts, scenarioDto);

        boolean isConditionWithProtectedField = ruleCondition.isConditionWithProtectedField();
        boolean isConditionWithVariable = ruleCondition.isConditionWithVariable();
        Object object = null;
        if (isConditionWithProtectedField) {
            object = protectedFieldService.getValue(ruleCondition.getProtectedField());
        }
        if (isConditionWithVariable) {
            object = variableRegistry.getVariable(VariableType.valueOf(ruleCondition.getVariable())).getValue(scenarioDto);
        }
        if (Objects.isNull(object)) {
            if (IS_NULL_PREDICATE.equals(ruleCondition.getPredicate()) || NON_NULL_PREDICATE.equals(ruleCondition.getPredicate())) {
                object = getFirstFromContexts(ruleCondition.getField(), documentContexts, Object.class);
            } else {
                object = getFirstFromContexts(ruleCondition.getField(), documentContexts, ruleCondition.getFieldType());
            }
        }
        boolean checkResult = checkPredicate(object, ruleCondition.getPredicate(), ruleCondition.getFieldType(), predicateArgs);

        if (log.isDebugEnabled()) log.debug("Check condition for field: {}, with type: {}, located: {}, using predicate: {}, with args: {}, produced: {}", object, ruleCondition.getFieldType(), ruleCondition.getField(), ruleCondition.getPredicate(), predicateArgs, checkResult);
        return checkResult;
    }


    public boolean checkPredicate(Object object, String predicate, ConditionFieldType fieldType, List<String> predicateArgs) {

        if (IS_NULL_PREDICATE.equals(predicate)) {
            return Objects.isNull(object);
        }

        if (NON_NULL_PREDICATE.equals(predicate)) {
            return Objects.nonNull(object);
        }

        if (Objects.isNull(object) && !fieldType.isAppliesToNullValue()) {
            return false;
        }

        /*
          1. getFirstFromContexts с String.class для сложных объектов может не сработать и вернуть null см. EPGUCORE-49712
          2. Date на текущий момент работает со строками, поэтому не стоит проверять на этот класс
          3. ConditionFieldType.ApplicantAnswer сам разбирается с нулевыми значениями и сложными объектами
         */
        if (
                ConditionFieldType.ApplicantAnswer != fieldType
                        && !List.of(String.class, Integer.class, Boolean.class, JSONArray.class).contains(object.getClass())
        ) {
            return false;
        }

        switch (fieldType) {
            case String:
                return stringPredicateFactory.getPredicate(predicate, predicateArgs).test((String) object);
            case Integer:
                return integerPredicateFactory.getPredicate(predicate, predicateArgs).test((Integer) object);
            case Boolean:
                return booleanPredicateFactory.getPredicate(predicate, predicateArgs).test((Boolean) object);
            case Date:
                return datePredicateFactory.getPredicate(predicate, predicateArgs).test((String) object);
            case Array:
                return arrayPredicateFactory.getPredicate(predicate, predicateArgs).test((List<Object>) object);
            case ApplicantAnswer:
                return applicantAnswerPredicateFactory.getPredicate(predicate, predicateArgs).test((ApplicantAnswer) object);
            default:
                throw new ValidationException("Condition for field with type " + fieldType + " not supported");
        }
    }

    public Object getFirstFromContexts(String field, List<DocumentContext> documentContexts, ConditionFieldType fieldType) {
        if (Objects.nonNull(fieldType)) {
            switch (fieldType) {
                case String:
                    return getFirstFromContexts(field, documentContexts, String.class);
                case Integer:
                    return getFirstFromContexts(field, documentContexts, Integer.class);
                case Boolean:
                    return getFirstFromContexts(field, documentContexts, Boolean.class);
                case Date:
                    return getFirstFromContexts(field, documentContexts, String.class);
                case Array:
                    return getFirstFromContexts(field, documentContexts, List.class);
                case ApplicantAnswer:
                    return getFirstFromContexts(field, documentContexts, ApplicantAnswer.class);
                default:
                    throw new ValidationException("Condition for field with type " + fieldType + " not supported");
            }
        }
        return getFirstFromContexts(field, documentContexts, Object.class);
    }

    public <T> T getFirstFromContexts(String field, List<DocumentContext> documentContexts, Class<T> aClass) {
        return documentContexts.stream()
                .map(
                        documentContext -> {
                            try {
                                return documentContext.read("$." + field, aClass);
                            } catch (PathNotFoundException e) {
                                return null;
                            }
                        }
                )
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Convert list of {@link PredicateArgument} to list of strings
     * Fetch data from previous answers if {@link PredicateArgumentType} is RequestData
     * @param predicateArguments list of {@link PredicateArgument} to convert
     * @param previousAnswers {@link DocumentContext} for map of previous answers in case complex conditions
     * @return List of predicate arguments, converted to strings
     */
    private List<String> getPredicateArgs(List<PredicateArgument> predicateArguments,
                                          List<DocumentContext> previousAnswers,
                                          ScenarioDto scenarioDto) {
        List<String> stringArgs = new ArrayList<>();
        if (predicateArguments != null) {
            for (PredicateArgument predicateArg: predicateArguments) {
                switch (predicateArg.getType()) {
                    case UserConst:
                        stringArgs.add(predicateArg.getValue());
                        break;
                    case RequestData:
                        stringArgs.add(getFirstFromContexts(predicateArg.getValue(), previousAnswers, String.class));
                        break;
                    case ProtectedField:
                        stringArgs.add(String.valueOf(protectedFieldService.getValue(predicateArg.getValue())));
                        break;
                    case Variable:
                        stringArgs.add(variableRegistry.getVariable(VariableType.valueOf(predicateArg.getValue())).getValue(scenarioDto));
                        break;
                    default:
                        throw new ValidationException("Predicate argument with type " + predicateArg.getType() + "not supported");
                }
            }
        }
        return stringArgs;
    }
}
