package ru.gosuslugi.pgu.fs.component.input

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.gender.LookupGenderComponent
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class LookupGenderComponentTest extends Specification {

    def 'GLookup helper test' () {
        given:
        LookupGenderComponent helper = new LookupGenderComponent()
        expect:
        assert helper.preSetComponentValue(Mock(FieldComponent), Mock(ScenarioDto)) != null
        assert helper.getType() == ComponentType.GLookup
    }
}
