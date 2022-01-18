package unit.ru.gosuslugi.pgu.fs.component.logic

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.logic.BarbarbokRestCallComponent
import ru.gosuslugi.pgu.fs.component.logic.RestCallComponent
import ru.gosuslugi.pgu.fs.service.BackRestCallService
import spock.lang.Ignore
import spock.lang.Specification

//@Ignore
// TODO: дописать
class BarbarbokRestCallComponentSpec extends Specification {

    def test() {
        given:
        def component = new BarbarbokRestCallComponent(
                Mock(RestCallComponent),
                Mock(BackRestCallService),
                Mock(UserPersonalData),
                'https://gosuslugi.ru'
        )
        expect:
        component.getType() == ComponentType.BarbarbokRestCall
    }
}
