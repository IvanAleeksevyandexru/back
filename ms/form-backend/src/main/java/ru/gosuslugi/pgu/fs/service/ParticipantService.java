package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.esia.search.dto.PersonWithAge;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

public interface ParticipantService {
    void setParticipant(ScenarioDto scenarioDto, FieldComponent fieldComponent, PersonWithAge ps, Integer itemIndex);
    void deleteRedundantParticipants(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor);
    void addParticipant(ScenarioDto scenarioDto, FieldComponent fieldComponent, PersonWithAge ps, Integer itemIndex);
    void deleteRedundantParticipantsByComponentId(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor, String componentId);
}
