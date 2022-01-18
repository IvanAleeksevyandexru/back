package ru.gosuslugi.pgu.fs.common.service.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.descriptor.RuleCondition;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.ScreenDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.condition.ConditionCheckerHelper;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ScreenDescriptorServiceImpl implements ScreenDescriptorService {

    private final JsonProcessingService jsonProcessingService;

    private final ConditionCheckerHelper conditionCheckerHelper;

    public boolean isSkippedScreen(ScenarioRequest req, ScreenDescriptor screenDescriptor) {
        Set<RuleCondition> skipConditions = screenDescriptor.getSkipConditions();
        if (skipConditions == null || skipConditions.isEmpty()) {
            return false;
        }
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(req.getScenarioDto().getApplicantAnswers()));
        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(req.getScenarioDto().getCurrentValue()));
        DocumentContext serviceInfoContext = JsonPath.parse(jsonProcessingService.toJson(req.getScenarioDto().getServiceInfo()));
        return skipConditions
                .stream()
                .allMatch(condition -> conditionCheckerHelper.check(
                        condition,
                        Arrays.asList(currentValueContext, applicantAnswersContext, serviceInfoContext),
                        req.getScenarioDto()));
    }
}
