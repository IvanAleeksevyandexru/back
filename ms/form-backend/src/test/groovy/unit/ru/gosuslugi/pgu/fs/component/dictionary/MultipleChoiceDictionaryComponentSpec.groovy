package unit.ru.gosuslugi.pgu.fs.component.dictionary

import com.fasterxml.jackson.databind.ObjectMapper
import org.joda.time.DateTime
import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.component.dictionary.MultipleChoiceDictionaryComponent
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService
import ru.gosuslugi.pgu.fs.service.impl.DictionaryFilterServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.NsiDictionaryFilterHelper
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService
import spock.lang.Specification

class MultipleChoiceDictionaryComponentSpec extends Specification {

    MultipleChoiceDictionaryComponent component

    def setup() {
        def nsiDictionaryServiceMock = Mock(NsiDictionaryService)
        def nsiDictionaryFilterHelper = Mock(NsiDictionaryFilterHelper)
        nsiDictionaryServiceMock.getDictionaryItemByValue(_ as String, _ as String, 'Ливия') >> Optional.ofNullable(new NsiDictionaryItem())
        nsiDictionaryServiceMock.getDictionaryItemByValue(_ as String, _ as String, 'Бенин') >> Optional.ofNullable(new NsiDictionaryItem())
        nsiDictionaryServiceMock.getDictionaryItemByValue(_ as String, _ as String, 'Тунис') >> Optional.empty()

        UserPersonalData userPersonalDataMock = Mock(UserPersonalData)
        DictionaryListPreprocessorService dictionaryListPreprocessorService = Mock(DictionaryListPreprocessorService)
        userPersonalDataMock.getPerson() >> new Person(gender: 'F')

        def jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
        def parseAttrValuesHelper = new ParseAttrValuesHelper(Stub(VariableRegistry), jsonProcessingService, Stub(ProtectedFieldService))
        def serviceIdVariable = new ServiceIdVariable(Stub(MainDescriptorService), jsonProcessingService, Stub(RuleConditionService))
        def calculatedAttributesHelper = new CalculatedAttributesHelper(serviceIdVariable, parseAttrValuesHelper, null)
        calculatedAttributesHelper.postConstruct()
        def dictionaryFilterService = new DictionaryFilterServiceImpl(nsiDictionaryServiceMock, nsiDictionaryFilterHelper, calculatedAttributesHelper, Mock(List))
        component = new MultipleChoiceDictionaryComponent(userPersonalDataMock, nsiDictionaryServiceMock, dictionaryFilterService, dictionaryListPreprocessorService)
    }

    def 'Can validate nsi dictionary list value with amount after submit '() {
        given:
        Map<String, String> incorrectAnswers = [:]
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry('country',
                '{ "list": [{"originalItem":{"value":"Тунис"}},{"originalItem":{"value":"Бенин"}}], "amount": 2}')
        FieldComponent fieldComponent = new FieldComponent(attrs: [dictionaryType: 'country'])

        when:
        component.validateAfterSubmit(incorrectAnswers, entry, Stub(ScenarioDto), fieldComponent)

        then:
        !incorrectAnswers.isEmpty()
        incorrectAnswers['country'] == 'Не найдено значение Тунис в справочнике НСИ country'

        when:
        incorrectAnswers = [:]
        entry = AnswerUtil.createAnswerEntry('country', '{ "list": [{"originalItem":{"value":"Ливия"}},{"originalItem":{"value":"Бенин"}}], "amount": 2}')
        component.validateAfterSubmit(incorrectAnswers, entry, Stub(ScenarioDto), fieldComponent)

        then:
        incorrectAnswers.isEmpty()
    }

    def 'Can validate gender nsi dictionary list value with amount after submit '() {
        given:
        Map<String, String> incorrectAnswers = [:]
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry('country',
                '{ "list": [{"originalItem":{"value":"Тунис"}},{"originalItem":{"value":"Бенин"}}], "amount": 2}')
        FieldComponent fieldComponent = new FieldComponent(attrs: [dictionaryType: ['country_male', 'country_female']])

        when:
        component.validateAfterSubmit(incorrectAnswers, entry, Stub(ScenarioDto), fieldComponent)

        then:
        !incorrectAnswers.isEmpty()
        incorrectAnswers['country'] == 'Не найдено значение Тунис в справочнике НСИ country_female'
    }

    def 'Can validate list dictionary with amount value after submit '() {
        given:
        Map<String, String> incorrectAnswers = [:]
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry('country',
                '{ "list": [{"id":"Уганда-0","originalItem":{"label":"Уганда"}},{"id":"Бенин-0","originalItem":{"label":"Бенин"}}], "amount": 2}')
        FieldComponent fieldComponent = new FieldComponent(attrs: [dictionaryList: [[label: 'Уганда'], [label: 'Ботсвана']]])

        when:
        component.validateAfterSubmit(incorrectAnswers, entry, Stub(ScenarioDto), fieldComponent)

        then:
        !incorrectAnswers.isEmpty()
        incorrectAnswers['country'] == 'Значение не из списка'

        when:
        incorrectAnswers = [:]
        fieldComponent.attrs['dictionaryList'] == [[label: 'Уганда'], [label: 'Бенин']]

        then:
        incorrectAnswers.isEmpty()
    }

    def 'Can set value for dictionary type "year"'() {
        given:
        FieldComponent fieldComponent = new FieldComponent(
                attrs: [year: [
                        first       : [
                                value: 'Current',
                                add  : 0
                        ],
                        gen         : -10,
                        sort        : "desc",
                        defaultEmpty: true

                ]])

        when:
        component.preProcess(fieldComponent, Stub(ScenarioDto))

        then:
        fieldComponent.attrs.containsKey('dictionaryList')
        def items = fieldComponent.attrs.get('dictionaryList') as List
        items.size() == 11
        items.get(0)['label'] == DateTime.now().year().get()
    }

    def 'Can set value for dictionary type "year" with amount'() {
        given:
        FieldComponent fieldComponent = new FieldComponent(
                attrs: [
                        year      : [
                                first       : [
                                        value: 'Current',
                                        add  : 0
                                ],
                                gen         : -10,
                                sort        : "desc",
                                defaultEmpty: true
                        ]
                ])

        when:
        component.preProcess(fieldComponent, Stub(ScenarioDto))

        then:
        fieldComponent.attrs.containsKey('dictionaryList')
        def items = fieldComponent.attrs.get('dictionaryList') as List
        items.size() == 11
        items.get(0)['label'] == DateTime.now().year().get()
    }
}
