package ru.gosuslugi.pgu.fs.pgu.dto.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * ДТО для ленты в ЛК.
 * Сделано, т.к. из либ ЕПГУ криво работает конвертер.
 * @see ru.gosuslugi.pgu.core.lk.model.feed.Feed
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedDto {
    private Detail detail;
}
