package ru.gosuslugi.pgu.fs.component.confirm


import ru.atc.carcass.security.rest.model.EsiaContact
import ru.atc.carcass.security.rest.model.orgs.Org
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException
import spock.lang.Specification

import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_EMAIL_TYPE_ATTR
import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.ConfirmLegalEmail

class ConfirmLegalEmailComponentTest extends Specification {

    private static FieldComponent fieldComponent
    private static ScenarioDto scenarioDto
    private static ConfirmLegalEmailComponent confirmLegalEmailComponent
    private static UserPersonalData userPersonalData
    private static UserOrgData userOrgData
    private static final String CORRECT_EMAIL = 'test@test.com'
    private static final String INCORRECT_EMAIL = 'test.com'
    static EsiaContact contact
    private static final String KEY = "key"
    private static final String SUFFIX = "-component.json"

    void setup(){
        scenarioDto = new ScenarioDto()
        userPersonalData = Stub(UserPersonalData)
        userOrgData = new UserOrgData()
        confirmLegalEmailComponent = new ConfirmLegalEmailComponent(userPersonalData, userOrgData)
        fieldComponent = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(ConfirmPersonalUserEmailComponentTest, SUFFIX), FieldComponent)
        contact = new EsiaContact()
        contact.setValue(CORRECT_EMAIL)
        contact.setType(EsiaContact.Type.EMAIL.getCode())
        contact.setVrfStu(VERIFIED_ATTR)
    }

    def "Chek method getType returns the correct value"() {
        given:
        confirmLegalEmailComponent = new ConfirmLegalEmailComponent(userPersonalData, userOrgData)

        expect:
        confirmLegalEmailComponent.getType() == ConfirmLegalEmail
    }

    def "GetInitialValue when userOrgData null throw FormBaseWorkflowException"(){
        given:
        userOrgData.setOrg(null)
        confirmLegalEmailComponent = new ConfirmLegalEmailComponent(userPersonalData, userOrgData)

        when:
        confirmLegalEmailComponent.getInitialValue(null,null)

        then:
        thrown(FormBaseWorkflowException)

    }

    def "GetInitialValue returns correct email"(){
        given:
        userOrgData.setOrg(new Org())
        userOrgData.setContacts(List.of([type: ORG_EMAIL_TYPE_ATTR, vrfStu: 'VERIFIED', value: CORRECT_EMAIL] as EsiaContact))
        confirmLegalEmailComponent = new ConfirmLegalEmailComponent(userPersonalData, userOrgData)
        def correctComponentResponseEmail = "test@test.com"

        when:
        def confirmLegalEmailComponent = confirmLegalEmailComponent.getInitialValue(fieldComponent,scenarioDto)

        then:
        correctComponentResponseEmail == confirmLegalEmailComponent.get()


    }

    def 'Test validateAfterSubmit: empty email'() {
        given:
        def entry = Map.entry(KEY, new ApplicantAnswer(false, ""))
        def incorrectAnswers = new HashMap<String, String>()
        confirmLegalEmailComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)
        expect:
        incorrectAnswers.get(KEY) == ConfirmLegalEmailComponent.EMPTY_EMAIL_WARRING_MSG
    }

    def 'Test validateAfterSubmit: incorrect email'() {
        given:
        def entry = Map.entry(KEY, new ApplicantAnswer(false, INCORRECT_EMAIL))
        def incorrectAnswers = new HashMap<String, String>()
        confirmLegalEmailComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)
        expect:
        incorrectAnswers.get(KEY) == ConfirmLegalEmailComponent.DEFAULT_EMAIL_ERROR_MSG
    }

    def 'Test getEmail'(){
        given:
        userOrgData.setOrg(new Org())
        userOrgData.setContacts(List.of([type: ORG_EMAIL_TYPE_ATTR, vrfStu: 'VERIFIED', value: CORRECT_EMAIL] as EsiaContact))
        def optionalCorrectEmail = Optional.ofNullable(CORRECT_EMAIL)

        when:
        def email = confirmLegalEmailComponent.getEmail()

        then:
        email == optionalCorrectEmail

    }

    def 'Test isPresentAndVerifiedEmail'(){
        given:
        userOrgData.setOrg(new Org())
        userOrgData.setContacts(List.of([type: ORG_EMAIL_TYPE_ATTR, vrfStu: 'VERIFIED', value: CORRECT_EMAIL] as EsiaContact))

        when:
        def isPresentAndVerifiedEmail = confirmLegalEmailComponent.isPresentAndVerifiedEmail('test@test.com')

        then:
        isPresentAndVerifiedEmail


    }

}

