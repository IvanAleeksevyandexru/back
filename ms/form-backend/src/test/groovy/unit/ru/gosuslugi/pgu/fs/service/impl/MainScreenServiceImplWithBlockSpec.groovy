package unit.ru.gosuslugi.pgu.fs.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.CacheManager
import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.draft.DraftClient
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.ScenarioRequest
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.ScreenRule
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry
import ru.gosuslugi.pgu.fs.common.service.ComponentService
import ru.gosuslugi.pgu.fs.common.service.CycledScreenService
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService
import ru.gosuslugi.pgu.fs.common.service.ScreenFinderService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.exception.DuplicateOrderException
import ru.gosuslugi.pgu.fs.generator.ScenarioGeneratorClient
import ru.gosuslugi.pgu.fs.helper.GenderHelper
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService
import ru.gosuslugi.pgu.fs.service.DeliriumService
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import ru.gosuslugi.pgu.fs.service.IntegrationService
import ru.gosuslugi.pgu.fs.service.OrderInfoService
import ru.gosuslugi.pgu.fs.service.TransformService
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.MainScreenServiceImpl
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService
import spock.lang.Ignore
import spock.lang.Specification

// todo вынести тесты в CreateOrderServiceImplSpec
@Ignore
class MainScreenServiceImplWithBlockSpec extends Specification {
    def serviceId = '1'
    def serviceCode = serviceId
    def targetCode = '-1000'

    static ServiceDescriptor getSimpleServiceDescriptor() {
        new ServiceDescriptor(
                init: 's1',
                screens: [
                        new ScreenDescriptor(id: 's1', name: 's1', type: ScreenType.INFO, componentIds: ['c1']),
                        new ScreenDescriptor(id: 's2', name: 's2', type: ScreenType.INFO, componentIds: ['c2']),
                        new ScreenDescriptor(id: 's3', name: 's3', type: ScreenType.INFO, componentIds: ['c3']),
                        new ScreenDescriptor(id: 's4', name: 's4', type: ScreenType.INFO, componentIds: ['c4'], isTerminal: true)
                ],
                applicationFields: [
                        new FieldComponent(id: 'c1', type: ComponentType.InfoScr),
                        new FieldComponent(id: 'c2', type: ComponentType.InfoScr),
                        new FieldComponent(id: 'c3', type: ComponentType.InfoScr),
                        new FieldComponent(id: 'c4', type: ComponentType.InfoScr)
                ],
                screenRules: [
                        's1': [new ScreenRule(conditions: [], nextDisplay: 's2')],
                        's2': [new ScreenRule(conditions: [], nextDisplay: 's3')],
                        's3': [new ScreenRule(conditions: [], nextDisplay: 's4')]
                ]
        )
    }

    static ScenarioRequest getSimpleScenarioRequest() {
        new ScenarioRequest(
                scenarioDto: new ScenarioDto(
                        finishedAndCurrentScreens: ['s1', 's2', 's3', 's4'],
                        display: new DisplayRequest(id: 's4')))
    }

    static ServiceDescriptor getSimpleServiceDescriptorWithCreateOrderFlag() {
        new ServiceDescriptor(
                init: 's1',
                screens: [
                        new ScreenDescriptor(id: 's1', name: 's1', type: ScreenType.INFO, componentIds: ['c1']),
                        new ScreenDescriptor(id: 's2', name: 's2', type: ScreenType.INFO, componentIds: ['c2']),
                        new ScreenDescriptor(id: 's3', name: 's3', type: ScreenType.INFO, componentIds: ['c3']),
                        new ScreenDescriptor(id: 's4', name: 's4', type: ScreenType.INFO, componentIds: ['c4'], isTerminal: true)
                ],
                applicationFields: [
                        new FieldComponent(id: 'c1', type: ComponentType.InfoScr),
                        new FieldComponent(id: 'c2', type: ComponentType.InfoScr),
                        new FieldComponent(id: 'c3', type: ComponentType.InfoScr, createOrder: true),
                        new FieldComponent(id: 'c4', type: ComponentType.InfoScr)
                ],
                screenRules: [
                        's1': [new ScreenRule(conditions: [], nextDisplay: 's2')],
                        's2': [new ScreenRule(conditions: [], nextDisplay: 's3')],
                        's3': [new ScreenRule(conditions: [], nextDisplay: 's4')]
                ]
        )
    }

