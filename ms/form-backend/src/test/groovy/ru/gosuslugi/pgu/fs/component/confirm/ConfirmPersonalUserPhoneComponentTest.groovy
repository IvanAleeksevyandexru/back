package ru.gosuslugi.pgu.fs.component.confirm


import ru.atc.carcass.security.rest.model.EsiaContact
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import spock.lang.Specification

import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR

class ConfirmPersonalUserPhoneComponentTest extends Specification {

    static FieldComponent COMPONENT
    static ScenarioDto SCENARIO_DTO
    static ConfirmPersonalUserPhoneComponent USER_PHONE_COMPONENT;
    static UserPersonalData userPersonalDataWithoutData
    static UserPersonalData userPersonalDataWithData
    static String testPhoneNumber
    static EsiaContact contact1

    def setupSpec() {
        SCENARIO_DTO = new ScenarioDto()
        userPersonalDataWithoutData = Stub(UserPersonalData)
        userPersonalDataWithData = new UserPersonalData()
        USER_PHONE_COMPONENT = new ConfirmPersonalUserPhoneComponent(userPersonalDataWithoutData)
        COMPONENT = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component.json"), FieldComponent)
        testPhoneNumber = "8-800-555-35-35"
        contact1 = new EsiaContact()
        contact1.setValue(testPhoneNumber)
        contact1.setType(EsiaContact.Type.MOBILE_PHONE.getCode())
        contact1.setVrfStu(VERIFIED_ATTR)

        userPersonalDataWithData.setContacts(List.of(contact1))
    }

    def 'testGetType'() {
        expect:
        USER_PHONE_COMPONENT.getType() == ComponentType.ConfirmPersonalUserPhone
    }

    def 'testGetInitialValueResponseIsEmpty'() {
        expect:
        USER_PHONE_COMPONENT.getInitialValue(COMPONENT, SCENARIO_DTO) == ComponentResponse.empty()
    }

    def 'testGetInitialValueResponseNotEmpty'() {
        given:
        USER_PHONE_COMPONENT = new ConfirmPersonalUserPhoneComponent(userPersonalDataWithData)
        expect:
        USER_PHONE_COMPONENT.getInitialValue(COMPONENT, SCENARIO_DTO).get().equals(testPhoneNumber)
    }
}
