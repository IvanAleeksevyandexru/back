package ru.gosuslugi.pgu.fs.pgu.client;

import ru.gosuslugi.pgu.core.lk.model.feed.Feed;
import ru.gosuslugi.pgu.fs.pgu.dto.feed.FeedDto;

public interface PguFeedClient {

    /**
     * Находит запись в ленте событий пользователя по указанному типу и идентификатору
     * @param type тип записи
     * @param id идентификатор
     * @return запись, {@code null}, если запись не найдена
     */
    FeedDto findFeed(Feed.FeedType type, Long id);
}
