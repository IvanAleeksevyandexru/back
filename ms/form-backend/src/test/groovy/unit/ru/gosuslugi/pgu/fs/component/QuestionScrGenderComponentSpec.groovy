package unit.ru.gosuslugi.pgu.fs.component

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.components.FieldComponentUtil
import ru.gosuslugi.pgu.dto.descriptor.Expression
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.common.component.input.InfoScrComponent
import ru.gosuslugi.pgu.fs.component.gender.QuestionScrGenderComponent
import spock.lang.Specification

class QuestionScrGenderComponentSpec extends Specification {

    QuestionScrGenderComponent component

    ComponentRegistry componentRegistry = Mock(ComponentRegistry)

    private static final String TEST_COMPONENT_ID = "da"
    private static final String MALE_TRIGGER_VALUE = "male"
    private static final String FEMALE_TRIGGER_VALUE = "female"
    private static final String LABEL_TEXT = '[\"<p>Жених</p><p>${lastName}</p><p>Адрес места регистрации</p>' +
            '<p>${addr}</p>\", \"<p>Невеста</p><p>${lastName} <p>Адрес места регистрации</p><p>${addr}</p>\"]'

    def setup() {
        component = new QuestionScrGenderComponent()
        ComponentTestUtil.setAbstractComponentGenderServices(component, componentRegistry)

        def targetComponent = new InfoScrComponent()
        componentRegistry.getComponent(_ as ComponentType) >> targetComponent
    }

    def 'Can fill component label by gender'() {
        given:
        FieldComponent fieldComponent
        ScenarioDto scenarioDto = new ScenarioDto()
        scenarioDto.setCurrentValue(Map.of())
        scenarioDto.setApplicantAnswers(getApplicantAnswers())
        scenarioDto.setGender("M")

        when: "Male"
        fieldComponent = getComponent()
        component.userPersonalData = new UserPersonalData(person: new Person(gender: 'M'), userId: 1)
        component.process(fieldComponent, scenarioDto, new ServiceDescriptor())

        then:
        component.type == ComponentType.GQuestionScr
        fieldComponent.getLabel() == "<p>Жених</p><p>Some lastname</p><p>Адрес места регистрации</p><p>Address</p>"

        when: "Female"
        fieldComponent = getComponent()
        component.userPersonalData = new UserPersonalData(person: new Person(gender: 'F'), userId: 1)
        component.process(fieldComponent, scenarioDto, new ServiceDescriptor())

        then:
        fieldComponent.label == "<p>Невеста</p><p>Some lastname <p>Адрес места регистрации</p><p>Address</p>"
    }

    private static Map<String, Object> getAttributes() {
        return Map.of(FieldComponentUtil.ACTIONS_ATTR_KEY, getActions())
    }

    private static List<Map<String, Object>> getActions() {
        return [
                ["label": "Подтвердить", "value": "Подтвердить", "type": "nextStep", "action": "getNextScreen"],
                ["label": "Подтвердить", "value": "Подтвердить", "type": "nextStep", "action": "getNextScreen"],
        ]
    }

    private static Map<String, ApplicantAnswer> getApplicantAnswers() {
        return [
                // This answers will be used to fill a label.
                "pd1": new ApplicantAnswer(value: '{"storedValues":{"lastName":"Some lastname"}}', visited: true),
                "ms1": new ApplicantAnswer(value: 'Address', visited: true),
                // The following answer contains a trigger value which controls which references will be used!
                "tr": new ApplicantAnswer(value: MALE_TRIGGER_VALUE, visited: true)
        ]
    }

    private static List<LinkedValue> getLinkedValues() {
        return [
                new LinkedValue(
                        source: 'tr', argument: 'lastName', defaultValue: 'Nothing', isJsonSource: false,
                        expressions: [
                                new Expression(when: MALE_TRIGGER_VALUE, then: '${pd1.value.storedValues.lastName}'),
                                new Expression(when: FEMALE_TRIGGER_VALUE, then: 'Female'),
                        ]
                ),
                new LinkedValue(
                        source: 'tr', argument: 'addr', defaultValue: 'No value', isJsonSource: false,
                        expressions: [
                                new Expression(when: MALE_TRIGGER_VALUE, then: '${ms1.value}'),
                                new Expression(when: FEMALE_TRIGGER_VALUE, then: 'Female Addr'),
                        ]
                ),
        ]
    }

    private static FieldComponent getComponent() {
        return new FieldComponent(
                id: TEST_COMPONENT_ID,
                type: ComponentType.GQuestionScr,
                required: false,
                value: "",
                label: LABEL_TEXT,
                attrs: getAttributes(),
                linkedValues: getLinkedValues()
        )
    }

}