package ru.gosuslugi.pgu.fs.pgu.service;

import ru.gosuslugi.lk.api.order.Order;
import ru.gosuslugi.pgu.core.lk.model.order.dto.request.SetOrderAttributeDTO;

public interface OrderAttributesService {

    Order updateOrderStatus(Long orderStatus, Long orderId);

    Order updateTechOrderStatus(Long orderStatus, Long orderId);

}
