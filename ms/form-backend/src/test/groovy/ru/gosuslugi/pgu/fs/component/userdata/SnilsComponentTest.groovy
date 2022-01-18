package ru.gosuslugi.pgu.fs.component.userdata

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.service.ParticipantService
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class SnilsComponentTest extends Specification {



    def "Test CheckSumValidation"() {
        given:
        def invalidSnils = '12341233'
        def validSnils = '66137556512'

        SnilsComponent helper = new SnilsComponent(Mock(PersonSearchService), Mock(JsonProcessingService), Mock(ParticipantService), Mock(UserPersonalData))
        expect:
        !helper.checkSumValidation(invalidSnils)
        helper.checkSumValidation(validSnils)
        assert helper.getType() == ComponentType.SnilsInput
    }
}
