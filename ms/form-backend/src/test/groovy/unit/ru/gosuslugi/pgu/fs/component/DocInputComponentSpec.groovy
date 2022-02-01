package unit.ru.gosuslugi.pgu.fs.component

import ru.atc.carcass.security.rest.model.DocsCollection
import ru.atc.carcass.security.rest.model.person.Kids
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.input.DocInputComponent
import ru.gosuslugi.pgu.fs.component.input.model.DocInputDto
import spock.lang.Specification

class DocInputComponentSpec extends Specification {

    DocInputComponent component
    UserPersonalData personalData

    def kidId = "1"

    def setup() {
        personalData = Mock(UserPersonalData)
        personalData.docs >> [
                new PersonDoc(
                        type: 'VERIFIED_DOC',
                        series: '111',
                        number: '111-777',
                        issuedBy: 'ЗАГС г.Магадан',
                        issueDate: '30.10.2010',
                        vrfStu: 'VERIFIED'),
                new PersonDoc(
                        type: 'UNVERIFIED_DOC',
                        series: '222',
                        number: '222-777',
                        issuedBy: 'ЗАГС г.Магадан',
                        issueDate: '30.10.2010',
                        vrfStu: 'NONE'),
                new PersonDoc(
                        type: 'RF_PASSPORT',
                        series: '1234',
                        number: '123456',
                        issuedBy: 'ЗАГС г.Магадан',
                        issueId: '888999',
                        issueDate: '30.10.2010',
                        vrfStu: 'VERIFIED'
                )
        ]

        personalData.kids >> List.of(
                new Kids(id: kidId,
                        documents: new DocsCollection(
                                elements: List.of(new PersonDoc(
                                        type: 'RF_BRTH_CERT',
                                        series: 'XO044',
                                        number: '555-777',
                                        issuedBy: 'ЗАГС г.Магадан',
                                        issueDate: '30.10.2010',
                                        vrfStu: 'NONE'
                                )))))

        component = new DocInputComponent(personalData)
    }

    def 'Can get initial value'() {
        given:
        def fieldComponent = new FieldComponent(
                type: ComponentType.DocInput,
                attrs: [type  : 'VERIFIED_DOC',
                        fields: [series: [:], number: [:], date: [:], emitter: [:]]])
        def result

        when: 'Verified document'
        result = component.getInitialValue(fieldComponent, Stub(ScenarioDto), Stub(ServiceDescriptor))

        then:
        result != null
        result.get() == new DocInputDto(
                date: '2010-10-30T00:00:00.000Z',
                number: '111-777',
                series: '111',
                emitter: 'ЗАГС г.Магадан')

        when: 'Unverified document'
        fieldComponent.attrs['type'] = 'UNVERIFIED_DOC'
        result = component.getInitialValue(fieldComponent, Stub(ScenarioDto), Stub(ServiceDescriptor))

        then:
        result != null
        def document = result.get()
        document.date == '2010-10-30T00:00:00.000Z'
        document.series == '222'
        document.number == '222-777'
        document.emitter == 'ЗАГС г.Магадан'
    }

    def 'Can get initial value not ignore boolean verification'() {
        given:
        def fieldComponent = new FieldComponent(
                type: ComponentType.DocInput,
                attrs: [type  : 'VERIFIED_DOC',
                        fields: [series: [:], number: [:], date: [:], emitter: [:]],
                        ignoreVerification: false])
        def result

        when: 'Verified document'
        result = component.getInitialValue(fieldComponent, Stub(ScenarioDto), Stub(ServiceDescriptor))

        then:
        result != null
        result.get() == new DocInputDto(
                date: '2010-10-30T00:00:00.000Z',
                number: '111-777',
                series: '111',
                emitter: 'ЗАГС г.Магадан')

        when: 'Unverified document'
        fieldComponent.attrs['type'] = 'UNVERIFIED_DOC'
        result = component.getInitialValue(fieldComponent, Stub(ScenarioDto), Stub(ServiceDescriptor))

        then:
        result != null
        def document = result.get()
        document.date == null
        document.series == null
        document.number == null
        document.emitter == null
    }

    def 'Can get cycled initial value'() {
        given:
        def fieldComponent = new FieldComponent(
                type: ComponentType.DocInput,
                attrs: [type              : 'RF_BRTH_CERT',
                        ignoreVerification: 'true',
                        fields            : [series: [:], number: [:], date: [:], emitter: [:]]])
        def externalData = [id: kidId]
        def result

        when: 'Ignore verification'
        result = component.getCycledInitialValue(fieldComponent, externalData)

        then:
        result != null
        result.get() == new DocInputDto(
                date: '2010-10-30T00:00:00.000Z',
                number: '555-777',
                series: 'XO044',
                emitter: 'ЗАГС г.Магадан')

        when: 'Check verification'
        fieldComponent.attrs.remove('ignoreVerification')
        result = component.getCycledInitialValue(fieldComponent, externalData)

        then:
        result != null
        def document = result.get()
        document.date == '2010-10-30T00:00:00.000Z'
        document.series == 'XO044'
        document.number == '555-777'
        document.emitter == 'ЗАГС г.Магадан'
    }

    def 'Can get cycled initial value and not ignore string verification'() {
        given:
        def fieldComponent = new FieldComponent(
                type: ComponentType.DocInput,
                attrs: [type              : 'RF_BRTH_CERT',
                        ignoreVerification: 'false',
                        fields            : [series: [:], number: [:], date: [:], emitter: [:]]])
        def externalData = [id: kidId]
        def result

        when: 'Ignore verification'
        result = component.getCycledInitialValue(fieldComponent, externalData)

        then:
        result != null
        result.get() == new DocInputDto(
                date: null,
                number: null,
                series: null,
                emitter: null)

        when: 'Check verification'
        fieldComponent.attrs.remove('ignoreVerification')
        result = component.getCycledInitialValue(fieldComponent, externalData)

        then:
        result != null
        def document = result.get()
        document.date == '2010-10-30T00:00:00.000Z'
        document.series == 'XO044'
        document.number == '555-777'
        document.emitter == 'ЗАГС г.Магадан'
    }

    def 'Date validation'() {
        def value = '{"date":"2021-09-16T00:00:00.000+03:00"}'
        def scenarioDto = new ScenarioDto()
        scenarioDto.setApplicantAnswers(['ref1':new ApplicantAnswer(true,'2021-09-14T00:00:00.000+03:00')])

        given:
        def validation = new HashMap(
                type: validationType,
                value: '',
                ref:'ref1',
                errorMsg:'dateError'
        )
        def fieldComponent = new FieldComponent(
                type: ComponentType.DocInput,
                attrs: [fields: [date: [type: 'date',
                                        label: '',
                                        required: true,
                                        attrs: [maxDate: 'today',
                                                minDate: '-130y',
                                                validation: [validation]]]]])
        def result

        when:
        result = component.validate(fieldComponent, value, scenarioDto)

        then:
        result == expectedResult

        where:
        validationType | expectedResult
        'minDate' | [:]
        'maxDate' | [date: 'dateError']
    }

    def 'can retrieve user issueId field'() {

        given:
        def fieldComponent = new FieldComponent(
                type: ComponentType.DocInput,
                attrs: [
                        type: 'RF_PASSPORT',
                        fields: [
                                issueId: [:]
                        ]
                ])

        expect:

        def response = component.getInitialValue(fieldComponent, null, null).get()
        response != null
        response.getIssueId() == '888-999'

    }
}
