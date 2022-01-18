package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.common.core.exception.dto.ExternalErrorInfo;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.fs.common.service.ScreenService;

public interface ErrorScreenService extends ScreenService {

    /**
     * Получение первого экрана сценария обработки ошибки
     * @param errorCode код ошибки
     * @param externalErrorInfo информация об ошибке
     * @return объект для отображения первого экрана
     */
    ScenarioResponse getInitScreen(int errorCode, ExternalErrorInfo externalErrorInfo);
}