    static ScenarioRequest getSimpleScenarioRequest(Long orderId, String serviceCode, String targetCode, List<String> screens, DisplayRequest displayRequest) {
        new ScenarioRequest(
                scenarioDto: new ScenarioDto(
                        orderId: orderId,
                        serviceCode: serviceCode,
                        targetCode: targetCode,
                        finishedAndCurrentScreens: screens,
                        display: displayRequest,
                        gender: GenderHelper.MAN_GENDER))
    }

    static ScenarioRequest getSimpleScenarioRequest(Long orderId, String serviceId, String serviceCode, String targetCode, List<String> screens, DisplayRequest displayRequest, Map<String, ApplicantAnswer> currentAnswer) {
        new ScenarioRequest(
                scenarioDto: new ScenarioDto(
                        orderId: orderId,
                        serviceId: serviceId,
                        serviceCode: serviceCode,
                        targetCode: targetCode,
                        finishedAndCurrentScreens: screens,
                        display: displayRequest,
                        currentValue: currentAnswer))
    }

    static ScenarioRequest getSimpleScenarioRequest(Long orderId, String serviceId, String serviceCode, String targetCode, List<String> screens, DisplayRequest displayRequest, Map<String, ApplicantAnswer> currentValue, Map<String, ApplicantAnswer> applicantAnswers) {
        new ScenarioRequest(
                scenarioDto: new ScenarioDto(
                        orderId: orderId,
                        serviceId: serviceId,
                        serviceCode: serviceCode,
                        targetCode: targetCode,
                        finishedAndCurrentScreens: screens,
                        display: displayRequest,
                        currentValue: currentValue,
                        applicantAnswers: applicantAnswers))
    }

