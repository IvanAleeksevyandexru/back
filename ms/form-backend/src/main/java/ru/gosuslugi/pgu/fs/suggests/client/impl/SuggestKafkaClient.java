package ru.gosuslugi.pgu.fs.suggests.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import ru.gosuslugi.pgu.common.kafka.properties.KafkaProducerProperties;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.suggest.SuggestDraftDto;
import ru.gosuslugi.pgu.fs.suggests.client.SuggestClient;
import ru.gosuslugi.pgu.fs.suggests.client.response.SuggestResultKafkaListener;

import static java.lang.String.format;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.debug;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka.producer.suggests", name = "enabled", havingValue = "true")
public class SuggestKafkaClient implements SuggestClient {

    private final KafkaProducerProperties suggestsProducerProperties;
    private final KafkaTemplate<Long, SuggestDraftDto> suggestsKafkaTemplate;

    @Override
    public void send(Long userId,ScenarioDto scenarioDto) {
        if (userId < 0) { // Для созаявителей создается копия основного заявления, а userId фейковый: -10000, -10001 и т.д.
            return;
        }
        debug(log, () -> format("Sending notification to suggestion service to save suggests for user %s and service code %s", userId, scenarioDto.getServiceCode()));
        var dto = new SuggestDraftDto();
        dto.setUserId(userId);
        dto.setScenarioDto(scenarioDto);
        ListenableFuture<SendResult<Long,SuggestDraftDto>> future =
                suggestsKafkaTemplate.send(suggestsProducerProperties.getTopic(), dto.getScenarioDto().getOrderId(), dto);
        future.addCallback(new SuggestResultKafkaListener(userId, dto));
    }
}
