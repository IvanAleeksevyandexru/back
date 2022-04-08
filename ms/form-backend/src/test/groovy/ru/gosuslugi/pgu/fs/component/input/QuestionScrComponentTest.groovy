package ru.gosuslugi.pgu.fs.component.input

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.input.QuestionScrComponent
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.impl.ComponentReferenceServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.UserCookiesServiceImpl
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import spock.lang.Specification

class QuestionScrComponentTest extends Specification {
    JsonProcessingService jsonProcessingService
    QuestionScrComponent component
    LinkedValuesService linkedValuesService
    ComponentReferenceService componentReferenceService

    def setup() {
        jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.objectMapper)
        linkedValuesService = ComponentTestUtil.getLinkedValuesService(jsonProcessingService)
        componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, new UserCookiesServiceImpl(), linkedValuesService)
        component = new QuestionScrComponent()
        component.linkedValuesService = linkedValuesService
        component.componentReferenceService = componentReferenceService
        component.jsonProcessingService = jsonProcessingService
    }
/*
    def "PreSetComponentValue"() {
        given:
        FieldComponent fieldComponent = jsonProcessingService.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-fieldInit.json"), FieldComponent.class)
        ScenarioDto scenarioDto = jsonProcessingService.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-scenarioDto.json"), ScenarioDto.class)

        when:
        component.process(fieldComponent, scenarioDto, null)
        println fieldComponent.getAttrs()

        then:
        ((List)fieldComponent.getAttrs().get("actions")).size() == 4
        ((List)fieldComponent.getAttrs().get("actions")) == [
                [label:"Ваши данные", value:"Ваши данные", type:"nextStep", action:"getNextScreen"],
                [action:"getNextScreen", label:"Данные ребёнка (Иван)", value:"73cfeeb9-227c-4f78-b0b3-fe66d2edcad9", type:"nextStep"],
                [action:"getNextScreen", label:"Данные ребёнка (Ивана)", value:"a27ca7d6-765b-4e0c-96e1-8afeb180c7ad", type:"nextStep"],
                [label:"Реф не в цикле (Один)", value:"Реф не в цикле", type:"nextStep", action:"getNextScreen"]
        ]

    }*/
}
