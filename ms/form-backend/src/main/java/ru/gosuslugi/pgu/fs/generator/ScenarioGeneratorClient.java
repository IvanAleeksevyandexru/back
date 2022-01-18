package ru.gosuslugi.pgu.fs.generator;

import ru.gosuslugi.pgu.dto.ScenarioGeneratorDto;

public interface ScenarioGeneratorClient {

    void generateScenario(ScenarioGeneratorDto dto);
    void generateAdditionalSteps(String serviceId);
}
