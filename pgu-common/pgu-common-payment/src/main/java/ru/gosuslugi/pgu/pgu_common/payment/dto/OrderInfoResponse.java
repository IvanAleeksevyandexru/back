package ru.gosuslugi.pgu.pgu_common.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Ответ на запрос о данных по списку платежей
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderInfoResponse {

    List<OrderInfo> data;
    Long total;
}
