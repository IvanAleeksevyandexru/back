package ru.gosuslugi.pgu.fs.service

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.core.lk.model.order.Order
import ru.gosuslugi.pgu.draft.DraftClient
import ru.gosuslugi.pgu.dto.InitServiceDto
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import ru.gosuslugi.pgu.fs.exception.DuplicateOrderException
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.MainScreenServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.ScenarioInitializerServiceImpl
import ru.gosuslugi.pgu.fs.service.process.ExternalScreenProcess
import ru.gosuslugi.pgu.fs.service.process.NextScreenProcess
import ru.gosuslugi.pgu.fs.service.process.PrevScreenProcess
import spock.lang.Specification

class MainScreenServiceSpec extends Specification {

    PguOrderService pguOrderService = Mock(PguOrderService)
    MainDescriptorService descriptorService = Mock(MainDescriptorService)
    FormScenarioDtoServiceImpl scenarioDtoService = Mock(FormScenarioDtoServiceImpl)
    EmpowermentService empowermentService = Mock(EmpowermentService)
    ErrorModalDescriptorService errorModalDescriptorService = Mock(ErrorModalDescriptorService)

    ScenarioInitializerService scenarioInitializerService = new ScenarioInitializerServiceImpl(
            pguOrderService,
            Stub(DraftClient),
            Stub(UserPersonalData),
            Stub(TransformService),
            Stub(DeliriumService),
            Stub(OrderInfoService),
            descriptorService,
            scenarioDtoService,
            errorModalDescriptorService,
            Stub(DisclaimersService)
    )

    MainScreenService screenService = new MainScreenServiceImpl(
            descriptorService,
            Stub(DeliriumService),
            Stub(UserPersonalData),
            pguOrderService,
            Stub(DraftClient),
            Stub(OrderInfoService),
            Stub(TransformService),
            Stub(PrevScreenProcess),
            Stub(NextScreenProcess),
            Stub(ExternalScreenProcess),
            scenarioInitializerService)

    String serviceId = '105'
    String targetId = '-105'
    InitServiceDto initDto = new InitServiceDto(targetId: targetId)
    def descriptor = new ServiceDescriptor(multipleOrders: true)
    def descriptorNotMultiple = new ServiceDescriptor(multipleOrders: false)

    def setup() {
        empowermentService.hasEmpowerment(_) >> true
    }

    def "getInitScreen/2"() {
        given:
        Order existingOrder = new Order(id: 1234L)
        scenarioDtoService.createInitScenario(_ as ServiceDescriptor, _ as InitServiceDto) >> new ScenarioDto()
        descriptorService.getServiceDescriptor(serviceId) >> descriptor

        when:
        screenService.getInitScreen(serviceId, initDto)

        then: "проверяем, что происходит удаление черновика, если был найден предыдущий"
        1 * pguOrderService.findLastOrder(serviceId, initDto.getTargetId()) >> existingOrder
        1 * pguOrderService.deleteOrderById(existingOrder.getId())
        noExceptionThrown()

        when:
        screenService.getInitScreen(serviceId, initDto)

        then: "проверяем, что черновик не удаляется, если не был найден предыдущий"
        1 * pguOrderService.findLastOrder(serviceId, initDto.getTargetId()) >> null
        0 * pguOrderService.deleteOrderById(_)
        noExceptionThrown()
    }

    def "getInitScreenWithNewOrderId_duplicated_orders_accept"() {
        given:
        //noinspection GroovyAssignabilityCheck
        scenarioDtoService.createInitScenario(_, _) >> new ScenarioDto()
        descriptorService.getServiceDescriptor(serviceId) >> descriptor
        pguOrderService.allTerminated(serviceId, initDto.getTargetId()) >> false

        when:
        screenService.getInitScreen(serviceId, initDto)

        then: "проверяем, дубли не запрещены при не терминальных статусах"
        0 * pguOrderService.deleteOrderById(_)
        2 * descriptorService.getServiceDescriptor(serviceId) >> descriptor
        0 * pguOrderService.allTerminated(serviceId, initDto.getTargetId())
        noExceptionThrown()
    }

    def "getInitScreenWithNewOrderId_duplicated_orders_locked"() {
        given:
        descriptorService.getServiceDescriptor(serviceId) >> descriptorNotMultiple
        pguOrderService.allTerminated(serviceId, initDto.getTargetId()) >> false

        when:
        screenService.getInitScreen(serviceId, initDto)

        then: "проверяем, дубли запрещены при не терминальных статусах"
        0 * pguOrderService.deleteOrderById(_)
        2 * descriptorService.getServiceDescriptor(serviceId) >> descriptorNotMultiple
        1 * pguOrderService.allTerminated(serviceId, initDto.getTargetId()) >> false
        thrown(DuplicateOrderException.class)
    }
}
