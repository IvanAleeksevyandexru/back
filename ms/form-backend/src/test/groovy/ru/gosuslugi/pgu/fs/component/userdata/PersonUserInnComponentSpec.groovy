package ru.gosuslugi.pgu.fs.component.userdata

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ServiceInfoDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.component.userdata.PersonUserInnComponent
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import spock.lang.Specification

class PersonUserInnComponentSpec extends Specification {
    PersonUserInnComponent personUserInnComponent

    def 'Check initial value '() {
        given:
        UserPersonalData userPersonalDataMock = Mock(UserPersonalData)
        userPersonalDataMock.getPerson() >> new Person(inn: '525616150575')
        personUserInnComponent = new PersonUserInnComponent(userPersonalDataMock)
        ComponentTestUtil.setAbstractComponentServices(personUserInnComponent)
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component.json"), FieldComponent.class)

        when:
        ComponentResponse result = personUserInnComponent.getInitialValue(component)

        then:
        result == ComponentResponse.of('525616150575')
    }

    def 'Check validations'() {
        given:
        UserPersonalData userPersonalDataMock = Mock(UserPersonalData)
        userPersonalDataMock.getPerson() >> new Person(inn: value)
        personUserInnComponent = new PersonUserInnComponent(userPersonalDataMock)
        ComponentTestUtil.setAbstractComponentServices(personUserInnComponent)
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component.json"), FieldComponent.class)
        Map.Entry<String, ApplicantAnswer> entry = getEntry("fai1", value2)
        def scenarioDto = ComponentTestUtil.mockScenario( new HashMap<String, ApplicantAnswer>(), new HashMap<String, ApplicantAnswer>(), new ServiceInfoDto())

        when:
        Map<String,String> map = personUserInnComponent.validate(entry, scenarioDto, component)

        then:
        map != null
        map.size() == size
        map.get("fai1") == text

        where:
        value         | value2        | size | text
        '525616150575'| '525616150576'| 1    | "ИНН не совпадает с запрошенным из ЛК: 525616150575"
        '525616150575'| '525616150575'| 0    | null
        '5150575'     | '5150575'     | 1    | "Значение не является ИНН"
        ''            | ''            | 1    | "Поле обязательно для заполнения"
    }

    private Map.Entry<String, ApplicantAnswer> getEntry(String id, String value) {
        ApplicantAnswer answer = new ApplicantAnswer();
        answer.setValue(value);
        Map.Entry<String, ApplicantAnswer> entry = new AbstractMap.SimpleEntry<String, ApplicantAnswer>(id, answer);
        return entry;
    }
}
