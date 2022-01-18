package ru.gosuslugi.pgu.fs.service.process;

import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;

/**
 * Процесс перехода между экранами
 */
public interface ScreenProcess<T> extends Process<T, ScenarioResponse> {

    T of(String serviceId, ScenarioRequest scenarioRequest);

    boolean needReInitScreen();

    void reInitScenario();

    void saveDraft();
}
