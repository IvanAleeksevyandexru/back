package ru.gosuslugi.pgu.fs.service

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.ScenarioRequest
import ru.gosuslugi.pgu.dto.ScenarioResponse
import ru.gosuslugi.pgu.draft.DraftClient
import ru.gosuslugi.pgu.fs.FormServiceApp
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService
import ru.gosuslugi.pgu.sd.storage.ServiceDescriptorClient
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

// todo дописать как вернуться настраиваемые пропуски экранов
@Ignore
@SpringBootTest(classes = FormServiceApp.class)
class AbstractScreenServiceTest extends Specification {

    @SpringBean private ServiceDescriptorClient serviceDescriptorClient = Mock()
    @SpringBean private DraftClient draftClient = Mock()
    @SpringBean private PguOrderService pguOrderService = Mock()

    @Autowired
    MainScreenService screenService

    def "getNextScreen with screen skipping"() {
        given:
        String json = Files.readString(Paths.get(getClass().getClassLoader().getResource("SkipScreen.json").toURI()))
        serviceDescriptorClient.getServiceDescriptor(_ as String) >> json

        when:
        ScenarioRequest request = getScenarioRequestForNextStep("1")
        ScenarioResponse response = screenService.getNextScreen(request, "serviceId")
        then:
        response.getScenarioDto().getDisplay().getId() == "SkipScreenId"

        when:
        request = getScenarioRequestForNextStep("2")
        response = screenService.getNextScreen(request, "serviceId")
        then:
        response.getScenarioDto().getDisplay().getId() == "AfterSkipScreenId"
        response.getScenarioDto().getApplicantAnswers().get("SkipComponentId1").getValue() == "Дефолтное значение"
        response.getScenarioDto().getApplicantAnswers().get("SkipComponentId2").getValue().isEmpty()
    }

    def "getPrevScreen with screen skipping"() {
        given:
        String json = Files.readString(Paths.get(getClass().getClassLoader().getResource("SkipScreen.json").toURI()))
        serviceDescriptorClient.getServiceDescriptor(_ as String) >> json

        when:
        ScenarioRequest request = getScenarioRequestForPrevStep("1")
        ScenarioResponse response = screenService.getPrevScreen(request, "serviceId")
        then:
        response.getScenarioDto().getDisplay().getId() == "SkipScreenId"

        when:
        request = getScenarioRequestForPrevStep("2")
        response = screenService.getPrevScreen(request, "serviceId")
        then:
        response.getScenarioDto().getDisplay().getId() == "BeforeSkipScreenId"
        !response.getScenarioDto().getApplicantAnswers().containsKey("SkipComponentId1")
        !response.getScenarioDto().getApplicantAnswers().containsKey("SkipComponentId2")
    }

    def getScenarioRequestForNextStep(String conditionAnswer) {
        ScenarioDto scenarioDto = new ScenarioDto()
        scenarioDto.getFinishedAndCurrentScreens().add("s1")
        scenarioDto.getFinishedAndCurrentScreens().add("BeforeSkipScreenId")
        ApplicantAnswer w1 = new ApplicantAnswer()
        w1.setVisited(true)
        w1.setValue("")
        scenarioDto.getApplicantAnswers().put("w1", w1)
        ApplicantAnswer q1 = new ApplicantAnswer()
        q1.setVisited(true)
        q1.setValue(conditionAnswer)
        scenarioDto.getCurrentValue().put("q1", q1)
        DisplayRequest display = new DisplayRequest()
        display.setId("BeforeSkipScreenId")
        scenarioDto.setDisplay(display)
        ScenarioRequest request = Mock()
        request.getScenarioDto() >> scenarioDto
        return request
    }

    def getScenarioRequestForPrevStep(String conditionAnswer) {
        ScenarioDto scenarioDto = new ScenarioDto()
        scenarioDto.getFinishedAndCurrentScreens().add("s1")
        scenarioDto.getFinishedAndCurrentScreens().add("BeforeSkipScreenId")
        scenarioDto.getFinishedAndCurrentScreens().add("SkipScreenId")
        scenarioDto.getFinishedAndCurrentScreens().add("AfterSkipScreenId")
        ApplicantAnswer w1 = new ApplicantAnswer()
        w1.setVisited(true)
        w1.setValue("")
        scenarioDto.getApplicantAnswers().put("w1", w1)
        ApplicantAnswer q1 = new ApplicantAnswer()
        q1.setVisited(true)
        q1.setValue(conditionAnswer)
        scenarioDto.getApplicantAnswers().put("q1", q1)
        ApplicantAnswer skipComponentId1 = new ApplicantAnswer()
        skipComponentId1.setVisited(true)
        skipComponentId1.setValue("Значение SkipComponentId1")
        scenarioDto.getApplicantAnswers().put("SkipComponentId1", skipComponentId1)
        ApplicantAnswer skipComponentId2 = new ApplicantAnswer()
        skipComponentId2.setVisited(true)
        skipComponentId2.setValue("")
        scenarioDto.getApplicantAnswers().put("SkipComponentId2", skipComponentId2)
        ApplicantAnswer w2 = new ApplicantAnswer()
        w2.setVisited(true)
        w2.setValue("")
        scenarioDto.getCurrentValue().put("w2", w2)
        DisplayRequest display = new DisplayRequest()
        display.setId("AfterSkipScreenId")
        scenarioDto.setDisplay(display)
        ScenarioRequest request = Mock()
        request.getScenarioDto() >> scenarioDto
        return request
    }
}
