package ru.gosuslugi.pgu.fs.service.impl;

import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.service.ExternalService;
import ru.gosuslugi.pgu.fs.service.ExternalServiceOrchestrator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.nonNull;

@Service
public class ExternalServiceOrchestratorImpl implements ExternalServiceOrchestrator {

    public static final String EXTERNAL_INTEGRATION_ARGUMENT = "externalIntegration";
    private final Map<String, Function<FieldComponent, String>> serviceRegistry = new HashMap<>();

    public ExternalServiceOrchestratorImpl(List<ExternalService> eaisdoServices) {
        eaisdoServices.forEach(service -> serviceRegistry.put(service.getServiceCode(), service::sendRequest));
    }

    @Override
    public String callExternalService(FieldComponent component) {
        String serviceCode = component.getArgument(EXTERNAL_INTEGRATION_ARGUMENT);
        var serviceFunction = serviceRegistry.get(serviceCode);
        if (nonNull(serviceFunction))
            return serviceFunction.apply(component);

        throw new ExternalServiceException("Сервис код не зарегистрирован: " + serviceCode);
    }

}
