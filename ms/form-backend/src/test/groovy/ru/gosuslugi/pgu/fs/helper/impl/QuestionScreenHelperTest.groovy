package ru.gosuslugi.pgu.fs.helper.impl

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType
import spock.lang.Specification

class QuestionScreenHelperTest extends Specification{

    def 'GQuestion screen helper test' () {
        given:
        GQuestionScreenHelper helper = new GQuestionScreenHelper(userPersonalData: new UserPersonalData(person: new Person(gender: 'М')))
        ScreenDescriptor descriptor = new ScreenDescriptor(header: '')

        expect:
        assert helper.processScreen(descriptor, Mock(ScenarioDto)) != null
        assert helper.getType() == ScreenType.GQUESTION

    }

    def 'test type' () {
        given:
        GQuestionScreenHelper helper = new GQuestionScreenHelper(userPersonalData: new UserPersonalData(person: new Person(gender: 'М')))

        expect:
        helper != null
        helper.getType() == ScreenType.GQUESTION
    }

    def 'test new type' () {
        given:
        GQuestionScreenHelper helper = new GQuestionScreenHelper(userPersonalData: new UserPersonalData(person: new Person(gender: 'М')))

        expect:
        helper != null
        helper.getNewType() == ScreenType.QUESTION
    }
}
