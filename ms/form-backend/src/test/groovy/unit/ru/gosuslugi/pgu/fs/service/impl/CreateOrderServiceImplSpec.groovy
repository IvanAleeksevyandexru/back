package unit.ru.gosuslugi.pgu.fs.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.OrderType
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.UserCookiesServiceImpl
import ru.gosuslugi.pgu.fs.exception.DuplicateOrderException
import ru.gosuslugi.pgu.fs.pgu.client.PguEmpowermentClient
import ru.gosuslugi.pgu.fs.pgu.client.PguEmpowermentClientV2
import ru.gosuslugi.pgu.fs.pgu.client.PguUtilsClient
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import ru.gosuslugi.pgu.fs.service.LkNotifierService
import ru.gosuslugi.pgu.fs.service.impl.CreateOrderServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.EmpowermentServiceImpl
import ru.gosuslugi.pgu.fs.service.ratelimit.RateLimitAnalyticProducer
import ru.gosuslugi.pgu.ratelimit.client.RateLimitService
import spock.lang.Specification

class CreateOrderServiceImplSpec extends Specification {

    CreateOrderServiceImpl createOrderService
    PguOrderService pguOrderServiceMock
    JsonProcessingService jsonProcessingService
    EmpowermentService empowermentService
    LkNotifierService lkNotifierService
    RateLimitService rateLimitService = Mock(RateLimitService)
    UserPersonalData userPersonalData = Mock(UserPersonalData)
    RateLimitAnalyticProducer rateLimitAnalyticProducer = Mock(RateLimitAnalyticProducer)

    Long orderId = 1L
    String serviceId = '1'

    def setup() {
        pguOrderServiceMock = Mock(PguOrderService)
        empowermentService = new EmpowermentServiceImpl(
                Mock(UserOrgData),
                Mock(UserPersonalData),
                Mock(PguEmpowermentClient),
                Mock(PguEmpowermentClientV2),
                Mock(PguUtilsClient))
        jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
        UserCookiesService userCookiesService = new UserCookiesServiceImpl()
        lkNotifierService = Mock(LkNotifierService)
        createOrderService = new CreateOrderServiceImpl(
                pguOrderServiceMock,
                jsonProcessingService,
                userCookiesService,
                empowermentService,
                lkNotifierService,
                rateLimitService,
                userPersonalData,
                rateLimitAnalyticProducer)
    }

    def 'Can try to create order: tryToCreateOrderId'() {
        given:
        def result

        when: 'поддержка обратной совместимости - если в услуге нет компонента с флагом createOrder - то создаем ордер на втором шаге'
        createOrderService.tryToCreateOrderId(
                serviceId,
                new ScenarioDto(finishedAndCurrentScreens: ['s1'], targetCode: '1', display: new DisplayRequest(
                        components: [new FieldComponent(id: 'test_id')]
                )),
                new ServiceDescriptor(applicationFields: [new FieldComponent(id: 'test_id', createOrder: true)])
        )

        then:
        1 * pguOrderServiceMock.createOrderId(serviceId, _ as String, _ as OrderType, null)

        when: 'orderId уже есть'
        result = createOrderService.tryToCreateOrderId(serviceId, new ScenarioDto(orderId: orderId), new ServiceDescriptor())

        then:
        result == 1L
        0 * pguOrderServiceMock.createOrderId(_ as String, _ as String, _ as OrderType, null)

        when: 'на экране нет компонентов с признаком createOrder'
        result = createOrderService.tryToCreateOrderId(serviceId, new ScenarioDto(display: new DisplayRequest(components: [])),
                new ServiceDescriptor(applicationFields: [new FieldComponent(createOrder: true)]))

        then:
        result == null
        0 * pguOrderServiceMock.createOrderId(_ as String, _ as String, _ as OrderType, null)

        when: 'на экране есть компонентов с признаком createOrder'
        pguOrderServiceMock.hasDuplicatesForOrder(_ as String, _ as String, _ as Map) >> true
        result = createOrderService.tryToCreateOrderId(serviceId,
                new ScenarioDto(serviceCode: serviceId, targetCode: '1',
                        display: new DisplayRequest(components: [new FieldComponent(id: 'c1')]),
                        currentValue: [c1: new ApplicantAnswer(visited: true, value: '1')]),
                new ServiceDescriptor(applicationFields: [new FieldComponent(id: 'c1', createOrder: true, attrs: [a1: ''])]))

        then:
        result == null
        1 * pguOrderServiceMock.createOrderId(_ as String, _ as String, _ as OrderType, null)
    }

