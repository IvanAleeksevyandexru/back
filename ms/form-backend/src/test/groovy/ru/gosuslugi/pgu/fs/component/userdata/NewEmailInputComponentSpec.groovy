package ru.gosuslugi.pgu.fs.component.userdata

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.esia.impl.EsiaRestContactDataClientImpl
import ru.gosuslugi.pgu.fs.service.PersonContactService
import ru.gosuslugi.pgu.fs.service.impl.OrgContactServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.PersonContactServiceImpl
import spock.lang.Specification

class NewEmailInputComponentSpec extends Specification{
    PersonContactService personContactService
    EsiaRestContactDataClientImpl esiaRestContactDataClient
    UserPersonalData userPersonalData
    NewEmailInputComponent newEmailInputComponent
    OrgContactServiceImpl orgContactService


    def 'check validateAfterSubmit' (){
        given:
        Map<String,String> incorrectAnswers = new HashMap()
        String email = exMail
        esiaRestContactDataClient = Mock(EsiaRestContactDataClientImpl){
            checkEmail('FalseMail@mail.ru') >> true
            checkEmail('TrueMail@mail.ru') >> false
        }
        personContactService = new PersonContactServiceImpl(esiaRestContactDataClient,userPersonalData)
        newEmailInputComponent = new NewEmailInputComponent(personContactService,Mock(OrgContactServiceImpl),userPersonalData)

        when:
        newEmailInputComponent.validateAfterSubmit(incorrectAnswers,'Err',exMail)

        then:
        incorrectAnswers.get('Err') == expectRes

        where:
        exMail              |     expectRes
        'TrueMail@mail.ru'  |     null
        'FalseMail@mail.ru' |     'Адрес электронной почты уже используется в другой учётной записи'
    }

    def 'check postProcess'(){
        given:
        String value = 'value'
        ScenarioDto scenarioDto = Mock(ScenarioDto)
        userPersonalData = Mock(UserPersonalData){
            getOrgId() >> orgId
        }
        orgContactService = Mock(OrgContactServiceImpl)
        personContactService = Mock(PersonContactService)
        newEmailInputComponent = new NewEmailInputComponent(personContactService, orgContactService,userPersonalData)

        when:
        newEmailInputComponent.postProcess(Mock(FieldComponent),scenarioDto,value)

        then:
        expectCount1 * personContactService.updateUserEmail(scenarioDto,value)
        expectCount2 * orgContactService.updateEmail(scenarioDto,value)

        where:
        orgId   || expectCount1 || expectCount2
        1       ||      0       ||      1
        null    ||      1       ||      0
    }
}
