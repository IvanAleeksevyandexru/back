package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Запрос для получения данных о ТС из нотариальной конторы
 */
@Data
@Builder
public class FederalNotaryRequest {
    /** Идентификатор заказа - произвольная константа */
    private String orderId;
    /** VIN номер */
    private String vin;
    /** Идентификатор транзакции */
    private String tx;
}
