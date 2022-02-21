package unit.ru.gosuslugi.pgu.fs.service.impl.process

import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.core.lk.model.order.Order
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.ScenarioRequest
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.OrderBehaviourType
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry
import ru.gosuslugi.pgu.fs.common.service.*
import ru.gosuslugi.pgu.fs.common.service.impl.ComputeAnswerServiceImpl
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService
import ru.gosuslugi.pgu.fs.service.CreateOrderService
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import ru.gosuslugi.pgu.fs.service.IntegrationService
import ru.gosuslugi.pgu.fs.service.impl.AdditionalAttributesHelper
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl
import ru.gosuslugi.pgu.fs.service.process.impl.screen.NextScreenProcessImpl
import ru.gosuslugi.pgu.fs.suggests.service.SuggestsService
import spock.lang.Specification

import java.util.function.Consumer
// todo не все тест кейсы покрыты
class NextScreenProcessImplSpec extends Specification {

    NextScreenProcessImpl process

    MainDescriptorService mainDescriptorServiceMock = Mock(MainDescriptorService)
    AnswerValidationService answerValidationServiceMock = Mock(AnswerValidationService)
    ComponentService componentServiceMock = Mock(ComponentService)
    CreateOrderService createOrderServiceMock = Mock(CreateOrderService)
    CycledScreenService cycledScreenServiceMock = Mock(CycledScreenService)
    ScreenFinderService screenFinderServiceMock = Mock(ScreenFinderService)
    HelperScreenRegistry screenRegistryMock = Mock(HelperScreenRegistry)
    UserPersonalData userPersonalDataMock = Mock(UserPersonalData)
    UserOrgData userOrgDataMock = Mock(UserOrgData)
    PguOrderService pguOrderServiceMock = Mock(PguOrderService)
    FormScenarioDtoServiceImpl scenarioDtoServiceMock = Mock(FormScenarioDtoServiceImpl)
    IntegrationService integrationServiceMock = Mock(IntegrationService)
    ComputeAnswerServiceImpl computeAnswerServiceMock = Mock(ComputeAnswerServiceImpl)
    AdditionalAttributesHelper additionalAttributesHelper = Mock(AdditionalAttributesHelper)
    EmpowermentService empowermentService = Mock(EmpowermentService)
    ErrorModalDescriptorService errorModalDescriptorService = Mock(ErrorModalDescriptorService)

    String serviceId = '1'
    Long orderId = 1

    def setup() {
        process = new NextScreenProcessImpl(
                mainDescriptorServiceMock,
                answerValidationServiceMock,
                componentServiceMock,
                createOrderServiceMock,
                cycledScreenServiceMock,
                screenFinderServiceMock,
                Stub(DisplayReferenceService),
                screenRegistryMock,
                userPersonalDataMock,
                pguOrderServiceMock,
                scenarioDtoServiceMock,
                integrationServiceMock,
                computeAnswerServiceMock,
                additionalAttributesHelper,
                Stub(SuggestsService),
                empowermentService,
                errorModalDescriptorService
        )
    }

    def 'Can build response'() {
        when:
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(finishedAndCurrentScreens: ['s1'])))
        process.buildResponse()
        def result = process.start()

        then:
        result.scenarioDto.finishedAndCurrentScreens == ['s1']
    }

    def 'Can try to create order id'() {
        when:
        createOrderServiceMock.tryToCreateOrderId(_ as String, _ as ScenarioDto, null) >> 100500
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(finishedAndCurrentScreens: ['s1'])))
                .execute({process.buildResponse()} as Consumer)
                .execute({process.tryToCreateOrderId()} as Consumer)
        def result = process.start()

        then:
        result.scenarioDto.orderId == 100500
    }

    def 'draftShouldExist must return correct value for SMEV_ORDER orderBehaviorType'() {

        when:
        mainDescriptorServiceMock.getServiceDescriptor(serviceId) >>> new ServiceDescriptor(
                orderBehaviourType: OrderBehaviourType.SMEV_ORDER,
                smevOrderStatuses: smevOrderStatuses,
        )
        pguOrderServiceMock.findOrderByIdCached(orderId) >>> new Order(orderStatusId: 0)
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(
                orderId: orderId,
                display: new DisplayRequest(
                        terminal: false
                )
        )))
        process.buildResponse()

        then:
        process.draftShouldExist() == draftShouldExist

        where:
        smevOrderStatuses | draftShouldExist
        [1L, 2L, 3L]      | true
        [0L]              | false
    }

}
