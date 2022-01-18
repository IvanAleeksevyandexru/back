package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

public interface ExternalServiceOrchestrator {

    String callExternalService(FieldComponent component);

}
