package ru.gosuslugi.pgu.fs.delirium.model;

import lombok.Data;
import ru.gosuslugi.pgu.fs.delirium.model.declaration.DeliriumContextDataDto;

@Data
public class DeliriumRequestDto {

    /**
     * Идентификатор текущего участника заявления
     */
    long oid;

    /**
     * Идентификатор черновика текущего участника
     */
    long orderId;

    /**
     * Дополнительные сведения
     */
    DeliriumContextDataDto contextData;

}
