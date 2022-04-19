package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface AnswerValidationService {
    ConcurrentHashMap<String, String> validateAnswers(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor);
    List<List<Map<String, String>>> validateItemsUniqueness(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor);
}
