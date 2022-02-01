package ru.gosuslugi.pgu.fs.component.confirm

import org.assertj.core.util.Lists
import ru.atc.carcass.security.rest.model.EsiaContact
import ru.atc.carcass.security.rest.model.orgs.Org
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException
import spock.lang.Specification

import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_PHONE_TYPE_ATTR
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.ConfirmLegalPhone

class ConfirmLegalPhoneComponentTest extends Specification {
    ConfirmLegalPhoneComponent emptyDataConfirmLegalPhoneComponent
    ConfirmLegalPhoneComponent confirmLegalPhoneComponent

    FieldComponent fieldComponent
    ScenarioDto scenarioDto

    private static final String KEY = "key"
    private static final String TEST_PHONE_NUMBER = "+7(777)7777777"
    private static final String EMPTY_PHONE_WARRING = "Не обнаружен подтвержденный телефонный номер для организации. Добавьте новый номер через личный кабинет с помощью кнопки \"Редактировать\""

    void setup() {
        fieldComponent = new FieldComponent(id: '1', type: ConfirmLegalPhone)
        scenarioDto = new ScenarioDto()

        UserOrgData emptyUserOrgData = new UserOrgData()
        emptyUserOrgData.setContacts(Lists.emptyList())
        emptyDataConfirmLegalPhoneComponent = new ConfirmLegalPhoneComponent(emptyUserOrgData)

        UserOrgData userOrgData = new UserOrgData()
        userOrgData.setOrg(new Org())
        userOrgData.setContacts(List.of([type: ORG_PHONE_TYPE_ATTR, vrfStu: 'VERIFIED', value: TEST_PHONE_NUMBER] as EsiaContact))
        confirmLegalPhoneComponent = new ConfirmLegalPhoneComponent(userOrgData)
    }

    def "Test getType returns a proper value"() {
        expect:
        confirmLegalPhoneComponent.getType() == ConfirmLegalPhone
    }

    def "Test getInitialValue: throw exception on empty UserOrgData.org value"() {
        when:
        emptyDataConfirmLegalPhoneComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        def exception = thrown(FormBaseWorkflowException)
        exception.getMessage() == ConfirmLegalDataComponent.USER_TYPE_ERROR
    }

    def "Test getInitialValue: returns correct phone number"() {
        when:
        def response = confirmLegalPhoneComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        response.get() == TEST_PHONE_NUMBER
    }

    def "Test preValidate: error message on empty initial value"() {
        when:
        emptyDataConfirmLegalPhoneComponent.preValidate(ComponentResponse.empty(), fieldComponent, scenarioDto)

        then:
        scenarioDto.getErrors() != null
        scenarioDto.getErrors().size() == 1
        scenarioDto.getErrors().get(fieldComponent.getId()) == EMPTY_PHONE_WARRING
    }

    def "Test preValidate: no validation errors on filled initial value"() {
        when:
        confirmLegalPhoneComponent.preValidate(ComponentResponse.of(TEST_PHONE_NUMBER), fieldComponent, scenarioDto)

        then:
        scenarioDto.getErrors() == null || scenarioDto.getErrors().isEmpty()
    }

    def "Test validateAfterSubmit: creates error message on empty UserOrgData org phone value"() {
        given:
        def incorrectAnswers = new HashMap<String, String>()

        when:
        emptyDataConfirmLegalPhoneComponent.validateAfterSubmit(incorrectAnswers, KEY, TEST_PHONE_NUMBER)

        then:
        incorrectAnswers.size() == 1
        incorrectAnswers.get(KEY) == EMPTY_PHONE_WARRING
    }

    def "Test validateAfterSubmit: no validation errors on filled UserOrgData org phone"() {
        given:
        def incorrectAnswers = new HashMap<String, String>()

        when:
        confirmLegalPhoneComponent.validateAfterSubmit(incorrectAnswers, KEY, TEST_PHONE_NUMBER)

        then:
        incorrectAnswers.size() == 0
    }
}
