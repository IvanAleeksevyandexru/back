package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;

public interface ComputeAnswerService {

    void computeValues(ScreenDescriptor component, ScenarioDto scenarioDto);

}
