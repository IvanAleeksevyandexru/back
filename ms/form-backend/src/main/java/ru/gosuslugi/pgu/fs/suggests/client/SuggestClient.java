package ru.gosuslugi.pgu.fs.suggests.client;

import ru.gosuslugi.pgu.dto.ScenarioDto;

public interface SuggestClient {

    void send(Long userId, ScenarioDto scenarioDto);
}
