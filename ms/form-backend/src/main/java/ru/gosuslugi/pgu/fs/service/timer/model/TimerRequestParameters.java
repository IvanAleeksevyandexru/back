package ru.gosuslugi.pgu.fs.service.timer.model;

import lombok.Data;

/**
 * Параметры обращения к сервису таймеров
 * @see ru.gosuslugi.pgu.fs.service.TimerClient
 */
@Data
public class TimerRequestParameters {

    /** UUID таймера */
    private final String uuid;

    /** Идентификатор заявки */
    private final String objectId;

    /** Код таймера */
    private final String code;
}
