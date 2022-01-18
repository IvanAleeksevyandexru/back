package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;

public interface DisplayReferenceService extends ReferenceService {

    /**
     * Формирует поля экрана для вывода в интерфейсе
     * @param displayRequest    Экран
     * @param scenarioDto       Сценарий
     */
    void processDisplayRefs(DisplayRequest displayRequest, ScenarioDto scenarioDto);
}
