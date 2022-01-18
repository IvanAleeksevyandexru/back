package unit.ru.gosuslugi.pgu.fs.service.impl.process

import ru.gosuslugi.pgu.dto.*
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry
import ru.gosuslugi.pgu.fs.common.service.ComponentService
import ru.gosuslugi.pgu.fs.common.service.CycledScreenService
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl
import ru.gosuslugi.pgu.fs.service.process.PrevScreenProcess
import ru.gosuslugi.pgu.fs.service.process.impl.screen.PrevScreenProcessImpl
import spock.lang.Specification

import java.util.function.Consumer

// todo не все тест кейсы покрыты
class PrevScreenProcessImplSpec extends Specification {

    PrevScreenProcess process

    MainDescriptorService mainDescriptorServiceMock
    CycledScreenService cycledScreenServiceMock
    HelperScreenRegistry screenRegistryMock
    ComponentService componentServiceMock
    FormScenarioDtoServiceImpl scenarioDtoServiceMock
    ServiceDescriptor serviceDescriptorMock
    ComponentRegistry componentRegistry
    String serviceId = '1'

    def setup() {
        mainDescriptorServiceMock = Mock(MainDescriptorService)
        cycledScreenServiceMock = Mock(CycledScreenService)
        screenRegistryMock = Mock(HelperScreenRegistry)
        componentServiceMock = Mock(ComponentService)
        scenarioDtoServiceMock = Mock(FormScenarioDtoServiceImpl)
        serviceDescriptorMock = Mock(ServiceDescriptor)
        componentRegistry = Mock(ComponentRegistry)

        process = new PrevScreenProcessImpl(
                mainDescriptorServiceMock,
                cycledScreenServiceMock,
                screenRegistryMock,
                componentServiceMock,
                Stub(DisplayReferenceService),
                scenarioDtoServiceMock,
                componentRegistry)
    }

