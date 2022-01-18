package ru.gosuslugi.pgu.fs.helper.impl

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType
import spock.lang.Specification

class GComponentScreenHelperTest extends Specification {

    def 'GComponent screen helper test' () {
        given:
        GComponentScreenHelper helper = new GComponentScreenHelper(userPersonalData: new UserPersonalData(person: new Person(gender: 'М')))
        ScreenDescriptor descriptor = new ScreenDescriptor()
        descriptor.setHeader('')
        ScenarioDto scenarioDto = new ScenarioDto()
        expect:
        assert helper.processScreen(descriptor, scenarioDto) != null
        assert helper.getType() == ScreenType.GCOMPONENT
    }

    def 'testType' () {
        given:
        GComponentScreenHelper helper = new GComponentScreenHelper()

        expect:
        helper != null
        helper.getType() == ScreenType.GCOMPONENT
        helper.getNewType() == ScreenType.COMPONENT
    }
}
