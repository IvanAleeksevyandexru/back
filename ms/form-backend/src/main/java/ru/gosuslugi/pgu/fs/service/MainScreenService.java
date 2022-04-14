package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.*;
import ru.gosuslugi.pgu.dto.order.OrderInfoDto;
import ru.gosuslugi.pgu.dto.order.OrderListInfoDto;
import ru.gosuslugi.pgu.fs.common.service.ScreenService;

public interface MainScreenService extends ScreenService {

    /**
     * Check if draft already exists and restores it for an Applicant (none-invited person).
     * Sets invite flag according to conditions.
     * (In this case invited flag is false by default)
     * @param initServiceDto init parameters
     * @param serviceId service ID
     * @return - ScenarioResponse in case if exists or empty ScenarioResponse otherwise
     */
    ScenarioResponse getExistingScenario(InitServiceDto initServiceDto, String serviceId);

    OrderListInfoDto getOrderInfo(InitServiceDto initServiceDto, String serviceId);

    OrderListInfoDto getOrderInfoById(InitServiceDto initServiceDto, String serviceId);

    /**
     * Установка статуса id
     * @param scenarioDto scenarioDto
     */
    void setStatusId(ScenarioDto scenarioDto);

    ScenarioResponse prepareScenarioFromExternal(ScenarioFromExternal scenarioFromExternal);
}
