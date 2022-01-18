package ru.gosuslugi.pgu.fs.component.confirm

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.service.OrgContactService
import spock.lang.Specification

import static ru.gosuslugi.pgu.fs.component.ComponentTestUtil.answerEntry

class ConfirmNewLegalEmailComponentTest extends Specification {

    private static final String COMPONENT_SUFFIX = "-component.json"
    private static final String TEST_EMAIL = "test@test.com"
    private static final String EMAIL = "email"

    ConfirmNewLegalEmailComponent confirmNewLegalEmailComponent
    OrgContactService orgContactService
    FieldComponent fieldComponent

    void setup() {
        orgContactService = Mock(OrgContactService)
        fieldComponent = JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), COMPONENT_SUFFIX),
                FieldComponent
        )
        confirmNewLegalEmailComponent = new ConfirmNewLegalEmailComponent(orgContactService)
        ComponentTestUtil.setAbstractComponentServices(confirmNewLegalEmailComponent)
    }

    def "Test getType returns a proper value"() {
        expect:
        confirmNewLegalEmailComponent.getType() == ComponentType.ConfirmLegalNewEmail
    }

    def 'Test getInitialValue returns a proper value if it is filled'() {
        expect:
        confirmNewLegalEmailComponent.getInitialValue(fieldComponent).get() == TEST_EMAIL
    }

    def "Test validate submitted value"() {
        when:
        def result = confirmNewLegalEmailComponent.validate(
                answerEntry(EMAIL, null),
                new ScenarioDto(),
                fieldComponent
        )

        then:
        0 * orgContactService.isEmailConfirmedAndLinked(_ as String) >> false
        result == ["email": "Введите адрес электронной почты"]

        when:
        result = confirmNewLegalEmailComponent.validate(
                answerEntry(EMAIL, TEST_EMAIL),
                new ScenarioDto(),
                fieldComponent
        )

        then:
        1 * orgContactService.isEmailConfirmedAndLinked(_ as String) >> false
        result == ["email": "Адрес электроной почты не подтвержден пользователем"]

        when:
        result = confirmNewLegalEmailComponent.validate(
                answerEntry(EMAIL, TEST_EMAIL),
                new ScenarioDto(),
                fieldComponent
        )

        then:
        1 * orgContactService.isEmailConfirmedAndLinked(_ as String) >> true
        result == [:]
    }
}
