package ru.gosuslugi.pgu.fs.suggests.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;

public interface SuggestsService {
    void send(Long userId,ScenarioDto scenario);
}
