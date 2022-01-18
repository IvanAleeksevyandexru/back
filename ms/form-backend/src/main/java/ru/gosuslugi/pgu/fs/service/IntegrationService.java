package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

public interface IntegrationService {

    /**
     * Осуществляем взаимодействие с внешними системами (сервис-процессинг, делириум и т.п.)
     * @param scenarioResponse response
     * @param serviceId service id
     * @param descriptor descriptor
     */
    void performIntegrationSteps(ScenarioResponse scenarioResponse, String serviceId, ServiceDescriptor descriptor);
}
