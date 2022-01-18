package ru.gosuslugi.pgu.fs.component.userdata

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.userdata.PersonInnComponent
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class PersonInnComponentTest extends Specification {

    def 'PersonInn helper test' () {
        given:
        PersonInnComponent helper = new PersonInnComponent()
        expect:
        assert helper.preSetComponentValue(Mock(FieldComponent)) == ""
        assert helper.preSetComponentValue(Mock(FieldComponent), Mock(ScenarioDto)) == ""
        assert helper.getType() == ComponentType.PersonInnInput
    }
}
