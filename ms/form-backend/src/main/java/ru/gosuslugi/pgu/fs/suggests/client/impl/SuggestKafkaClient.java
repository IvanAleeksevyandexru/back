package ru.gosuslugi.pgu.fs.suggests.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.suggest.SuggestDraftDto;
import ru.gosuslugi.pgu.fs.suggests.client.SuggestClient;
import ru.gosuslugi.pgu.fs.suggests.client.response.SuggestResultKafkaListener;
import ru.gosuslugi.pgu.fs.suggests.properties.KafkaProducerProperties;

import static java.lang.String.format;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.debug;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.kafka.producer.suggests", name = "enabled", havingValue = "true")
public class SuggestKafkaClient implements SuggestClient {

    private final KafkaProducerProperties properties;
    private final KafkaTemplate<Long, SuggestDraftDto> suggestsKafkaTemplate;

    public SuggestKafkaClient(KafkaProducerProperties properties,
                              KafkaTemplate<Long, SuggestDraftDto> suggestsKafkaTemplate
    ) {
        this.properties = properties;
        this.suggestsKafkaTemplate = suggestsKafkaTemplate;
    }

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
                suggestsKafkaTemplate.send(properties.getTopicName(), dto.getScenarioDto().getOrderId(),dto);
        future.addCallback(new SuggestResultKafkaListener(userId, dto));
    }
}
