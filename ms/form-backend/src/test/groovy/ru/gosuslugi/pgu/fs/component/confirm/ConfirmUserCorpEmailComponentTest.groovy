package ru.gosuslugi.pgu.fs.component.confirm


import ru.atc.carcass.security.rest.model.orgs.Org
import ru.atc.carcass.security.rest.model.person.EsiaRole
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import spock.lang.Specification

class ConfirmUserCorpEmailComponentTest extends Specification {

    private static final String KEY = "key"
    private static final String INCORRECT_EMAIL = 'test.com'
    private static final String SUFFIX = "-component.json"
    private static ConfirmUserCorpEmailComponent confirmUserCorpEmailComponent
    private static UserOrgData userOrgData
    UserPersonalData userPersonalDataMock
    ScenarioDto scenarioDto
    FieldComponent fieldComponent

    def setup() {
        fieldComponent = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(ConfirmPersonalUserEmailComponentTest, SUFFIX), FieldComponent)
        userPersonalDataMock = Mock(UserPersonalData)
        userPersonalDataMock.getToken() >> "token"
        userPersonalDataMock.getUserId() >> { 1000298933L }
        confirmUserCorpEmailComponent = new ConfirmUserCorpEmailComponent(userPersonalDataMock, userOrgData)
    }

    def 'Can get initial value'() {
        given:
        userOrgData = new UserOrgData()
        userOrgData.setOrg(new Org())
        EsiaRole esiaRole = new EsiaRole()
        esiaRole.setEmail("test@mail.ru")
        userOrgData.setOrgRole(esiaRole)
        confirmUserCorpEmailComponent = new ConfirmUserCorpEmailComponent(userPersonalDataMock, userOrgData)
        def correctComponentResponseEmail = "test@mail.ru"

        when:
        def confirmUserCorpEmailComponent = confirmUserCorpEmailComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        correctComponentResponseEmail == confirmUserCorpEmailComponent.get()
    }

    def 'Test validateAfterSubmit: empty email'() {
        given:
        def entry = Map.entry(KEY, new ApplicantAnswer(false, ""))
        def incorrectAnswers = new HashMap<String, String>()
        confirmUserCorpEmailComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)
        expect:
        incorrectAnswers.get(KEY) == ConfirmUserCorpEmailComponent.EMPTY_EMAIL_WARRING_MSG
    }

    def 'Test validateAfterSubmit: incorrect email'() {
        given:
        def entry = Map.entry(KEY, new ApplicantAnswer(false, INCORRECT_EMAIL))
        def incorrectAnswers = new HashMap<String, String>()
        confirmUserCorpEmailComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)
        expect:
        incorrectAnswers.get(KEY) == ConfirmUserCorpEmailComponent.DEFAULT_EMAIL_ERROR_MSG
    }
}