    def 'Can check for duplicate - checkForDuplicate'() {
        given:
        def result

        when: 'компонет не найден'
        result = hasDuplicatesForOrder(new ScenarioDto(), Optional.empty(), new ServiceDescriptor())

        then:
        result

        when: 'нет ответа для компонента'
        hasDuplicatesForOrder(new ScenarioDto(currentValue: [c1: new ApplicantAnswer()]), Optional.of(new FieldComponent(id: 'c1')), new ServiceDescriptor())

        then:
        thrown(DuplicateOrderException)

        when: 'вызываем методы pguOrderService для обычных - не цикличных ответов'
        hasDuplicatesForOrder(new ScenarioDto(serviceCode: serviceId, targetCode: '1',
                currentValue: [c1: new ApplicantAnswer(value: '1')]), Optional.of(new FieldComponent(id: 'c1', attrs: [a1: ''])), new ServiceDescriptor())

        then:
        1 * pguOrderServiceMock.getPguServiceCodes(_ as String, _ as String)
        1 * pguOrderServiceMock.hasDuplicatesForOrder(_ as String, _ as String, _ as Map) >> true

        when: 'вызываем методы pguOrderService для обычных - не цикличных ответов и ссылками valuesForSave в описании компонента'
        hasDuplicatesForOrder(
                new ScenarioDto(
                        serviceCode: serviceId,
                        targetCode: '1',
                        currentValue: [c1: new ApplicantAnswer(value: '1')],
                        applicantAnswers: [q2: new ApplicantAnswer(value: 'qwerty')]
                ),
                Optional.of(new FieldComponent(id: 'c1', attrs: [a1: ''], fieldsForCheck: ['c1.value', 'q2.valuesForSave'])),
                new ServiceDescriptor(applicationFields: [
                        new FieldComponent(
                                id: 'q2',
                                attrs: [
                                        answers: [
                                                [
                                                        value        : 'qwerty',
                                                        valuesForSave: [
                                                                [key: 'q2-1', value: 'qwerty 1'],
                                                                [key: 'q2-2', value: 'qwerty 2']
                                                        ]
                                                ]
                                        ]
                                ]
                        )
                ])
        )

        then:
        1 * pguOrderServiceMock.getPguServiceCodes(_ as String, _ as String)
        1 * pguOrderServiceMock.hasDuplicatesForOrder(_ as String, _ as String, _ as Map) >> true

        when: 'вызываем методы pguOrderService даже когда orderId nonNull'
        hasDuplicatesForOrder(new ScenarioDto(serviceCode: serviceId, targetCode: '1', orderId: orderId,
                currentValue: [c1: new ApplicantAnswer(value: '1')]), Optional.of(new FieldComponent(id: 'c1', attrs: [a1: ''])), new ServiceDescriptor())

        then:
        1 * pguOrderServiceMock.getPguServiceCodes(_ as String, _ as String)
        1 * pguOrderServiceMock.hasDuplicatesForOrder(_ as String, _ as String, _ as Map) >> true

        when: 'вызываем методы pguOrderService для цикличных ответов'
        hasDuplicatesForOrder(new ScenarioDto(serviceCode: serviceId, targetCode: '1',
                currentValue: [c1: new ApplicantAnswer(value: '[{"f1": "vf11", "f2": "vf21"}, {"f1": "vf12", "f2": "vf22"}]')]),
                Optional.of(new FieldComponent(id: 'c1', attrs: [isCycled: true], fieldsForCheck: ['c1.value.f1', 'c1.value.f2'])), new ServiceDescriptor())

        then:
        1 * pguOrderServiceMock.getPguServiceCodes(_ as String, _ as String)
        2 * pguOrderServiceMock.hasDuplicatesForOrder(_ as String, _ as String, _ as Map)

        when: 'вызываем методы pguOrderService для цикличных ответов и обычного ответа'
        hasDuplicatesForOrder(new ScenarioDto(serviceCode: serviceId, targetCode: '1',
                currentValue: [c1: new ApplicantAnswer(value: '[{"f1": "vf11", "f2": "vf21"}, {"f1": "vf12", "f2": "vf22"}]')]),
                Optional.of(new FieldComponent(id: 'c1', attrs: [isCycled: true], fieldsForCheck: ['c1.value.f1', 'c1.value.f2', 'c2'])), new ServiceDescriptor())

        then:
        1 * pguOrderServiceMock.getPguServiceCodes(_ as String, _ as String)
        3 * pguOrderServiceMock.hasDuplicatesForOrder(_ as String, _ as String, _ as Map) >> true
    }

    def 'Can save values for order'() {
        given:
        def result

        when: 'нет ответов'
        result = createOrderService.saveValuesForOrder(
                new ServiceDescriptor(),
                new ScenarioDto(applicantAnswers: [:] as Map))

        then:
        result
        0 * pguOrderServiceMock.saveChoosenValuesForOrder(_ as String, _ as String, _ as Long, _ as Map)

        when: 'сохраняем значения для обычных - не цикличных ответов'
        createOrderService.saveValuesForOrder(
                new ServiceDescriptor(applicationFields: [new FieldComponent(id: 'c1', createOrder: true)]),
                new ScenarioDto(orderId: orderId, serviceCode: serviceId, targetCode: '1',
                        applicantAnswers: [c1: new ApplicantAnswer(value: '1')]))

        then:
        1 * pguOrderServiceMock.saveChoosenValuesForOrder(_ as String, _ as String, _ as Long, _ as Map)

        when: 'сохраняем значения для цикличных ответов'
        createOrderService.saveValuesForOrder(
                new ServiceDescriptor(applicationFields: [new FieldComponent(id: 'c1', attrs: [isCycled: true], fieldsForCheck: ['c1.value.f1', 'c1.value.f2'], createOrder: true)]),
                new ScenarioDto(orderId: orderId, serviceCode: serviceId, targetCode: '1',
                        applicantAnswers: [c1: new ApplicantAnswer(value: '[{"f1": "vf11", "f2": "vf21"}, {"f1": "vf12", "f2": "vf22"}]')]))

        then:
        3 * pguOrderServiceMock.saveChoosenValuesForOrder(_ as String, _ as String, _ as Long, _ as Map)
    }

    // грязный вызов метода
    def hasDuplicatesForOrder(ScenarioDto scenarioDto, Optional<FieldComponent> fieldComponent, ServiceDescriptor serviceDescriptor) {
        createOrderService.checkForDuplicate(scenarioDto, fieldComponent, serviceDescriptor)
    }
}
