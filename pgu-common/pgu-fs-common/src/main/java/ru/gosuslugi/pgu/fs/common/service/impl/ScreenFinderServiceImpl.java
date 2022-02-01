package ru.gosuslugi.pgu.fs.common.service.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.fs.common.exception.MultipleScreensFoundException;
import ru.gosuslugi.pgu.fs.common.exception.NoScreensFoundException;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ScreenRule;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService;
import ru.gosuslugi.pgu.fs.common.service.ScreenFinderService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScreenFinderServiceImpl implements ScreenFinderService {

    private final JsonProcessingService jsonProcessingService;
    private final RuleConditionService ruleConditionService;

    /**
     * {@inheritDoc}
     * @throws MultipleScreensFoundException найдено более одного экрана для перехода
     * @throws NoScreensFoundException не найдено экранов для перехода
     */
    @Override
    public ScreenDescriptor findScreenDescriptorByRules(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor, List<ScreenRule> screenRuleListSupplied){
        return findScreenDescriptorByRulesOrEmpty(scenarioDto, serviceDescriptor, screenRuleListSupplied)
                .orElseThrow(() -> new NoScreensFoundException("На экране "
                        + Optional.ofNullable(scenarioDto.getDisplay()).orElse(new DisplayRequest()).getId()
                        + " не найдено экранов для перехода"));
    }

    public Optional<ScreenDescriptor> findScreenDescriptorByRulesOrEmpty(
            ScenarioDto scenarioDto,
            ServiceDescriptor serviceDescriptor,
            List<ScreenRule> rules
    ) {
        // удаляем из ответов те, что пришли от пользователя (currentValue)
        var previousAnswers = new HashMap<>(scenarioDto.getApplicantAnswers());
        scenarioDto.getCurrentValue().keySet().forEach(previousAnswers::remove);

        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(previousAnswers));
        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue()));
        DocumentContext serviceInfoContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getServiceInfo()));
        DocumentContext cycledCurrentItemContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getCycledApplicantAnswerContext()));

        List<ScreenRule> screenRuleList = CollectionUtils.isEmpty(rules)
                ? serviceDescriptor.getScreenRules().get(scenarioDto.getDisplay().getId())
                : rules;
        List<ScreenRule> validScreenRules = screenRuleList
                .stream()
                .filter(id -> ruleConditionService.isRuleApplyToAnswers(
                        id.getConditions(),
                        Arrays.asList(scenarioDto.getCurrentValue(), scenarioDto.getApplicantAnswers()),
                        Arrays.asList(currentValueContext, applicantAnswersContext, serviceInfoContext, cycledCurrentItemContext),
                        scenarioDto))
                .collect(Collectors.toList());

        if(validScreenRules.size() > 1){
            throw new MultipleScreensFoundException("На экране " + Optional.ofNullable(scenarioDto.getDisplay()).orElse(new DisplayRequest()).getId() +" найдено более 1 экрана для перехода " + validScreenRules.stream().map(ScreenRule::getNextDisplay).collect(Collectors.joining(",")));
        }
        if(validScreenRules.size() == 0) {
            return Optional.empty();
        }
        String nextScreenId = validScreenRules.get(0).getNextDisplay();
        Optional<ScreenDescriptor> foundScreen = serviceDescriptor.getScreenDescriptorById(nextScreenId);

        if (foundScreen.isPresent()) {
            if (log.isWarnEnabled()) log.warn("Отправляю пользователю экран = {}", foundScreen.get().getId());
            return foundScreen;
        }
        throw new NoScreensFoundException("Не найден экран с идентификатором " + nextScreenId);
    }

}
