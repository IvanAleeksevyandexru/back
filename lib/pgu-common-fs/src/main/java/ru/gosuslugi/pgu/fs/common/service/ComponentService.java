package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ComponentService {

    Optional<FieldComponent> getFieldComponent(String serviceId, Map.Entry<String, ApplicantAnswer> answerEntry, ServiceDescriptor currentDescriptor);

    List<FieldComponent> getScreenFields(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor);

    List<FieldComponent> getLogicFields(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor);

    FieldComponent processField(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor);

    void fillCycledField(FieldComponent component, ScenarioDto scenarioDto);

    void preloadComponents(String screenId, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor);

    void executeComponentAfterOrderCreated(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor);
}
