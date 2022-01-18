package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.fs.common.service.ScreenService;

public interface SubScreenService extends ScreenService {

    /**
     * Получение начального экрана услуги
     * @param serviceId         Идентификатор услуги
     * @param initServiceDto    Дополнительная информация для инициализации услуги
     * @param parentServiceId   Идентификатор родительской услуги
     * @return                  Сценарий
     */
    ScenarioResponse getInitScreen(String serviceId, InitServiceDto initServiceDto, String parentServiceId);
}
