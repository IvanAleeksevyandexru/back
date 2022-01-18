package ru.gosuslugi.pgu.fs.utils;

import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.OrderBehaviourType;

public class OrderBehaviourTypeUtil {

    public static boolean getSmevOrderDraftFlag(ServiceDescriptor descriptor, Order order, boolean statusContains) {
        return descriptor.getOrderBehaviourType() == OrderBehaviourType.SMEV_ORDER
            && statusContains == descriptor.getSmevOrderStatuses().contains(order.getOrderStatusId());
    }

}
