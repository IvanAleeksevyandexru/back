package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.BackRestCallResponseDto;
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto;

import java.util.HashMap;
import java.util.Map;

public interface BackRestCallService {

    Map<String, Boolean> OPTIONS = new HashMap<>();
    BackRestCallResponseDto sendRequest(RestCallDto request);

    default void setOption(String option) {
        OPTIONS.put(option, true);
    }

    default void clearOptions() {
        OPTIONS.clear();
    }
}
