package ru.gosuslugi.pgu.fs.descriptor;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;

public interface SubDescriptorService extends DescriptorService {

    void presetFieldsForCurrentStep(String serviceId, ScenarioDto scenarioDto);

}
