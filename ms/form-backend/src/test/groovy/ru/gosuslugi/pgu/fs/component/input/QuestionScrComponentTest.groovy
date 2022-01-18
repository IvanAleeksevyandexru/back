package ru.gosuslugi.pgu.fs.component.input

import com.jayway.jsonpath.DocumentContext
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.components.descriptor.attr_factory.AttrsFactory
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.fs.common.component.BaseComponent
import ru.gosuslugi.pgu.fs.common.component.input.QuestionScrComponent
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.impl.ComponentReferenceServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.UserCookiesServiceImpl
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class QuestionScrComponentTest extends Specification {

    @Shared
    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())

    @Shared
    LinkedValuesService linkedValuesService = new LinkedValuesService() {
        @Override
        void fillLinkedValues(FieldComponent fieldComponent, ScenarioDto scenarioDto, DocumentContext... externalContexts) {

        }

        @Override
        void fillLinkedValues(DisplayRequest displayRequest, ScenarioDto scenarioDto) {

        }

        @Override
        String getValue(LinkedValue linkedValue, ScenarioDto scenarioDto, AttrsFactory attrsFactory, DocumentContext... externalContexts) {
            return null
        }
    }
    @Shared
    ComponentReferenceService componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, new UserCookiesServiceImpl(), linkedValuesService)

    @Shared
    BaseComponent<String> component = new QuestionScrComponent(componentReferenceService)

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

    }
}
