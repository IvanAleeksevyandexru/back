package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.ExternalOrderRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;

public interface AutofillOrderService {
    ScenarioDto processExternalOrderRequest(ExternalOrderRequest request);
}
