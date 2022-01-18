package ru.gosuslugi.pgu.player.service.unsupported;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService;

import java.util.Map;

@Slf4j
@Service
public class InitialValueServiceStub implements InitialValueFromService {
    @Override
    public String getValue(FieldComponent component, ScenarioDto scenarioDto) {
        return component.getValue();
    }

    @Override
    public String getValue(FieldComponent component, ScenarioDto scenarioDto, Map<String, Object> presetStructureMap) {
        return component.getValue();
    }
}
