package ru.gosuslugi.pgu.fs.component.input

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.input.LabelSectionComponent
import spock.lang.Specification

class LabelSectionComponentTest extends Specification {

    def 'Label section helper test'() {
        given:
        LabelSectionComponent helper = new LabelSectionComponent()
        expect:
        helper.getInitialValue(Mock(FieldComponent)).get() == null
        helper.getInitialValue(Mock(FieldComponent), Mock(ScenarioDto)).get() == null
        helper.getType() == ComponentType.LabelSection
    }

    @SuppressWarnings("GroovyAccessibility")
    def 'When label section has fields'() {
        given:
        def componenet = new LabelSectionComponent()
        def fieldComponent = fieldComponent()

        when:
        componenet.preProcessCycledComponent(fieldComponent, cycledApplicantAnswerItem().getEsiaData())
        def label = fieldComponent.getLabel()

        then:
        'Получатель средств: Vasya Pupkin 41' == label
    }

    def fieldComponent() {
        [
                label: "Получатель средств:",
                attrs: [
                        fields: [
                                [fieldName: 'firstName'],
                                [fieldName: 'middleName'],
                                [fieldName: 'lastName'],
                                [fieldName: 'age']
                        ]
                ],
                value: ''
        ] as FieldComponent
    }

    def cycledApplicantAnswerItem() {
        [
                esiaData: [
                        firstName: 'Vasya',
                        lastName: 'Pupkin',
                        age: '41'
                ]
        ] as CycledApplicantAnswerItem
    }
}