    def 'Check can creating order for blocking in new orders'() {
        given:
        MainDescriptorService mainDescriptorService = Mock(MainDescriptorService)
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptor()
        PguOrderService pguOrderService = Mock(PguOrderService)
        MainScreenServiceImpl service = new MainScreenServiceImpl(
                mainDescriptorService,
                Stub(DeliriumService),
                Stub(UserPersonalData),
                Stub(UserOrgData),
                pguOrderService,
                Stub(DraftClient),
                Stub(IntegrationService),
                Stub(FormScenarioDtoServiceImpl),
                Stub(OrderInfoService),
                Stub(TransformService),
                Stub(BillingService),
                Stub(ScenarioGeneratorClient),
                Stub(CacheManager),
                Stub(EmpowermentService)
        )

        when: 'Service descriptor без компонентов с флагом createOrder c одним пройденным экраном'
        List<String> finishedScreens = List.of('s1')
        List<FieldComponent> fieldComponentList = List.of(getSimpleServiceDescriptor().getFieldComponentById('c2').get())
        DisplayRequest displayRequest = new DisplayRequest(id: 's2', components: fieldComponentList)
        service.beforeNextScreen(getSimpleScenarioRequest(null, serviceCode, targetCode, finishedScreens, displayRequest))

        then:
        1 * pguOrderService.createOrderId(serviceId, targetCode)
        0 * pguOrderService.hasDuplicatesForOrder(serviceCode, targetCode, _)

        when: 'Service descriptor без компонентов с флагом createOrder c одним пройденным экраном и orderId существует'
        finishedScreens = List.of('s1')
        fieldComponentList = List.of(getSimpleServiceDescriptor().getFieldComponentById('c2').get())
        displayRequest = new DisplayRequest(id: 's2', components: fieldComponentList)
        service.beforeNextScreen(getSimpleScenarioRequest(1, serviceCode, targetCode, finishedScreens, displayRequest))

        then:
        0 * pguOrderService.createOrderId(serviceId, targetCode)
        0 * pguOrderService.hasDuplicatesForOrder(serviceCode, targetCode, _)

        when: 'Service descriptor с компонентами с флагом createOrder текущий экран без компонентов с флагом'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c2').get())
        displayRequest = new DisplayRequest(id: 's2', components: fieldComponentList)
        service.beforeNextScreen(getSimpleScenarioRequest(null, serviceCode, targetCode, finishedScreens, displayRequest))

        then:
        0 * pguOrderService.createOrderId(serviceId, targetCode)
        0 * pguOrderService.hasDuplicatesForOrder(serviceCode, targetCode, _)

        when: 'Service descriptor с компонентами с флагом createOrder текущий экран с компонентом с флагом без currentValue'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c3').get())
        displayRequest = new DisplayRequest(id: 's3', components: fieldComponentList)
        service.beforeNextScreen(getSimpleScenarioRequest(null, serviceCode, targetCode, finishedScreens, displayRequest))

        then:
        def ex = thrown(Exception)
        ex.message == 'Ошибка при проверке возможности создания заявления - applicantAnswer или его значение пусто'
        0 * pguOrderService.createOrderId(serviceId, targetCode)
        0 * pguOrderService.hasDuplicatesForOrder(serviceCode, targetCode, _)

        when: 'Service descriptor с компонентами с флагом createOrder текущий экран с компонентом с флагом с currentValue и pguOrderService.canCreateOrder вернул false'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c3').get())
        displayRequest = new DisplayRequest(id: 's3', components: fieldComponentList)
        Map<String, ApplicantAnswer> currentValue = getCurrentValue('c3', true, 'Test')
        service.beforeNextScreen(getSimpleScenarioRequest(null, serviceId, serviceCode, targetCode, finishedScreens, displayRequest, currentValue))

        then:
        0 * pguOrderService.createOrderId(serviceId, targetCode)
        1 * pguOrderService.hasDuplicatesForOrder(serviceCode, targetCode, _) >> Boolean.FALSE

        when: 'Service descriptor с компонентами с флагом createOrder текущий экран с компонентом с флагом с currentValue и pguOrderService.canCreateOrder вернул true'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c3').get())
        displayRequest = new DisplayRequest(id: 's3', components: fieldComponentList)
        currentValue = getCurrentValue('c3', true, 'Test')
        service.beforeNextScreen(getSimpleScenarioRequest(null, serviceId, serviceCode, targetCode, finishedScreens, displayRequest, currentValue))

        then:
        1 * pguOrderService.createOrderId(serviceId, targetCode)
        1 * pguOrderService.hasDuplicatesForOrder(serviceCode, targetCode, _) >> Boolean.TRUE
    }

    ScreenDescriptor screenDescriptorS2 = getSimpleServiceDescriptor().getScreenDescriptorById('s2').get()
    ScreenDescriptor screenDescriptorS4 = getSimpleServiceDescriptor().getScreenDescriptorById('s4').get()
    ScreenDescriptor screenDescriptorS2_with_CreateOrderFlag = getSimpleServiceDescriptorWithCreateOrderFlag().getScreenDescriptorById('s2').get()
    ScreenDescriptor screenDescriptorS4_with_CreateOrderFlag = getSimpleServiceDescriptorWithCreateOrderFlag().getScreenDescriptorById('s4').get()

    def 'Check method getMapValuesForCheck()'() {
        given:
        MainDescriptorService mainDescriptorService = Mock(MainDescriptorService)
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        PguOrderService pguOrderService = Mock(PguOrderService)
        MainScreenServiceImpl service = new MainScreenServiceImpl(
                mainDescriptorService,
                Stub(DeliriumService),
                Stub(UserPersonalData),
                Stub(UserOrgData),
                Stub(PguOrderService),
                Stub(DraftClient),
                Stub(IntegrationService),
                Stub(FormScenarioDtoServiceImpl),
                Stub(OrderInfoService),
                Stub(TransformService),
                Stub(BillingService),
                Stub(ScenarioGeneratorClient),
                Stub(CacheManager),
                Stub(EmpowermentService)
        )
        service.jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())

        when: 'fieldsForCheck is empty, value is simple'
        Map<String, ApplicantAnswer> applicantAnswers = Map.of('c3', new ApplicantAnswer(visited: true, value: 'test value'))
        def result = service.getMapValuesForCheck('c3', applicantAnswers, null, null)

