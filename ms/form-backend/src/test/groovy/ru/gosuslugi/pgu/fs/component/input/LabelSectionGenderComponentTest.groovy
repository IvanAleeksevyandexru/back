package ru.gosuslugi.pgu.fs.component.input

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.gender.LabelSectionGenderComponent
import spock.lang.Specification

class LabelSectionGenderComponentTest extends Specification{

    @SuppressWarnings("GroovyAccessibility")
    def 'GLabel section helper test' () {
        given:
        LabelSectionGenderComponent helper = new LabelSectionGenderComponent()
        expect:
        helper.getInitialValue(Mock(FieldComponent), Mock(ScenarioDto)).get() == null
        helper.getType() == ComponentType.GLabelSection
        helper.getTargetComponentType() == ComponentType.LabelSection
    }
}
