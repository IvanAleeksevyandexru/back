package unit.ru.gosuslugi.pgu.fs.service.impl

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.draft.DraftClient
import ru.gosuslugi.pgu.draft.model.DraftHolderDto
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.ScenarioRequest
import ru.gosuslugi.pgu.dto.ScenarioResponse
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.ScreenRule
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry
import ru.gosuslugi.pgu.fs.common.service.ComponentService
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService
import ru.gosuslugi.pgu.fs.common.service.impl.AnswerValidationServiceImpl
import ru.gosuslugi.pgu.fs.descriptor.SubDescriptorService
import ru.gosuslugi.pgu.fs.esia.EsiaCacheService
import ru.gosuslugi.pgu.fs.service.impl.MainScreenServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.SubScreenServiceImpl
import spock.lang.Specification

class SubScreenServiceImplSpec extends Specification {

    static def serviceId = '1'
    static def orderId = 1L

    SubScreenServiceImpl service
    MainDescriptorService mainDescriptorServiceMock

    def setup() {
        DraftClient draftClientMock = Mock(DraftClient) {
            it.getDraftById(_ as Long, _ as Long, _ as Long) >> { [body: [] as ScenarioDto] as DraftHolderDto }
        }

        SubDescriptorService subDescriptorServiceMock = Mock(SubDescriptorService) {
            it.getServiceDescriptor(serviceId) >> { getSimpleServiceDescriptor() }
        }

        HelperScreenRegistry screenRegistry = Mock(HelperScreenRegistry) {
            it.getHelper(_ as ScreenType) >> { null }
        }

        ComponentService componentService = Mock(ComponentService) {
            it.getScreenFields(_ as ScreenDescriptor, _ as ScenarioDto, _ as ServiceDescriptor) >> { [] }
        }

        mainDescriptorServiceMock = Mock(MainDescriptorService)

        service = new SubScreenServiceImpl(
                subDescriptorServiceMock,
                draftClientMock,
                Stub(UserPersonalData),
                Stub(EsiaCacheService),
                Stub(MainScreenServiceImpl),
                mainDescriptorServiceMock
        )

        service.componentService = componentService
        service.screenRegistry = screenRegistry
        service.answerValidationService = new AnswerValidationServiceImpl(Mock(ComponentService), Mock(ComponentRegistry))
        service.displayReferenceService = Stub(DisplayReferenceService)
    }

    def 'Can go back to the previous step'() {
        given:
        ScenarioResponse response

        when: 'Steps back = 1'
        response = service.getPrevScreen(getSimpleScenarioRequest(), serviceId, 1)

        then:
        response.isInternalScenario
        response.scenarioDto.display.id == 's3'

        when: 'Steps back = 3'
        response = service.getPrevScreen(getSimpleScenarioRequest(), serviceId, 3)

        then:
        response.isInternalScenario
        response.scenarioDto.display.id == 's1'

        when: 'Back to init screen'
        response = service.getPrevScreen(getSimpleScenarioRequest(), serviceId, 100)

        then:
        !response.isInternalScenario
    }

    def 'Can get service descriptor for overrated components'() {
        given:
        def descriptorWithOverratedComponents = 'descriptorWithOverratedComponents'
        def overrideComponent = [id: 'c1', name: 'overrated name'] as FieldComponent
        def scenario = getSimpleScenarioRequest().scenarioDto
        scenario.parentServiceId = parentServiceId

        when:
        mainDescriptorServiceMock.getServiceDescriptor(descriptorWithOverratedComponents) >> { [overrideSubServiceComponents: ['1': [overrideComponent]]] as ServiceDescriptor }
        mainDescriptorServiceMock.getServiceDescriptor(_ as String) >> { [] as ServiceDescriptor }

        def result = service.getServiceDescriptor(serviceId, scenario)
        def hasAny = result.applicationFields.stream().anyMatch({ it -> it.name == overrideComponent.name })

        then:
        hasAny == hasOverridedComponents

        where:
        parentServiceId                        || hasOverridedComponents
        null                                   || false
        'descriptorWithOverratedComponents'    || true
        'descriptorWithoutOverratedComponents' || false
    }

    static ScenarioRequest getSimpleScenarioRequest() {
        [
                callBackServiceId: serviceId,
                callBackOrderId  : orderId,
                scenarioDto      : [
                        finishedAndCurrentScreens: ['s1', 's2', 's3', 's4'],
                        display                  : [id: 's4']
                ]
        ]
    }

    static ServiceDescriptor getSimpleServiceDescriptor() {
        [
                init             : 's1',
                screens          : [
                        [id: 's1', name: 's1', type: ScreenType.INFO, componentIds: ['c1']] as ScreenDescriptor,
                        [id: 's2', name: 's2', type: ScreenType.INFO, componentIds: ['c2']] as ScreenDescriptor,
                        [id: 's3', name: 's3', type: ScreenType.INFO, componentIds: ['c3']] as ScreenDescriptor,
                        [id: 's4', name: 's4', type: ScreenType.INFO, componentIds: ['c4']] as ScreenDescriptor
                ],
                applicationFields: [
                        [id: 'c1', type: ComponentType.InfoScr] as FieldComponent,
                        [id: 'c2', type: ComponentType.InfoScr] as FieldComponent,
                        [id: 'c3', type: ComponentType.InfoScr] as FieldComponent,
                        [id: 'c4', type: ComponentType.InfoScr] as FieldComponent
                ],
                screenRules      : [
                        s1: [[conditions: [], nextDisplay: 's2'] as ScreenRule],
                        s2: [[conditions: [], nextDisplay: 's3'] as ScreenRule],
                        s3: [[conditions: [], nextDisplay: 's4'] as ScreenRule]
                ],
                parameters       : [param1: 'value1']
        ]
    }
}