        then:
        result.get('c3') == 'test value'

        when: 'fieldsForCheck is empty, value is complex'
        applicantAnswers = Map.of('c3', new ApplicantAnswer(visited: true, value: '\'key1\':\'value for key1\',\'key2\':\'value for key2\',\'key3\':\'value for key3\''))
        result = service.getMapValuesForCheck('c3', applicantAnswers, null, null)

        then:
        result.get('c3') == '\'key1\':\'value for key1\',\'key2\':\'value for key2\',\'key3\':\'value for key3\''

        when: 'fieldsForCheck is not empty, value is simple'
        applicantAnswers = ['c3': new ApplicantAnswer(visited: true, value: '{\"key1\": \"value for key1\", \"key2\": \"value for key2\"}')]
        def fieldForCheck = ['c3.value.key1', 'c3.value.complexKey.complexKey2']
        result = service.getMapValuesForCheck('c3', applicantAnswers, null, fieldForCheck)

        then:
        result.get('c3.value.key1') == 'value for key1'

        when: 'fieldsForCheck is not empty, value is complex'
        applicantAnswers = Map.of('c3', new ApplicantAnswer(visited: true, value: '{\"key1\": \"value for key1\", \"key2\": \"value for key2\", \"complexKey\": {\"complexKey1\": \"complex value1\", \"complexKey2\": \"complex value2\"}}'))
        fieldForCheck = ['c3.value.key1', 'c3.value.complexKey.complexKey2']
        result = service.getMapValuesForCheck('c3', applicantAnswers, null, fieldForCheck)

