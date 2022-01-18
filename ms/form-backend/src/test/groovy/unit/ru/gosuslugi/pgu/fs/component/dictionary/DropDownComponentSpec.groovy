package unit.ru.gosuslugi.pgu.fs.component.dictionary

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ServiceInfoDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.dictionary.DropDownComponent
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService
import spock.lang.Specification

import java.time.LocalDate

class DropDownComponentSpec extends Specification {

    DropDownComponent component

    def setup() {
        UserPersonalData userPersonalDataMock = Mock(UserPersonalData)
        userPersonalDataMock.person >> new Person(gender: 'M')
        DictionaryListPreprocessorService dictionaryListPreprocessorService = Mock(DictionaryListPreprocessorService)

        component = new DropDownComponent(userPersonalDataMock, dictionaryListPreprocessorService) {
            @Override
            protected LocalDate getCurrentDate() {
                return LocalDate.of(2021, 8, 1)
            }
        }
        ComponentTestUtil.setAbstractComponentServices(component)
    }

    def 'Can filter dictionary list by gender'() {
        given:
        FieldComponent fieldComponent = new FieldComponent(
                attrs: [dictionaryList: [
                        [label: 'Брат', code: 'brother', gender: 'man'],
                        [label: 'Сестра', code: 'sister', gender: 'woman'],
                        [label: 'Муж', code: 'husband', gender: 'man'],
                        [label: 'Жена', code: 'wife', gender: 'woman'],
                        [label: 'Кошка', code: 'cat', gender: 'incorrect value'],
                        [label: 'Собака', code: 'dog']
                ]])
        ScenarioDto scenarioDto = new ScenarioDto()

        when:
        component.preProcess(fieldComponent, scenarioDto)

        then:
        def items = fieldComponent.attrs.get('dictionaryList') as List
        items.size() == 3
        items[0]['code'] == 'brother'
        items[1]['code'] == 'husband'
        items[2]['code'] == 'dog'
    }

    def 'Can generate years with day and month'() {
        given:
        FieldComponent fieldComponent = new FieldComponent(
                attrs: [
                        year: [
                                first:[
                                        value: "Current",
                                        day: "01",
                                        month: month,
                                        offset: offset,
                                        add: add
                                ],
                                gen: -4,
                                sort: "desc",
                                defaultEmpty: true
                        ]
                ]
        )
        ScenarioDto scenarioDto = new ScenarioDto()

        when:
        component.preProcess(fieldComponent, scenarioDto)

        then:
        def items = fieldComponent.attrs.get('dictionaryList') as List
        items.size() == 5

        for (i in 0..4) {
            items[i]['label'] == firstYear - i
        }

        where:
        month   | firstYear | offset  | add
        "04"    | 2020      | 1       | -1
        "09"    | 2019      | 1       | -1
        "04"    | 2019      | 2       | -1
        "09"    | 2018      | 2       | -1
        "04"    | 2016      | 1       | -5
        "09"    | 2015      | 1       | -5
    }

    def 'Test value validation'() {
        given:
        FieldComponent fieldComponent = JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), "-component.json"),
                FieldComponent.class)
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry(fieldComponent.getId(), value)
        def scenarioDto = ComponentTestUtil.mockScenario( new HashMap<String, ApplicantAnswer>(), new HashMap<String, ApplicantAnswer>(), new ServiceInfoDto())

        when:
        Map<String, String> validationErrors = component.validate(entry, scenarioDto, fieldComponent)

        then:
        validationErrors != null
        validationErrors.size() == errorSize
        if (errorSize > 0) {
            validationErrors.containsKey(fieldComponent.getId())
            validationErrors.get(fieldComponent.getId()) == error
        }

        where:
        value                                          | errorSize | error
        null                                           | 1         | 'Значение не задано'
        "{\"originalItem\": {\"label\":\"\"}}"         | 1         | 'Значение не из списка'
        "{\"originalItem\": {\"label\":\"2020 год\"}}" | 1         | 'Значение не из списка'
        "{\"originalItem\": {\"label\":\"2020\"}}"     | 0         | ''
    }
}