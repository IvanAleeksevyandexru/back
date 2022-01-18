package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioResponse;

public interface ScenarioInitializerService {

    ScenarioResponse getInitScreen(InitServiceDto initServiceDto, String serviceId);

    ScenarioResponse getInitScreenWithExistedOrderId(String serviceId, Order order, InitServiceDto initServiceDto);

    ScenarioResponse getInvitedScenario(InitServiceDto initServiceDto, String serviceId);

    ScenarioResponse getExistingScenario(InitServiceDto initServiceDto, String serviceId);
}
