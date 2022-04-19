package ru.gosuslugi.pgu.fs.common.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ComputeItem;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.fs.common.service.ComputeAnswerService;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static ru.gosuslugi.pgu.dto.descriptor.ComputeItem.ComputeItemType.NSI;

@Service
@RequiredArgsConstructor
public class ComputeAnswerServiceImpl implements ComputeAnswerService {

    private Map<ComputeItem.ComputeItemType, BiFunction<ComputeItem, ScenarioDto, ComputeItem>> strategyMap = new EnumMap<>(ComputeItem.ComputeItemType.class);
    private final ComputeDictionaryItemService computeDictionaryItemService;

    @PostConstruct
    public void postConstruct() {
        strategyMap.put(NSI, computeDictionaryItemService::computeDictionaryItem);
    }

    @Override
    public void computeValues(ScreenDescriptor component, ScenarioDto scenarioDto) {
        if (Objects.isNull(component.getComputedAnswers())) return;

        component.getComputedAnswers().forEach((computedAnswerName,computeItem) -> {
            if (strategyMap.containsKey(computeItem.getType())) {
                ComputeItem result = strategyMap.get(computeItem.getType()).apply(computeItem, scenarioDto);
                scenarioDto.getApplicantAnswers().put(computedAnswerName, new ApplicantAnswer(true, result.getResult()));
            }
        });
    }

}