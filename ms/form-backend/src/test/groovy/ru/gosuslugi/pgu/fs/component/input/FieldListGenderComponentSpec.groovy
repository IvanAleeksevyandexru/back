package ru.gosuslugi.pgu.fs.component.input

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.component.gender.FieldListGenderComponent
import spock.lang.Specification

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.GFieldList

class FieldListGenderComponentSpec extends Specification {

    def fieldComponent = new FieldComponent(
            id: "id",
            type: GFieldList,
            attrs: [
                    "fieldGroups": [
                            [
                                    "groupName": "Test Group",
                                    "needDivider": true,
                                    "fields": [
                                            [
                                                    "label": "[\"ml\",\"fl\"]",
                                                    "value": "[\"mv\",\"fv\"]",
                                                    "fieldName": "g1.f1.fieldName",
                                                    "rank": "g1.f1.rank"
                                            ]
                                    ]
                            ]
                    ]
            ]
    )

    def scenario = new ScenarioDto()

    def 'returns correct ComponentType'() {
        given:
        def component = new FieldListGenderComponent()

        expect:
        component.type == GFieldList
    }

    def 'fails without userPersonalData'() {
        given:
        def component = new FieldListGenderComponent()

        when:
        component.processGender(fieldComponent, scenario)

        then:
        thrown Exception
    }

    def 'must process fieldGroups label and value'() {
        given:
        def component = new FieldListGenderComponent(
                userPersonalData: new UserPersonalData(
                        userId: 1,
                        person: new Person(
                                gender: gender
                        )
                )
        )

        when:
        component.processGender(fieldComponent, scenario)

        then:

        def field = fieldComponent.attrs.fieldGroups[0].fields[0]
        field.label == label
        field.value == value

        where:
        gender | label             | value
        "M"    | "ml"              | "mv"
        "F"    | "fl"              | "fv"
        ""     | "[\"ml\",\"fl\"]" | "[\"mv\",\"fv\"]"
        null   | "[\"ml\",\"fl\"]" | "[\"mv\",\"fv\"]"
    }

}
