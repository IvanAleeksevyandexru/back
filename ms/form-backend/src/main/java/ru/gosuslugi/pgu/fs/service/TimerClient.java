package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.PguTimer;
import ru.gosuslugi.pgu.fs.service.timer.model.TimerRequestParameters;

/**
 * Клиент к сервису таймеров
 */
public interface TimerClient {
    /**
     * Получает данные о таймере по заданным параметрам
     * @param parameters параметры запроса к сервису
     * @param authToken токен авторизации
     * @return данные о таймере
     */
    PguTimer getTimer(TimerRequestParameters parameters, String authToken);
}
