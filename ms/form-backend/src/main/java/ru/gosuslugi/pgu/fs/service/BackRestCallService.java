package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.BackRestCallResponseDto;
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto;

public interface BackRestCallService {

    BackRestCallResponseDto sendRequest(RestCallDto request);
}
