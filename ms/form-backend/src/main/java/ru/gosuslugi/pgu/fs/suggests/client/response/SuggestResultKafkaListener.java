package ru.gosuslugi.pgu.fs.suggests.client.response;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import ru.gosuslugi.pgu.dto.suggest.SuggestDraftDto;

import static java.lang.String.format;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.debug;

@Slf4j
@AllArgsConstructor
public class SuggestResultKafkaListener implements ListenableFutureCallback<SendResult<Long, SuggestDraftDto>> {

    private final Long userId;
    private final SuggestDraftDto dto;

    @Override
    public void onSuccess(SendResult<Long, SuggestDraftDto> result) {
        if (result == null) {
            log.error("Kafka sending error: result is null for user {} and serviceId  {}", userId, dto.getScenarioDto().getServiceCode());
            return;
        }

        debug(log, () -> format("Notification to suggestion service sent successfully for user %s, serviceId %s and offset=[%s]",
                userId, dto.getScenarioDto().getServiceCode(), result.getRecordMetadata().offset()));
    }

    @Override
    public void onFailure(Throwable ex) {
        log.error("Suggestion service:unable to send draft with user {} and serviceId {} due to: {}", userId, dto.getScenarioDto().getServiceCode(), ex.getMessage(), ex);
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        } else {
            throw new RuntimeException(ex);
        }
    }
}