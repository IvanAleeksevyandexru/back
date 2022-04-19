package ru.gosuslugi.pgu.fs.common.variable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class FormIdVariable extends AbstractVariable {

    @Override
    public String getValue(ScenarioDto scenarioDto) {
        if (scenarioDto.getServiceInfo() != null) {
            return scenarioDto.getServiceInfo().getFormId();
        }
        return null;
    }

    @Override
    public VariableType getType() {
        return VariableType.formId;
    }
}
