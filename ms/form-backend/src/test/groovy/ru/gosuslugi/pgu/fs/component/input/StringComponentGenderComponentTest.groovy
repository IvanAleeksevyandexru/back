package ru.gosuslugi.pgu.fs.component.input

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.gender.StringComponentGenderComponent
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class StringComponentGenderComponentTest extends Specification {

    def 'GString component helper test' () {
        given:
        StringComponentGenderComponent scr = new StringComponentGenderComponent()
        expect:
        assert scr.preSetComponentValue(Mock(FieldComponent), Mock(ScenarioDto)) != null
        assert scr.getType() == ComponentType.GStringInput
    }
}
