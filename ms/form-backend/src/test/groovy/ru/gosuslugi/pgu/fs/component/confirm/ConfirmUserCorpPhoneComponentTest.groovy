package ru.gosuslugi.pgu.fs.component.confirm

import ru.atc.carcass.security.rest.model.orgs.Org
import ru.atc.carcass.security.rest.model.person.EsiaRole
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException
import spock.lang.Specification

class ConfirmUserCorpPhoneComponentTest extends Specification {
    private static final String PHONE = "88005553535"
    private static final String EMPTY_PHONE_WARRING = "Не обнаружен подтвержденный телефонный номер для организации. Добавьте новый номер через личный кабинет с помощью кнопки \"Редактировать\""

    ConfirmUserCorpPhoneComponent confirmUserCorpPhoneComponent
    UserOrgData userOrgData

    void setup() {
        userOrgData = Mock(UserOrgData)
        confirmUserCorpPhoneComponent = new ConfirmUserCorpPhoneComponent(userOrgData)
    }

    def "Test exception for not org user in getInitialValue"() {
        given:
        ScenarioDto scenarioDto = new ScenarioDto()
        FieldComponent fieldComponent = new FieldComponent()

        when:
        userOrgData.getOrg() >> null
        confirmUserCorpPhoneComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        thrown(FormBaseWorkflowException)
    }

    def "Test correct getInitialValue"() {
        given:
        ScenarioDto scenarioDto = new ScenarioDto()
        FieldComponent fieldComponent = new FieldComponent()
        EsiaRole esiaRole = new EsiaRole()
        esiaRole.setPhone(PHONE)

        when:
        userOrgData.getOrg() >> new Org()
        userOrgData.getOrgRole() >> esiaRole
        ComponentResponse response = confirmUserCorpPhoneComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        response.get() == PHONE
    }

    def "Test wrong validate"() {
        given:
        ScenarioDto scenarioDto = new ScenarioDto()
        FieldComponent fieldComponent = new FieldComponent()
        Map<String, String> incorrectAnswers = new HashMap<>()

        //test preValidate()
        when:
        confirmUserCorpPhoneComponent.preValidate(ComponentResponse.empty(), fieldComponent, scenarioDto)

        then:
        scenarioDto.getErrors().get(fieldComponent.getId()) == EMPTY_PHONE_WARRING

        //test validateAfterSubmit()
        when:
        confirmUserCorpPhoneComponent.validateAfterSubmit(incorrectAnswers, fieldComponent.getId(), PHONE)

        then:
        incorrectAnswers.get(fieldComponent.getId()) == EMPTY_PHONE_WARRING
    }

    def "Test correct validate"() {
        given:
        ScenarioDto scenarioDto = new ScenarioDto()
        FieldComponent fieldComponent = new FieldComponent()
        Map<String, String> incorrectAnswers = new HashMap<>()
        EsiaRole esiaRole = new EsiaRole()
        esiaRole.setPhone(PHONE)

        //test preValidate()
        when:
        confirmUserCorpPhoneComponent.preValidate(ComponentResponse.of(PHONE), fieldComponent, scenarioDto)

        then:
        scenarioDto.getErrors().isEmpty()

        //test validateAfterSubmit()
        when:
        userOrgData.getOrgRole() >> esiaRole
        confirmUserCorpPhoneComponent.validateAfterSubmit(incorrectAnswers, fieldComponent.getId(), PHONE)

        then:
        incorrectAnswers.isEmpty()
    }
}
