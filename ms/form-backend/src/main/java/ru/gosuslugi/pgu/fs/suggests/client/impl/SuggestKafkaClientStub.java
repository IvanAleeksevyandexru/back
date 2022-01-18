package ru.gosuslugi.pgu.fs.suggests.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.suggests.client.SuggestClient;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.kafka.producer.suggests", name = "enabled", havingValue = "false")
public class SuggestKafkaClientStub implements SuggestClient {
    @Override
    public void send(Long userId, ScenarioDto scenarioDto) {

    }
}
