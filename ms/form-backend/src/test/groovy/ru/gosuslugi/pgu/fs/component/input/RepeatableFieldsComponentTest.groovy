package ru.gosuslugi.pgu.fs.component.input

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class RepeatableFieldsComponentTest extends Specification {

    def 'Repeatable fields test' () {
        given:
        RepeatableFieldsComponent helper = new RepeatableFieldsComponent()
        expect:
        assert helper.preSetComponentValue(Mock(FieldComponent)) == ""
        assert helper.preSetComponentValue(Mock(FieldComponent), Mock(ScenarioDto)) == ""
        assert helper.getType() == ComponentType.RepeatableFields
    }
}
