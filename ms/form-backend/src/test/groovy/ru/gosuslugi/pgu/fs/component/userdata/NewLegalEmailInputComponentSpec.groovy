package ru.gosuslugi.pgu.fs.component.userdata

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.service.impl.OrgContactServiceImpl
import spock.lang.Specification

class NewLegalEmailInputComponentSpec extends Specification{
    UserPersonalData userPersonalData
    NewLegalEmailInputComponent newLegalEmailInputComponent
    OrgContactServiceImpl orgContactService

    def 'check postProcess'(){
        given:
        String value = 'value'
        ScenarioDto scenarioDto = Mock(ScenarioDto)
        userPersonalData = Mock(UserPersonalData)
        orgContactService = Mock(OrgContactServiceImpl)
        newLegalEmailInputComponent = new NewLegalEmailInputComponent(orgContactService)

        when:
        newLegalEmailInputComponent.postProcess(Mock(FieldComponent),scenarioDto,value)

        then:
        1 * orgContactService.updateEmail(scenarioDto,value)
    }
}
