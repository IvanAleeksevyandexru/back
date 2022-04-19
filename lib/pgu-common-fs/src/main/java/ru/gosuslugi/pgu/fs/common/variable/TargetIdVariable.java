package ru.gosuslugi.pgu.fs.common.variable;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.fs.common.exception.TargetIdException;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.value.target.TargetId;
import ru.gosuslugi.pgu.dto.descriptor.value.target.TargetIds;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetIdVariable extends AbstractVariable{

    private final MainDescriptorService mainDescriptorService;
    private final JsonProcessingService jsonProcessingService;
    private final RuleConditionService ruleConditionService;

    @Override
    public String getValue(ScenarioDto scenarioDto) {
        return calculateTargetId(scenarioDto);
    }

    private String calculateTargetId(ScenarioDto scenarioDto) {
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(scenarioDto.getServiceDescriptorId());
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()));
        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue()));
        DocumentContext serviceInfoContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getServiceInfo()));
        DocumentContext cycledCurrentItemContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getCycledApplicantAnswerContext()));

        List<TargetId> ids = Optional.ofNullable(serviceDescriptor.getTargetIds())
                .map(TargetIds::getIds)
                .orElse(Collections.emptyList());
        List<TargetId> validIds = ids.stream()
                .filter(id -> ruleConditionService.isRuleApplyToAnswers(
                        id.getConditions(),
                        Arrays.asList(scenarioDto.getCurrentValue(), scenarioDto.getApplicantAnswers()),
                        Arrays.asList(currentValueContext, applicantAnswersContext, serviceInfoContext, cycledCurrentItemContext),
                        scenarioDto))
                .collect(Collectors.toList());

        if (validIds.size() > 1) {
            throw new TargetIdException("Найдено более 1 цели услуги " + validIds.stream().map(TargetId::getId).collect(Collectors.joining(",")));
        }
        if (validIds.size() == 0) {
            return getDefaultServiceId(scenarioDto);
        }

        return validIds.get(0).getId();

    }

    private String getDefaultServiceId(ScenarioDto scenarioDto) {
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(scenarioDto.getServiceDescriptorId());
        return Optional.ofNullable(serviceDescriptor.getTargetIds())
                .map(TargetIds::getDefaultTargetId)
                .orElse(scenarioDto.getTargetCode());
    }

    @Override
    public VariableType getType() {
        return VariableType.targetId;
    }
}
