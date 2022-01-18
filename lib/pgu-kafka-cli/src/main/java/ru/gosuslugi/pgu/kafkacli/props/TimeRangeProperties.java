package ru.gosuslugi.pgu.kafkacli.props;

import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;

@Data
public class TimeRangeProperties {

    /**
     * С какого времени (включая таймзону)
     */
    OffsetDateTime since;

    /**
     * По какое время (включая таймзону)
     */
    OffsetDateTime until;

    public boolean includes(Instant instant) {
        return (since == null || instant != null && !instant.isBefore(since.toInstant()))
            && (until == null || instant != null && !instant.isAfter(until.toInstant()));
    }

}
