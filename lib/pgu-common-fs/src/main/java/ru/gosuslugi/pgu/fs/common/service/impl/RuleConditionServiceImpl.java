package ru.gosuslugi.pgu.fs.common.service.impl;

import com.jayway.jsonpath.DocumentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.RuleCondition;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService;
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService;
import ru.gosuslugi.pgu.fs.common.service.condition.ConditionCheckerHelper;
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry;
import ru.gosuslugi.pgu.fs.common.variable.VariableType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleConditionServiceImpl implements RuleConditionService {

    private final ConditionCheckerHelper conditionCheckerHelper;
    private final ProtectedFieldService protectedFieldService;
    private final VariableRegistry variableRegistry;

    @Override
    public boolean isRuleApplyToAnswers(
            Set<RuleCondition> conditions,
            List<Map<String, ApplicantAnswer>> answersMaps,
            List<DocumentContext> documentContexts,
            ScenarioDto scenarioDto
    ) {
        return conditions.stream().allMatch(
                condition -> {

                    // Predicate values
                    if(condition.hasPredicate()){
                        return conditionCheckerHelper.check(condition, documentContexts, scenarioDto);
                    }

                    // Protected values
                    if (condition.isConditionWithProtectedField()) {
                        if(Objects.nonNull(condition.getValue())) {
                            return condition.getValue().equals(protectedFieldService.getValue(condition.getProtectedField()));
                        }
                        return true;
                    }

                    if (condition.isConditionWithVariable()) {
                        if(Objects.nonNull(condition.getValue())) {
                            return condition.getValue().equals(variableRegistry.getVariable(VariableType.valueOf(condition.getVariable())).getValue(scenarioDto));
                        }
                        return true;
                    }

                    // Old-style values
                    for (Map<String, ApplicantAnswer> answers : answersMaps) {
                        if(checkValidInAnswer(condition, answers)) {
                            return true;
                        }
                    }

                    return false;
                }
        );
    }

    private boolean checkValidInAnswer(RuleCondition ruleCondition, Map<String, ApplicantAnswer> answers){
        if (
                answers.containsKey(ruleCondition.getField())
                        && Objects.equals(ruleCondition.getVisited(), answers.get(ruleCondition.getField()).getVisited())
        ) {
            if (Objects.nonNull(ruleCondition.getValue())) {
                return ruleCondition.getValue().equals(answers.get(ruleCondition.getField()).getValue());
            }
            return true;
        }
        return false;
    }
}