    def 'Check only init screen was show'() {
        given:
        def result

        when:
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(finishedAndCurrentScreens: ['s1'])))
        result = process.onlyInitScreenWasShow()

        then:
        result

        when:
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(finishedAndCurrentScreens: ['s1', 's2'])))
        result = process.onlyInitScreenWasShow()

        then:
        !result
    }

    def 'Can get init screen'() {
        given:
        def result

        when:
        scenarioDtoServiceMock.createInitScenario(null, _ as InitServiceDto) >> new ScenarioDto(currentUrl: 'test')
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto())).getInitScreen()
        result = process.start()

        then:
        result.scenarioDto.currentUrl == 'test'
    }

    def 'Check is cycled screen'() {
        given:
        def result

        when:
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(finishedAndCurrentScreens: ['s1'], display: new DisplayRequest(id: 'c1'))))
        result = process.isCycledScreen()

        then:
        result

        when:
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(finishedAndCurrentScreens: ['s1'], display: new DisplayRequest(id: 's1'))))
        result = process.isCycledScreen()

        then:
        !result
    }

    def 'Can set response if prev screen cycled'() {
        given:
        def result

        when:
        cycledScreenServiceMock.getPrevScreen(_ as ScenarioRequest, _ as String) >> new ScenarioResponse()
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto())).setResponseIfPrevScreenCycled()
        result = process.start()

        then:
        result
    }

    def 'Can set response if prev screen not cycled'() {
        given:
        def result

        when:
        cycledScreenServiceMock.getPrevScreen(_ as ScenarioRequest, _ as String) >> null
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto())).setResponseIfPrevScreenCycled()
        result = process.start()

        then:
        !result
    }

    def 'Check response is null if response null'() {
        given:
        def result

        when:
        cycledScreenServiceMock.getPrevScreen(_ as ScenarioRequest, _ as String) >> null
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto()))
                .setResponseIfPrevScreenCycled()
        result = process.checkResponseIsNull()

        then:
        result
    }

    def 'Check response is null if response not null'() {
        given:
        def result

        when:
        cycledScreenServiceMock.getPrevScreen(_ as ScenarioRequest, _ as String) >> null
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto()))
                .setResponseIfPrevScreenCycled()
        result = process.checkResponseIsNull()

        then:
        result
    }

    def 'Can calculate next screen'() {
        given:
        def result

        when:
        serviceDescriptorMock.getScreenDescriptorById(_ as String) >> new ScreenDescriptor()
        mainDescriptorServiceMock.getServiceDescriptor(_ as String) >> new ServiceDescriptor(
                screens: [new ScreenDescriptor(id: 's1', type: ScreenType.INFO, componentIds: ['q1'])],
                applicationFields: [new FieldComponent(id: 'q1')])
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(
                finishedAndCurrentScreens: ['s1', 's2'],
                display: new DisplayRequest(id: 's2'), currentValue: [v1: new ApplicantAnswer(visited: true, value: 'v1')])))
        result = process
                .execute({process.putAnswersToCache()} as Consumer)
                .execute({process.removeScreenFromFinished()} as Consumer)
                .execute({process.removeAnswersFromDto()} as Consumer)
                .execute({process.calculateNextScreen()} as Consumer)
                .start()

        then:
        result.scenarioDto.display.id == 's1'
        result.scenarioDto.finishedAndCurrentScreens == ['s1']
    }

    def 'Can skip EMPTY screens'() {
        given:
        def result

        when:
        serviceDescriptorMock.getScreenDescriptorById(_ as String) >> new ScreenDescriptor()
        mainDescriptorServiceMock.getServiceDescriptor(_ as String) >> new ServiceDescriptor(
                screens: [new ScreenDescriptor(id: 's1', type: ScreenType.INFO, componentIds: ['q1']),
                          new ScreenDescriptor(id: 's2', type: ScreenType.EMPTY, componentIds: ['q2']),
                          new ScreenDescriptor(id: 's3', type: ScreenType.EMPTY, componentIds: ['q3'])
                ],
                applicationFields: [new FieldComponent(id: 'q1'), new FieldComponent(id: 'q2'), new FieldComponent(id: 'q3')])
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(
                finishedAndCurrentScreens: ['s1', 's2', 's3', 's4'],
                display: new DisplayRequest(id: 's4'), currentValue: [v1: new ApplicantAnswer(visited: true, value: 'v1')])))
        result = process
                .execute({process.putAnswersToCache()} as Consumer)
                .execute({process.removeScreenFromFinished()} as Consumer)
                .execute({process.removeAnswersFromDto()} as Consumer)
                .execute({process.calculateNextScreen()} as Consumer)
                .start()

        then:
        result.scenarioDto.display.id == 's1'
        result.scenarioDto.finishedAndCurrentScreens == ['s1']
    }

    def 'Must restore cycled applicantAnswers'() {
        given:

        def scenarioDto = new ScenarioDto(
                finishedAndCurrentScreens: ['s1'],
                cycledApplicantAnswers: [
                        currentAnswerId: 'abc',
                        answers: [
                                new CycledApplicantAnswer(
                                        id: 'q1',
                                        currentItemId: '7595395',
                                        items: [
                                                new CycledApplicantAnswerItem(
                                                        id: '7595395',
                                                        itemAnswers: [
                                                                "a1_1": new ApplicantAnswer(
                                                                        value: "a1_1_value",
                                                                        visited: false
                                                                )
                                                        ]
                                                )

                                        ]
                                )
                        ]
                ]
        )

        serviceDescriptorMock.getScreenDescriptorById(_ as String) >> new ScreenDescriptor()
        mainDescriptorServiceMock.getServiceDescriptor(_ as String) >> new ServiceDescriptor(
                screens: [
                        new ScreenDescriptor(id: 's1', type: ScreenType.INFO, componentIds: ['q1'])
                ],
                applicationFields: [
                        new FieldComponent(id: 'q1', attrs: ['isCycled': true])
                ]
        )
        process.of(serviceId, new ScenarioRequest(scenarioDto: scenarioDto))

        when:
            process.setResponseIfScreenHasCycledComponent()

        then:
            scenarioDto.cycledApplicantAnswers.currentAnswerId == 'q1'
            scenarioDto.applicantAnswers['a1_1'].value == 'a1_1_value'
    }

}
