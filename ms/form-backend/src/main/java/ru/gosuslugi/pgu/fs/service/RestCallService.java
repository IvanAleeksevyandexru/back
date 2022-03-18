package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto;

public interface RestCallService {

    ComponentResponse<RestCallDto> fillRestCallDto(FieldComponent component, String restCallUrl);
}
