package ru.gosuslugi.pgu.fs.suggests.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.suggests.client.SuggestClient;
import ru.gosuslugi.pgu.fs.suggests.service.SuggestsService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestsServiceImpl implements SuggestsService {

    private final SuggestClient suggestClient;

    @Override
    public void send(Long userId,ScenarioDto scenario) {
        suggestClient.send(userId, scenario);
    }
}
