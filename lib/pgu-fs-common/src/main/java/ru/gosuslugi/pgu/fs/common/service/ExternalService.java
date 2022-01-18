package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

public interface ExternalService {

    String getServiceCode();

    String sendRequest(FieldComponent fieldComponent);

}
