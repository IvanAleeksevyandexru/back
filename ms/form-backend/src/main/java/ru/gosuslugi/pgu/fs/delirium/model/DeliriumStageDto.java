package ru.gosuslugi.pgu.fs.delirium.model;

import lombok.*;
import ru.gosuslugi.pgu.components.descriptor.types.StageStatus;

/**
 * Состояние комплексной заявки
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliriumStageDto {
    /** Номер заявки */
    private Long orderId;

    /** Стадия заполнения заявки */
    private String stage;

    /** Стадия заполнения черновика участником */
    private StageStatus complete;

    /** Признак, что во время обработки запроса был изменен статус */
    private boolean statusChanged;

    /** UUID таймера */
    private String timerToken;

    /** Идентификатор заявки в сервисе тайммеров (не обязательно является orderId) */
    private String timerObjectId;
}
