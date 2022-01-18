package ru.gosuslugi.pgu.player.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.ServiceInfoDto;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.AbstractScreenService;
import ru.gosuslugi.pgu.fs.common.service.ScenarioDtoService;

@Slf4j
@Service
@RequiredArgsConstructor
public class LightweightScreenService extends AbstractScreenService {

    private final MainDescriptorService mainDescriptorService;
    private final ScenarioDtoService scenarioDtoService;

    @Override
    protected DescriptorService getDescriptorService() {
        return mainDescriptorService;
    }

    public ScenarioResponse getInitScreen(String serviceId) {
        ScenarioDto scenarioDto = initDefaultScenarioByScreen(serviceId, new ServiceInfoDto());
        ScenarioResponse scenarioResponse = new ScenarioResponse();
        scenarioResponse.setScenarioDto(scenarioDto);
        scenarioDtoService.prepareScenarioDto(scenarioDto, getDescriptorService().getServiceDescriptor(serviceId), serviceId);

        return scenarioResponse;
    }

    @Override
    protected ScenarioResponse afterPrevScreen(ScenarioResponse scenarioResponse, String serviceId) {
        return scenarioResponse;
    }

    @Override
    protected ScenarioResponse afterNextScreen(ScenarioResponse scenarioResponse, String serviceId) {
        return scenarioResponse;
    }
}
