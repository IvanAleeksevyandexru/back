package ru.gosuslugi.pgu.fs.component.input

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.gender.QuestionScrGenderComponent
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class QuestionScrGenderComponentTest extends Specification {

    def 'GQuestion no NPE on nullable attrs' () {
        setup:
        QuestionScrGenderComponent genderHelper  = new QuestionScrGenderComponent(userPersonalData: new UserPersonalData(person: new Person(gender: 'M')))
        FieldComponent component = new FieldComponent(type: ComponentType.GQuestionScr)
        ScenarioDto scenarioDto = new ScenarioDto()

        when:
        genderHelper.preSetComponentValue(component, scenarioDto)
        genderHelper.processGender(component, scenarioDto)

        then:
        notThrown(NullPointerException.class)
    }

    def 'GQuestion scr test' () {
        given:
        QuestionScrGenderComponent scr = new QuestionScrGenderComponent()
        FieldComponent fieldComponent = new FieldComponent(
                type: ComponentType.PhoneNumberConfirmCodeInput,
                label: 'reference_to_mail',
                arguments: [
                        refs: new ArrayList<String>()
                ]
        )
        ScenarioDto scenarioDto = new ScenarioDto()

        expect:
        scr.preSetComponentValue(fieldComponent) != null
        scr.preSetComponentValue(fieldComponent, scenarioDto) != null
        scr.getType() == ComponentType.GQuestionScr
    }
}
