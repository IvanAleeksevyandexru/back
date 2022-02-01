package unit.ru.gosuslugi.pgu.fs.service.impl

import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.draft.DraftClient
import ru.gosuslugi.pgu.dto.*
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.ScreenRule
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry
import ru.gosuslugi.pgu.fs.common.service.*
import ru.gosuslugi.pgu.fs.common.service.impl.ComputeAnswerServiceImpl
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService
import ru.gosuslugi.pgu.fs.service.*
import ru.gosuslugi.pgu.fs.service.impl.AdditionalAttributesHelper
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.MainScreenServiceImpl
import ru.gosuslugi.pgu.fs.service.process.ExternalScreenProcess
import ru.gosuslugi.pgu.fs.service.process.NextScreenProcess
import ru.gosuslugi.pgu.fs.service.process.PrevScreenProcess
import ru.gosuslugi.pgu.fs.service.process.impl.screen.NextScreenProcessImpl
import ru.gosuslugi.pgu.fs.service.process.impl.screen.PrevScreenProcessImpl
import ru.gosuslugi.pgu.fs.suggests.service.SuggestsService
import spock.lang.Specification

class MainScreenServiceImplSpec extends Specification {

    def serviceId = '1'

    MainScreenServiceImpl mainScreenService
    PrevScreenProcessImpl prevScreenProcess
    NextScreenProcessImpl nextScreenProcess

    MainDescriptorService mainDescriptorService
    HelperScreenRegistry screenRegistry
    FormScenarioDtoServiceImpl scenarioDtoService
    CycledScreenService cycledScreenService
    ComputeAnswerServiceImpl computeAnswerService
    ComponentRegistry componentRegistry
    AdditionalAttributesHelper additionalAttributesHelper

    def setup() {
        mainDescriptorService = Mock(MainDescriptorService)
        mainDescriptorService.getServiceDescriptor(serviceId) >> getSimpleServiceDescriptor()

        screenRegistry = Mock(HelperScreenRegistry)
        screenRegistry.getHelper(_ as ScreenType) >> null

        scenarioDtoService = Mock(FormScenarioDtoServiceImpl)
        scenarioDtoService.createInitScenario(_ as ServiceDescriptor, _ as InitServiceDto) >> new ScenarioDto(display: new DisplayRequest(id: 's1'))

        cycledScreenService = Mock(CycledScreenService)

        componentRegistry = Mock(ComponentRegistry)

        ErrorModalDescriptorService errorModalDescriptorService = Mock(ErrorModalDescriptorService)

        prevScreenProcess = new PrevScreenProcessImpl(
                mainDescriptorService,
                cycledScreenService,
                screenRegistry,
                Stub(ComponentService),
                Stub(DisplayReferenceService),
                scenarioDtoService,
                componentRegistry,
                errorModalDescriptorService
        )

        computeAnswerService = Mock(ComputeAnswerServiceImpl)

        additionalAttributesHelper = Mock(AdditionalAttributesHelper)

        EmpowermentService empowermentService = Mock(EmpowermentService)

        nextScreenProcess = new NextScreenProcessImpl(
                mainDescriptorService,
                Stub(AnswerValidationService),
                Stub(ComponentService),
                Stub(CreateOrderService),
                cycledScreenService,
                Stub(ScreenFinderService),
                Stub(DisplayReferenceService),
                screenRegistry,
                Stub(UserPersonalData),
                Stub(UserOrgData),
                Stub(PguOrderService),
                scenarioDtoService,
                Stub(IntegrationService),
                computeAnswerService,
                additionalAttributesHelper,
                Stub(SuggestsService),
                empowermentService,
                errorModalDescriptorService
        )

        mainScreenService = new MainScreenServiceImpl(
                mainDescriptorService,
                Stub(DeliriumService),
                Stub(UserPersonalData),
                Stub(PguOrderService),
                Stub(DraftClient),
                Stub(OrderInfoService),
                Stub(TransformService),
                prevScreenProcess as PrevScreenProcess,
                nextScreenProcess as NextScreenProcess,
                Stub(ExternalScreenProcess),
                Stub(ScenarioInitializerService))

        mainScreenService.screenRegistry = screenRegistry

        ComponentService componentService = Mock(ComponentService)
        componentService.getScreenFields(_ as ScreenDescriptor, _ as ScenarioDto, _ as ServiceDescriptor) >> []
        mainScreenService.componentService = componentService

        mainScreenService.displayReferenceService = Stub(DisplayReferenceService)
    }

    def 'Can go back to the previous step'() {
        given:
        ScenarioResponse response

        when: 'Steps back = 1'
        response = mainScreenService.getPrevScreen(getSimpleScenarioRequest(), serviceId, 1)

        then:
        response.scenarioDto.display.id == 's3'

        when: 'Steps back = 3'
        response = mainScreenService.getPrevScreen(getSimpleScenarioRequest(), serviceId, 3)

        then:
        response.scenarioDto.display.id == 's1'

        when: 'Back to init screen'
        response = mainScreenService.getPrevScreen(getSimpleScenarioRequest(), serviceId, 100)

        then:
        response.scenarioDto.display.id == 's1'
    }

    def 'Can go back to the previous step for cycled screens'() {
        given:
        ScenarioRequest request = getSimpleScenarioRequest()
        request.scenarioDto.display.id = 'cycled1'
        cycledScreenService.getPrevScreen(request, serviceId) >> {
            ScenarioDto scenarioDto = request.scenarioDto
            scenarioDto.display.id = scenarioDto.finishedAndCurrentScreens.getLast()
            new ScenarioResponse(scenarioDto: scenarioDto)
        }

        when:
        ScenarioResponse response = mainScreenService.getPrevScreen(request, serviceId, 3)

        then:
        response.scenarioDto.display.id == 's2'
    }

    static ServiceDescriptor getSimpleServiceDescriptor() {
        new ServiceDescriptor(
                init: 's1',
                screens: [
                        new ScreenDescriptor(id: 's1', name: 's1', type: ScreenType.INFO, componentIds: ['c1']),
                        new ScreenDescriptor(id: 's2', name: 's2', type: ScreenType.INFO, componentIds: ['c2']),
                        new ScreenDescriptor(id: 's3', name: 's3', type: ScreenType.INFO, componentIds: ['c3']),
                        new ScreenDescriptor(id: 's4', name: 's4', type: ScreenType.INFO, componentIds: ['c4'])
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
                ],
                parameters: ["param1":"value1"]
        )
    }

    static ScenarioRequest getSimpleScenarioRequest() {
        new ScenarioRequest(
                scenarioDto: new ScenarioDto(
                        finishedAndCurrentScreens: ['s1', 's2', 's3', 's4'],
                        display: new DisplayRequest(id: 's4')))
    }

}