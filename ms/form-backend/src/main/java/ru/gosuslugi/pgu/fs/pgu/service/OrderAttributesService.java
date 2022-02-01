package ru.gosuslugi.pgu.fs.pgu.service;

import ru.gosuslugi.lk.api.order.Order;

public interface OrderAttributesService {

    Order updateOrderStatus(Long orderStatus, Long orderId);

    Order updateTechOrderStatus(Long orderStatus, Long orderId);

    Order updateTechOrderStatusNoCache(Long orderStatus, Long orderId);

}
