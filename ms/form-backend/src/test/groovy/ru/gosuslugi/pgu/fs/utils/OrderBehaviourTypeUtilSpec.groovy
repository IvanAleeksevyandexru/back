package ru.gosuslugi.pgu.fs.utils

import ru.gosuslugi.pgu.core.lk.model.order.Order
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import spock.lang.Specification

import static ru.gosuslugi.pgu.dto.descriptor.types.OrderBehaviourType.ORDER_AND_DRAFT
import static ru.gosuslugi.pgu.dto.descriptor.types.OrderBehaviourType.SMEV_ORDER

class OrderBehaviourTypeUtilSpec extends Specification {
    def 'getSmevOrderDraftFlag must return correct value'() {
        expect:
        result == OrderBehaviourTypeUtil.getSmevOrderDraftFlag(new ServiceDescriptor(
                orderBehaviourType: type,
                smevOrderStatuses: statuses
        ), new Order(orderStatusId: status), statusContains)

        where:
        type             | statuses | status | statusContains | result
        ORDER_AND_DRAFT  | []       | 0L     | false          | false
        SMEV_ORDER       | []       | 0L     | false          | true
        SMEV_ORDER       | [1L]     | 0L     | false          | true
        SMEV_ORDER       | [1L]     | 1L     | false          | false
        SMEV_ORDER       | [1L]     | 0L     | true           | false
        SMEV_ORDER       | [1L]     | 1L     | true           | true
    }
}