        then:
        result.get('c3.value.key1') == 'value for key1'
        result.get('c3.value.complexKey.complexKey2') == 'complex value2'
    }

    def 'Check saving values for blocking in new orders'() {
        given:
        MainDescriptorService mainDescriptorService = Mock(MainDescriptorService)
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptor()

        UserPersonalData userPersonalData = Stub(UserPersonalData)
        Person person = new Person();
        person.setGender(GenderHelper.MAN_GENDER)
        userPersonalData.getPerson() >> person

        PguOrderService pguOrderService = Mock(PguOrderService)
        MainScreenServiceImpl service = new MainScreenServiceImpl(
                mainDescriptorService,
                Stub(DeliriumService),
                Stub(UserPersonalData),
                Stub(UserOrgData),
                pguOrderService,
                Stub(DraftClient),
                Stub(IntegrationService),
                Stub(FormScenarioDtoServiceImpl),
                Stub(OrderInfoService),
                Stub(TransformService),
                Stub(BillingService),
                Stub(ScenarioGeneratorClient),
                Stub(CacheManager),
                Stub(EmpowermentService)
        )
        CycledScreenService cycledScreenService = Stub(CycledScreenService)
        service.cycledScreenService = cycledScreenService
        cycledScreenService.getNextScreen(_, _) >> null
        ScreenFinderService screenFinderService = Stub(ScreenFinderService)
        service.screenFinderService = screenFinderService
        HelperScreenRegistry screenRegistry = Stub(HelperScreenRegistry)
        service.screenRegistry = screenRegistry
        screenRegistry.getHelper(_) >> null
        ComponentService componentService = Stub(ComponentService)
        service.componentService = componentService
        DisplayReferenceService displayReferenceService = Stub(DisplayReferenceService)
        service.displayReferenceService = displayReferenceService

        when: 'Service descriptor без компонентов с флагом createOrder и isTerminal экрана false'
        screenFinderService.findScreenDescriptorByRules(_, _, _) >> screenDescriptorS2
        componentService.getScreenFields(_, _, _) >> getSimpleServiceDescriptor().getFieldComponentsForScreen(screenDescriptorS2)
        List<String> finishedScreens = List.of('s1')
        List<FieldComponent> fieldComponentList = List.of(getSimpleServiceDescriptor().getFieldComponentById('c2').get())
        DisplayRequest displayRequest = new DisplayRequest(screenDescriptorS2, fieldComponentList)
        service.skipStep(getSimpleScenarioRequest(1, serviceCode, targetCode, finishedScreens, displayRequest), serviceId)

        then:
        0 * pguOrderService.saveChoosenValuesForOrder(serviceId, targetCode, _, _)

        when: 'Service descriptor без компонентов с флагом createOrder и isTerminal экрана true'
        screenFinderService.findScreenDescriptorByRules(_, _, _) >> screenDescriptorS4
        componentService.getScreenFields(_, _, _) >> getSimpleServiceDescriptor().getFieldComponentsForScreen(screenDescriptorS4)
        finishedScreens = List.of('s1')
        fieldComponentList = List.of(getSimpleServiceDescriptor().getFieldComponentById('c4').get())
        displayRequest = new DisplayRequest(screenDescriptorS4, fieldComponentList)
        service.skipStep(getSimpleScenarioRequest(1, serviceCode, targetCode, finishedScreens, displayRequest), serviceId)

        then:
        0 * pguOrderService.saveChoosenValuesForOrder(serviceId, targetCode, _, _)

        when: 'Service descriptor с компонентами с флагом createOrder и isTerminal экрана false'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        screenFinderService.findScreenDescriptorByRules(_, _, _) >> screenDescriptorS2_with_CreateOrderFlag
        componentService.getScreenFields(_, _, _) >> getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentsForScreen(screenDescriptorS2_with_CreateOrderFlag)
        finishedScreens = List.of('s1')
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c2').get())
        displayRequest = new DisplayRequest(screenDescriptorS2_with_CreateOrderFlag, fieldComponentList)
        service.skipStep(getSimpleScenarioRequest(1, serviceCode, targetCode, finishedScreens, displayRequest), serviceId)

        then:
        0 * pguOrderService.saveChoosenValuesForOrder(serviceId, targetCode, _, _)

        when: 'Service descriptor с компонентами с флагом createOrder и isTerminal экрана true и нет orderId'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        screenFinderService.findScreenDescriptorByRules(_, _, _) >> screenDescriptorS4_with_CreateOrderFlag
        componentService.getScreenFields(_, _, _) >> getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentsForScreen(screenDescriptorS4_with_CreateOrderFlag)
        finishedScreens = List.of('s1')
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c4').get())
        displayRequest = new DisplayRequest(screenDescriptorS4_with_CreateOrderFlag, fieldComponentList)
        service.skipStep(getSimpleScenarioRequest(null, serviceCode, targetCode, finishedScreens, displayRequest), serviceId)

        then:
        def noOrderIdException = thrown(DuplicateOrderException)
        noOrderIdException.message == 'Экран терминальный, но orderId не был создан'
        0 * pguOrderService.saveChoosenValuesForOrder(serviceId, targetCode, _, _)

        when: 'Service descriptor с компонентами с флагом createOrder и isTerminal экрана true и в ApplicantAnswers пусто'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        screenFinderService.findScreenDescriptorByRules(_, _, _) >> screenDescriptorS4_with_CreateOrderFlag
        componentService.getScreenFields(_, _, _) >> getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentsForScreen(screenDescriptorS4_with_CreateOrderFlag)
        finishedScreens = List.of('s1')
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c4').get())
        displayRequest = new DisplayRequest(screenDescriptorS4_with_CreateOrderFlag, fieldComponentList)
        service.skipStep(getSimpleScenarioRequest(1, serviceCode, targetCode, finishedScreens, displayRequest), serviceId)

        then:
        0 * pguOrderService.saveChoosenValuesForOrder(serviceId, targetCode, _, _)

        when: 'Service descriptor с компонентами с флагом createOrder и isTerminal экрана true и в ApplicantAnswers нет компонента с createOrder'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        screenFinderService.findScreenDescriptorByRules(_, _, _) >> screenDescriptorS4_with_CreateOrderFlag
        componentService.getScreenFields(_, _, _) >> getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentsForScreen(screenDescriptorS4_with_CreateOrderFlag)
        finishedScreens = List.of('s1')
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c4').get())
        displayRequest = new DisplayRequest(screenDescriptorS4_with_CreateOrderFlag, fieldComponentList)
        Map<String, ApplicantAnswer> applicantAnswerMap = getCurrentValue("c1", true, "c1 value")
        service.skipStep(getSimpleScenarioRequest(1, serviceId, serviceCode, targetCode, finishedScreens, displayRequest, Collections.EMPTY_MAP, applicantAnswerMap), serviceId)

        then:
        0 * pguOrderService.saveChoosenValuesForOrder(serviceId, targetCode, _, _)

        when: 'Service descriptor с компонентами с флагом createOrder и isTerminal экрана true и в ApplicantAnswers/CurrentValue нет компонента с createOrder'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        screenFinderService.findScreenDescriptorByRules(_, _, _) >> screenDescriptorS4_with_CreateOrderFlag
        componentService.getScreenFields(_, _, _) >> getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentsForScreen(screenDescriptorS4_with_CreateOrderFlag)
        finishedScreens = List.of('s1')
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c4').get())
        displayRequest = new DisplayRequest(screenDescriptorS4_with_CreateOrderFlag, fieldComponentList)
        Map<String, ApplicantAnswer> currentValue = getCurrentValue("c2", true, "c2 value")
        Map<String, ApplicantAnswer> applicantAnswers = getCurrentValue("c1", true, "c1 value")
        service.skipStep(getSimpleScenarioRequest(1, serviceId, serviceCode, targetCode, finishedScreens, displayRequest, currentValue, applicantAnswers), serviceId)

        then:
        0 * pguOrderService.saveChoosenValuesForOrder(serviceId, targetCode, _, _)

        when: 'Service descriptor с компонентами с флагом createOrder и isTerminal экрана true и ApplicantAnswers есть компонент с createOrder и УСПЕХ'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        screenFinderService.findScreenDescriptorByRules(_, _, _) >> screenDescriptorS4_with_CreateOrderFlag
        componentService.getScreenFields(_, _, _) >> getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentsForScreen(screenDescriptorS4_with_CreateOrderFlag)
        finishedScreens = List.of('s1')
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c4').get())
        displayRequest = new DisplayRequest(screenDescriptorS4_with_CreateOrderFlag, fieldComponentList)
        currentValue = getCurrentValue("c3", true, "c3 value")
        applicantAnswers = getCurrentValue("c1", true, "c1 value")
        service.skipStep(getSimpleScenarioRequest(1, serviceId, serviceCode, targetCode, finishedScreens, displayRequest, currentValue, applicantAnswers), serviceId)

        then:
        1 * pguOrderService.saveChoosenValuesForOrder(serviceId, targetCode, _, _) >> Boolean.TRUE

        when: 'Service descriptor с компонентами с флагом createOrder и isTerminal экрана true и ApplicantAnswers есть компонент с createOrder и ЛК не смог сохранить заявление'
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptorWithCreateOrderFlag()
        screenFinderService.findScreenDescriptorByRules(_, _, _) >> screenDescriptorS4_with_CreateOrderFlag
        componentService.getScreenFields(_, _, _) >> getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentsForScreen(screenDescriptorS4_with_CreateOrderFlag)
        finishedScreens = List.of('s1')
        fieldComponentList = List.of(getSimpleServiceDescriptorWithCreateOrderFlag().getFieldComponentById('c4').get())
        displayRequest = new DisplayRequest(screenDescriptorS4_with_CreateOrderFlag, fieldComponentList)
        currentValue = getCurrentValue("c3", true, "c3 value")
        applicantAnswers = getCurrentValue("c1", true, "c1 value")
        service.skipStep(getSimpleScenarioRequest(1, serviceId, serviceCode, targetCode, finishedScreens, displayRequest, currentValue, applicantAnswers), serviceId)

        then:
        def ex = thrown(DuplicateOrderException)
        ex.message == 'Ошибка при сохранении выбранных значений для заявления'
        1 * pguOrderService.saveChoosenValuesForOrder(serviceId, targetCode, _, _) >> Boolean.FALSE
    }

    Map<String, ApplicantAnswer> getCurrentValue(String componentId, Boolean visited, String value) {
        ApplicantAnswer applicantAnswer = new ApplicantAnswer()
        applicantAnswer.setVisited(visited)
        applicantAnswer.setValue(value)
        Map<String, ApplicantAnswer> result = new HashMap<>()
        result.put(componentId, applicantAnswer)
        return result
    }
}
