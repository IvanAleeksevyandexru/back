package ru.gosuslugi.pgu.fs.component.userdata

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.userdata.LegalInnComponent
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class LegalInnComponentTest extends Specification {

    def 'Legal inn helper test' () {
        given:
        LegalInnComponent helper = new LegalInnComponent()
        expect:
        assert helper.preSetComponentValue(Mock(FieldComponent)) == ""
        assert helper.preSetComponentValue(Mock(FieldComponent), Mock(ScenarioDto)) == ""
        assert helper.getType() == ComponentType.LegalInnInput
    }
}
