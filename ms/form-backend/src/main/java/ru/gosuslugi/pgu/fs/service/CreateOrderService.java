package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

import java.util.Optional;

public interface CreateOrderService {

    Long tryToCreateOrderId(String serviceId, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor);

    Boolean saveValuesForOrder(ServiceDescriptor descriptor, ScenarioDto scenarioDto);

    Boolean checkForDuplicate(ScenarioDto scenarioDto, Optional<FieldComponent> fieldComponent, ServiceDescriptor serviceDescriptor);
}
