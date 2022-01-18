package unit.ru.gosuslugi.pgu.fs.pgu

import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.core.lk.model.order.Order
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService
import ru.gosuslugi.pgu.fs.pgu.client.PguClient
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses
import ru.gosuslugi.pgu.fs.pgu.mapper.HighLoadOrderPguMapper
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService
import ru.gosuslugi.pgu.fs.pgu.service.impl.PguOrderServiceImpl
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import spock.lang.Specification

class PguOrderServiceSpec extends Specification {
    private PguClient pguClient = Mock(PguClient)

    private PguOrderService service = new PguOrderServiceImpl(
            Stub(UserPersonalData),
            Stub(UserOrgData),
            pguClient,
            Stub(UserCookiesService),
            Stub(EmpowermentService),
            Mock(HighLoadOrderPguMapper)
    )

    def "all statuses terminated"() {
        given:
        String serviceCode = "1"
        String targetCode = "1"
        List<Order> orders = [
                new Order(orderStatusId: OrderStatuses.terminated().first()),
                new Order(orderStatusId: OrderStatuses.terminated().last())
        ]

        when:
        boolean terminated = service.allTerminated(serviceCode, targetCode)

        then:
        1 * pguClient.findOrders(serviceCode, targetCode) >> orders
        terminated
    }

    def "contains not terminated statuses"() {
        given:
        String serviceCode = "1"
        String targetCode = "1"
        List<Order> orders = [
                new Order(orderStatusId: OrderStatuses.terminated().first()),
                new Order(orderStatusId: 13)
        ]

        when:
        boolean terminated = service.allTerminated(serviceCode, targetCode)

        then:
        1 * pguClient.findOrders(serviceCode, targetCode) >> orders
        !terminated
    }
}
