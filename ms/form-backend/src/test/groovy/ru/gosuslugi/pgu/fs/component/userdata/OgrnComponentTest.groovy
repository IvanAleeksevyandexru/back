package ru.gosuslugi.pgu.fs.component.userdata

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.userdata.OgrnComponent
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class OgrnComponentTest extends Specification {

    def 'Ogrn helper test' () {
        given:
        OgrnComponent helper = new OgrnComponent()
        expect:
        assert helper.preSetComponentValue(Mock(FieldComponent)) == ""
        assert helper.preSetComponentValue(Mock(FieldComponent), Mock(ScenarioDto)) == ""
        assert helper.getType() == ComponentType.OgrnInput
    }
}
