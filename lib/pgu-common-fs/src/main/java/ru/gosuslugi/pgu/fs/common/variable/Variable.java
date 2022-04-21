package ru.gosuslugi.pgu.fs.common.variable;

import ru.gosuslugi.pgu.dto.ScenarioDto;

public interface Variable {
    String getValue(ScenarioDto scenarioDto);
    VariableType getType();
}
