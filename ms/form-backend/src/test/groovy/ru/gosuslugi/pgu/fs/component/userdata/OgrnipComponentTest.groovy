package ru.gosuslugi.pgu.fs.component.userdata

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.userdata.OgrnipComponent
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class OgrnipComponentTest extends Specification{

    def 'Ogrn ip helper test' () {
        given:
        OgrnipComponent helper = new OgrnipComponent()
        expect:
        assert helper.preSetComponentValue(Mock(FieldComponent)) == ""
        assert helper.preSetComponentValue(Mock(FieldComponent), Mock(ScenarioDto)) == ""
        assert helper.getType() == ComponentType.OgrnipInput
    }
}
