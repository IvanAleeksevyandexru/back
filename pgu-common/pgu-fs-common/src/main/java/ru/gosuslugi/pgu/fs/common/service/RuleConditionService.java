package ru.gosuslugi.pgu.fs.common.service;

import com.jayway.jsonpath.DocumentContext;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.RuleCondition;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RuleConditionService {
    /**
     * Применяется ли правило к текущему экрану
     * @param conditions бизнес-правило
     * @param answersMaps ответы пользователя
     * @param documentContexts контексты ответов
     * @return применяется ли правило к текущему экрану
     */
    boolean isRuleApplyToAnswers(
        Set<RuleCondition> conditions,
        List<Map<String, ApplicantAnswer>> answersMaps,
        List<DocumentContext> documentContexts,
        ScenarioDto scenarioDto
    );
}
