package ru.gosuslugi.pgu.fs.common.variable;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.exception.ServiceIdException;
import ru.gosuslugi.pgu.dto.descriptor.value.service.ServiceId;
import ru.gosuslugi.pgu.dto.descriptor.value.service.ServiceIds;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Глобальная переменная для вычисляемых данных об услуге
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceIdVariable extends AbstractVariable {
    private final MainDescriptorService mainDescriptorService;
    private final JsonProcessingService jsonProcessingService;
    private final RuleConditionService ruleConditionService;

    /**
     * При каждом обращении вычисляется serviceId на основании ответов пользователя.
     * @param scenarioDto
     * @return
     */
    @Override
    public String getValue(ScenarioDto scenarioDto) {
        return getRecalculatedServiceId(scenarioDto);
    }

    @Override
    public VariableType getType() {
        return VariableType.serviceId;
    }

    private String getRecalculatedServiceId(ScenarioDto scenarioDto) {
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(scenarioDto.getServiceDescriptorId());
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()));
        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue()));
        DocumentContext serviceInfoContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getServiceInfo()));
        DocumentContext cycledCurrentItemContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getCycledApplicantAnswerContext()));

        List<ServiceId> ids = Optional.ofNullable(serviceDescriptor.getServiceIds())
                .map(ServiceIds::getIds)
                .orElse(Collections.emptyList());
        List<ServiceId> validIds = ids.stream()
                .filter(id -> ruleConditionService.isRuleApplyToAnswers(
                        id.getConditions(),
                        Arrays.asList(scenarioDto.getCurrentValue(), scenarioDto.getApplicantAnswers()),
                        Arrays.asList(currentValueContext, applicantAnswersContext, serviceInfoContext, cycledCurrentItemContext),
                        scenarioDto))
                .collect(Collectors.toList());

        if (validIds.size() > 1) {
            throw new ServiceIdException("Найдено более 1 кода услуги " + validIds.stream().map(ServiceId::getId).collect(Collectors.joining(",")));
        }
        if (validIds.size() == 0) {
            return getDefaultServiceId(scenarioDto);
        }

        return validIds.get(0).getId();
    }

    private String getDefaultServiceId(ScenarioDto scenarioDto) {
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(scenarioDto.getServiceDescriptorId());
        return Optional.ofNullable(serviceDescriptor.getServiceIds())
                .map(ServiceIds::getDefaultServiceId)
                .orElse(scenarioDto.getServiceCode());
    }
}
