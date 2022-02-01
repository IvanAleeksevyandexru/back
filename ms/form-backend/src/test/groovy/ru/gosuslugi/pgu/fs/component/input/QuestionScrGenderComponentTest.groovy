package ru.gosuslugi.pgu.fs.component.input

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.gender.QuestionScrGenderComponent
import spock.lang.Specification

class QuestionScrGenderComponentTest extends Specification {

    private static QuestionScrGenderComponent QUESTION_SCR_GENDER_COMPONENT

    private static FieldComponent COMPONENT
    private static ScenarioDto SCENARIO_DTO
    private static final String SUFFIX = "-component.json"

    def setupSpec() {
        QUESTION_SCR_GENDER_COMPONENT = new QuestionScrGenderComponent()
        SCENARIO_DTO = new ScenarioDto()
    }

    def 'Get type'() {
        expect:
        QUESTION_SCR_GENDER_COMPONENT.getType() == ComponentType.GQuestionScr
    }

    def 'Test processGender: get gender values of attrs'() {
        given:
        COMPONENT = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), SUFFIX), FieldComponent)
        def userPersonalData = new UserPersonalData(
                userId: 1,
                person: new Person(
                        gender: gender
                )
        )
        QUESTION_SCR_GENDER_COMPONENT.setUserPersonalData(userPersonalData)

        when:
        QUESTION_SCR_GENDER_COMPONENT.processGender(COMPONENT, SCENARIO_DTO)

        then:
        def attrs = COMPONENT.attrs
        attrs.actions[0].label == actions
        attrs.answers[0].label == answers

        where:
        gender | actions                                  | answers
        "M"    | "act_lab_man"                            | "ans_lab_man"
        "F"    | "act_lab_woman"                          | "ans_lab_woman"
        ""     | ["act_lab_man", "act_lab_woman"] as List | ["ans_lab_man", "ans_lab_woman"] as List
        null   | ["act_lab_man", "act_lab_woman"] as List | ["ans_lab_man", "ans_lab_woman"] as List
    }
}
