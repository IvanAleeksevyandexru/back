package unit.ru.gosuslugi.pgu.fs.component

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService
import ru.gosuslugi.pgu.fs.component.userdata.SnilsComponent
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import ru.gosuslugi.pgu.fs.service.ParticipantService
import spock.lang.Specification

class SnilsComponentSpec extends Specification {

    SnilsComponent helper = new SnilsComponent(Mock(PersonSearchService), Mock(JsonProcessingService),
            Mock(ParticipantService), Mock(UserPersonalData), Mock(InitialValueFromService), Mock(ErrorModalDescriptorService))

    def "Test CheckSumValidation"() {
        given:
        def invalidSnils = '12341233'
        def validSnils = '66137556512'

        expect:
        !helper.checkSumValidation(invalidSnils)
        helper.checkSumValidation(validSnils)
    }
}