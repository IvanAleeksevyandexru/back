package ru.gosuslugi.pgu.fs.pgu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.gosuslugi.pgu.core.lk.model.order.Order;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListOrderResponse {

    Long total;

    List<Order> order;
}
