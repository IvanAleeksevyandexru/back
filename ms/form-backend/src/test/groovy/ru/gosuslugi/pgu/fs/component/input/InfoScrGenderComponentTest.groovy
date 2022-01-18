package ru.gosuslugi.pgu.fs.component.input

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.gender.InfoScrGenderComponent
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class InfoScrGenderComponentTest extends Specification {

    def 'GInfo scr helper test' () {
        given:
        InfoScrGenderComponent helper = new InfoScrGenderComponent()
        expect:
        assert helper.preSetComponentValue(Mock(FieldComponent), Mock(ScenarioDto)) != null
        assert helper.getType() == ComponentType.GInfoScr
    }
}
