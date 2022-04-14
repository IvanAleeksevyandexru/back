package ru.gosuslugi.pgu.fs.component.child

import ru.atc.carcass.security.rest.model.DocsCollection
import ru.atc.carcass.security.rest.model.person.Kids
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.*
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.service.ListComponentItemUniquenessService
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.EsiaUsers
import ru.gosuslugi.pgu.fs.component.input.RepeatableFieldsComponent
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService
import ru.gosuslugi.pgu.fs.service.ParticipantService
import spock.lang.Specification

import static ru.gosuslugi.pgu.components.ComponentAttributes.MDCL_PLCY_ATTR
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.StringInput

class ChildrenListComponentTest extends Specification {

    private static final String SUFFIX = "-component.json"

    private static ScenarioDto scenarioDto
    private static FieldComponent fieldComponent
    private static ServiceDescriptor serviceDescriptor
    private static ChildrenListComponent component

    def setupSpec() {
        scenarioDto = new ScenarioDto()
        scenarioDto.setCurrentValue(Map.of())
        scenarioDto.setApplicantAnswers([
                "cl24": new ApplicantAnswer(value: '[{"id":"bb"},{"id":"cc"}]', visited: true)
        ])

        fieldComponent = JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), SUFFIX),
                FieldComponent
        )

        serviceDescriptor = new ServiceDescriptor(applicationFields: [
                new FieldComponent(id: 'cl24', type: StringInput, attrs: [components: ["bb", "cc"], fields: [["fieldName": "firstName"], ["fieldName": "lastName"], ["fieldName": "rfBirthCertificateSeries"], ["fieldName": "rfBirthCertificateIssueDate"]]]),
                new FieldComponent(id: 'bb', type: StringInput, attrs: [fields: [["fieldName": "firstName"], ["fieldName": "lastName"], ["fieldName": "rfBirthCertificateSeries"], ["fieldName": "rfBirthCertificateIssueDate"]]]),
                new FieldComponent(id: 'cc', type: StringInput, attrs: [fields: [["fieldName": "isNew"]]])
        ])

        UserPersonalData user = EsiaUsers.userWithChildren
        user.getKids().get(0).setDocuments(new DocsCollection(docs: [new PersonDoc(type: MDCL_PLCY_ATTR, number: "11111111111")]))

        component = new ChildrenListComponent(
                user,
                Mock(RepeatableFieldsComponent),
                Mock(ParticipantService),
                Mock(ListComponentItemUniquenessService),
                Mock(DictionaryListPreprocessorService)
        )
        ComponentTestUtil.setAbstractComponentServices(component)
    }

    def 'Get type'() {
        expect:
        component.getType() == ComponentType.ChildrenList
    }

    def 'Test getInitialValue: get initial value response is correct when component contains "childListRef"'() {
        given:
        fieldComponent.getAttrs().put("childListRef", "cl24.value")

        when:
        def result = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        assert result.get() == '[{"id":"bb"},{"id":"cc"}]'
    }

    def 'Test getInitialValue: get initial value response is correct when component does not contain "childListRef"'() {
        when:
        def result = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        assert result.get() == '[{"bb":{"firstName":"Петя","lastName":"Петечкин"},"cc":false},{"bb":{"firstName":"Вася","lastName":"Васечкин"},"cc":false},{"bb":{"firstName":"Вася","lastName":"Васечкин","rfBirthCertificateIssueDate":"2013-10-10T00:00:00Z","rfBirthCertificateSeries":"22"},"cc":false}]'
    }

    def 'Test getInitValue: get init value response is correct with birthDate filters'() {
        given:
        fieldComponent.getAttrs().put("bornAfterDate", bornAfterDate)
        fieldComponent.getAttrs().put("bornBeforeDate", bornBeforeDate)

        when:
        def result = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        result.get() == response

        where:
        bornAfterDate || bornBeforeDate || response
        '10.06.2005'  || null           || '[{"bb":{"firstName":"Петя","lastName":"Петечкин"},"cc":false},{"bb":{"firstName":"Вася","lastName":"Васечкин"},"cc":false},{"bb":{"firstName":"Вася","lastName":"Васечкин","rfBirthCertificateIssueDate":"2013-10-10T00:00:00Z","rfBirthCertificateSeries":"22"},"cc":false}]'
        null          || '10.05.2013'   || '[{"bb":{"firstName":"Петя","lastName":"Петечкин"},"cc":false},{"bb":{"firstName":"Вася","lastName":"Васечкин"},"cc":false},{"bb":{"firstName":"Вася","lastName":"Васечкин","rfBirthCertificateIssueDate":"2013-10-10T00:00:00Z","rfBirthCertificateSeries":"22"},"cc":false}]'
        '01.01.2009'  || null           || '[{"bb":{"firstName":"Вася","lastName":"Васечкин","rfBirthCertificateIssueDate":"2013-10-10T00:00:00Z","rfBirthCertificateSeries":"22"},"cc":false}]'
        '01.01.2009'  || '10.05.2013'   || '[{"bb":{"firstName":"Вася","lastName":"Васечкин","rfBirthCertificateIssueDate":"2013-10-10T00:00:00Z","rfBirthCertificateSeries":"22"},"cc":false}]'
        null          || '11.06.2005'   || '[{"bb":{"firstName":"Петя","lastName":"Петечкин"},"cc":false},{"bb":{"firstName":"Вася","lastName":"Васечкин"},"cc":false}]'
        '01.01.2020'  || '01.01.2022'   || '[]'
        '01.01.2020'  || null           || '[]'
        null          || null           || '[{"bb":{"firstName":"Петя","lastName":"Петечкин"},"cc":false},{"bb":{"firstName":"Вася","lastName":"Васечкин"},"cc":false},{"bb":{"firstName":"Вася","lastName":"Васечкин","rfBirthCertificateIssueDate":"2013-10-10T00:00:00Z","rfBirthCertificateSeries":"22"},"cc":false}]'
    }

    def 'Test getInitValue: get init value response is correct with gender filters'() {
        given:
        UserPersonalData userPersonalData = Mock(UserPersonalData)
        ChildrenListComponent component = new ChildrenListComponent(
                userPersonalData,
                Mock(RepeatableFieldsComponent),
                Mock(ParticipantService),
                Mock(ListComponentItemUniquenessService),
                Mock(DictionaryListPreprocessorService)
        )
        ComponentTestUtil.setAbstractComponentServices(component)
        fieldComponent.getAttrs().put("gender", gender)
        userPersonalData.kids >> List.of(
                new Kids(id: '1',
                        firstName: 'Петя', lastName: 'Петечкин', birthDate: '10.06.2010',
                        gender: 'M',
                        documents: new DocsCollection(
                                elements: [new PersonDoc(
                                        type: 'BRTH_CERT',
                                        series: '22',
                                        number: '123-456',
                                        issuedBy: 'Загс',
                                        issueDate: '10.10.2010',
                                )])
                ),
                new Kids(id: '2',
                        firstName: 'Василиса', lastName: 'Петечкина', birthDate: '10.06.2020',
                        gender: 'F',
                        documents: new DocsCollection(
                                elements: [new PersonDoc(
                                        type: 'BRTH_CERT',
                                        series: '33',
                                        number: '123-456',
                                        issuedBy: 'Загс',
                                        issueDate: '10.10.2020',
                                )])
                ))

        when:
        def result = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        result.get() == response

        where:
        gender   || response
        ""       || "[]"
        "male"   || '[{"bb":{"firstName":"Петя","lastName":"Петечкин","rfBirthCertificateIssueDate":"2010-10-10T00:00:00Z","rfBirthCertificateSeries":"22"},"cc":false}]'
        "female" || '[{"bb":{"firstName":"Василиса","lastName":"Петечкина","rfBirthCertificateIssueDate":"2020-10-10T00:00:00Z","rfBirthCertificateSeries":"33"},"cc":false}]'
        "both"   || '[{"bb":{"firstName":"Петя","lastName":"Петечкин","rfBirthCertificateIssueDate":"2010-10-10T00:00:00Z","rfBirthCertificateSeries":"22"},"cc":false},{"bb":{"firstName":"Василиса","lastName":"Петечкина","rfBirthCertificateIssueDate":"2020-10-10T00:00:00Z","rfBirthCertificateSeries":"33"},"cc":false}]'
        null     || '[]'
    }

    def "Should do valid post processing"() {
        given:
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry('bb', '[{"id":"bb"},{"id":"cc"}]')

        ScenarioDto preparedScenarioDto =
                new ScenarioDto(
                        display: new DisplayRequest(
                                components:
                                        [
                                                new FieldComponent(id: 'bb', attrs: ["imaginaryOidBase": 1])
                                        ]
                        ),
                        participants: Map.of(
                                "1111", new ApplicantDto(role: ApplicantRole.Coapplicant),
                                "2222", new ApplicantDto(role: ApplicantRole.Coapplicant),
                                "3333", new ApplicantDto(role: ApplicantRole.Approval),
                                "4444", new ApplicantDto(role: ApplicantRole.ApprovalParent),
                                "5555", new ApplicantDto(role: ApplicantRole.ChildrenAbove14)
                        )
                )

        FieldComponent fieldComponentMock = Mock(FieldComponent) {
            getAttrs() >> Mock(Map) {
                containsKey("imaginaryOidBase") >> true
                get("imaginaryOidBase") >> 1
            }
        }

        when:
        component.postProcess(entry, preparedScenarioDto, fieldComponentMock)

        then:
        assert preparedScenarioDto.getParticipants().size() == 7
    }

    def 'validateOff attribute test'() {
        given:
        def scenarioDto = Mock(ScenarioDto)
        def entry = [] as Map.Entry<String, ApplicantAnswer>
        def fieldComponent = new FieldComponent()
        fieldComponent.setAttrs(new HashMap<>())
        def repeatableFieldsComponent = Mock(RepeatableFieldsComponent) {
            validate(entry, scenarioDto, fieldComponent) >> new HashMap<String, String>() {
                {
                    put("fieldName", "errorMsg")
                }
            }
        }

        def component = new ChildrenListComponent(Mock(UserPersonalData), repeatableFieldsComponent, Mock(ParticipantService), Mock(ListComponentItemUniquenessService),Mock(DictionaryListPreprocessorService))
        Map<String, String> incorrectAnswers = new HashMap<>()

        when:
        fieldComponent.getAttrs().put(ChildrenListComponent.VALIDATE_OFF_ATTR_NAME, validateOff)
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        !incorrectAnswers.isEmpty() == hasErrors

        where:
        validateOff   | hasErrors
        null          | true
        Boolean.TRUE  | false
        'true'        | false
        Boolean.FALSE | true
        'FALSE'       | true
    }
}
