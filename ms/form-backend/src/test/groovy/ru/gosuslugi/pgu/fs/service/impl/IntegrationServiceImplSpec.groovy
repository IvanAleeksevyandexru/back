package ru.gosuslugi.pgu.fs.service.impl

import ru.gosuslugi.pgu.dto.ApplicantDto
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.ScenarioResponse
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.draft.DraftClient
import ru.gosuslugi.pgu.fs.booking.service.BookingService
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import ru.gosuslugi.pgu.fs.pgu.service.OrderAttributesService
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService
import ru.gosuslugi.pgu.fs.service.DeliriumService
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import ru.gosuslugi.pgu.fs.service.IntegrationService
import ru.gosuslugi.pgu.fs.service.ParticipantService
import ru.gosuslugi.pgu.fs.service.TerrabyteService
import ru.gosuslugi.pgu.fs.sp.ServiceProcessingClient
import spock.lang.Specification

class IntegrationServiceImplSpec extends Specification {

    EmpowermentService empowermentService = Mock(EmpowermentService)

    def setup() {
        empowermentService.hasEmpowerment(_) >> true
        empowermentService.checkUserUserPermissionForSendOrder(_) >> true
    }

    def "test if delirium was called"() {
        DeliriumService deliriumService = Mock(DeliriumService)
        PguOrderService pguOrderService = Mock(PguOrderService)
        pguOrderService.findDrafts(_, _) >> Collections.emptyList()
        IntegrationService integrationService = new IntegrationServiceImpl(
                Mock(UserPersonalData),
                Mock(ServiceProcessingClient),
                deliriumService,
                pguOrderService,
                Mock(TerrabyteService),
                Mock(ParticipantService),
                Mock(DraftClient),
                empowermentService,
                Mock(BookingService),
                Mock(ErrorModalDescriptorService),
                Mock(OrderAttributesService)
        )

        when:
        integrationService.performIntegrationSteps(scenarioResponse, "105", descriptor)

        then:
        methodCalled * deliriumService.notifyScenarioEnd(_)

        where:
        scenarioResponse       |      descriptor            | methodCalled
        noneTerminalScenario() | noStagesDescriptor()       | 0
        noneTerminalScenario() | multipleStagesDescriptor() | 0
        terminalScenario()     | noStagesDescriptor()       | 0
        terminalScenario()     | multipleStagesDescriptor() | 1
        participantsScenario() | noStagesDescriptor()       | 1
        participantsScenario() | multipleStagesDescriptor() | 1
    }

    static ScenarioResponse noneTerminalScenario() {
        return new ScenarioResponse(scenarioDto: new ScenarioDto(display: new DisplayRequest(new ScreenDescriptor(isTerminal: false), Collections.emptyList())))
    }

    static ScenarioResponse terminalScenario() {
        return new ScenarioResponse(scenarioDto: new ScenarioDto(display: new DisplayRequest(new ScreenDescriptor(isTerminal: true), Collections.emptyList())))
    }

    static ScenarioResponse participantsScenario() {
        return new ScenarioResponse(scenarioDto: new ScenarioDto(
                display: new DisplayRequest(new ScreenDescriptor(isTerminal: true), Collections.emptyList()),
                participants: [
                        "100010101": new ApplicantDto()
                ]
        ))
    }

    static ServiceDescriptor multipleStagesDescriptor() {
        return new ServiceDescriptor(initScreens: [
                Applicant: [
                        Applicant: "s1",
                        FinalError: "errorScreen"
                ]
        ])
    }

    static ServiceDescriptor noStagesDescriptor() {
        return new ServiceDescriptor()
    }
}
