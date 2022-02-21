package unit.ru.gosuslugi.pgu.fs.service.impl.process

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.draft.DraftClient
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.InitServiceDto
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.ScenarioRequest
import ru.gosuslugi.pgu.dto.ScenarioResponse
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto
import ru.gosuslugi.pgu.fs.service.DeliriumService
import ru.gosuslugi.pgu.fs.service.ScenarioInitializerService
import ru.gosuslugi.pgu.fs.service.process.ScreenProcess
import ru.gosuslugi.pgu.fs.service.process.impl.screen.AbstractScreenProcess
import spock.lang.Specification

import java.util.function.Consumer

// todo не все кейсы needReInitScreen/0 покрыты тестами
class AbstractScreenProcessSpec extends Specification {

    TestScreenProcess process

    DeliriumService deliriumServiceMock
    ScenarioInitializerService scenarioInitializerServiceMock
    DraftClient draftClientMock
    UserPersonalData userPersonalDataMock
    MainDescriptorService mainDescriptorServiceMock

    String serviceId = '1'

    def setup() {
        deliriumServiceMock = Mock(DeliriumService)
        scenarioInitializerServiceMock = Mock(ScenarioInitializerService)
        draftClientMock = Mock(DraftClient)
        userPersonalDataMock = Mock(UserPersonalData)
        mainDescriptorServiceMock = Mock(MainDescriptorService)

        process = new TestScreenProcess()
        process.deliriumService = deliriumServiceMock
        process.scenarioInitializerService = scenarioInitializerServiceMock
        process.draftClient = draftClientMock
        process.userPersonalData = userPersonalDataMock
        process.mainDescriptorService = mainDescriptorServiceMock
    }

    def 'Can check need reinit screen if screen descriptor not present'() {
        given:
        def result

        when:
        mainDescriptorServiceMock.getServiceDescriptor(_ as String) >> new ServiceDescriptor(screens: [])
        result = process.of(serviceId, new ScenarioRequest()).needReInitScreen()

        then:
        !result
    }

    def 'Can check need reinit screen if is not delirium scenario'() {
        given:
        def result

        when:
        mainDescriptorServiceMock.getServiceDescriptor(_ as String) >> new ServiceDescriptor(screens: [new ScreenDescriptor(id: 's1')])
        result = process.of(serviceId, new ScenarioRequest()).needReInitScreen()

        then:
        !result
    }

    def 'Can check need reinit screen if is delirium scenario and init stages contains delirium stage'() {
        given:
        def result

        when:
        deliriumServiceMock.getStage(_ as Long) >> new DeliriumStageDto(stage: 'k1')
        mainDescriptorServiceMock.getServiceDescriptor(_ as String) >> new ServiceDescriptor(screens: [new ScreenDescriptor(id: 's1', initStages: ['k1'])], initScreens: [k1: [a: 1], k2: [a: 2]])
        result = process.of(serviceId, new ScenarioRequest()).needReInitScreen()

        then:
        result
    }

    def 'Can reinit scenario'() {
        when:
        process.of(serviceId, new ScenarioRequest(scenarioDto: new ScenarioDto(orderId: 1, display: new DisplayRequest(id: 's1'))))
                .execute({process.reInitScenario()} as Consumer)
                .start()

        then:
        1 * scenarioInitializerServiceMock.getExistingScenario(_ as InitServiceDto, _ as String)
    }


    def 'Can save draft'() {
        when:
        userPersonalDataMock.getUserId() >> 1L
        mainDescriptorServiceMock.getServiceDescriptor(_ as String) >> new ServiceDescriptor(draftTtl: 1, orderTtl: 10)
        process.of(serviceId, new ScenarioRequest())
                .execute({process.saveDraft()} as Consumer)
                .start()

        then:
        1 * draftClientMock.saveDraft(_ as ScenarioDto, _ as String, _ as Long, null, _ as Integer, _ as Integer)
    }

    class TestScreenProcess extends AbstractScreenProcess<TestScreenProcess> implements ScreenProcess<TestScreenProcess> {

        MainDescriptorService mainDescriptorService

        @Override
        TestScreenProcess of(String serviceId, ScenarioRequest request) {
            super.of(serviceId, request)
            response = new ScenarioResponse(scenarioDto: new ScenarioDto(orderId: 1, display: new DisplayRequest(id: 's1')))
            return this
        }

        @Override
        MainDescriptorService getMainDescriptorService() {
            mainDescriptorService
        }

        @Override
        TestScreenProcess getProcess() {
            return this
        }
    }

}