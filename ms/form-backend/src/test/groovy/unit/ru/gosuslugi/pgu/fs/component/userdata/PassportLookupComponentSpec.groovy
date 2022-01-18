package unit.ru.gosuslugi.pgu.fs.component.userdata

import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException
import ru.gosuslugi.pgu.common.esia.search.dto.PersonWithAge
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.userdata.PassportLookupComponent
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import ru.gosuslugi.pgu.fs.service.ParticipantService
import spock.lang.Specification

class PassportLookupComponentSpec extends Specification {

    PassportLookupComponent component
    ServiceDescriptor serviceDescriptorMock
    ScenarioDto scenarioDtoMock
    FieldComponent fieldComponent
    Map.Entry<String, ApplicantAnswer> entry

    def setup() {
        PersonSearchService personSearchServiceMock = Mock(PersonSearchService) {
            it.searchOneTrusted(_ as String, _ as String) >> testPerson()
        }
        ParticipantService participantServiceMock = Mock(ParticipantService)
        ErrorModalDescriptorService errorModalDescriptorService = Mock(ErrorModalDescriptorService)
        component = new PassportLookupComponent(personSearchServiceMock, participantServiceMock, errorModalDescriptorService)

        serviceDescriptorMock = Mock(ServiceDescriptor)
        scenarioDtoMock = Mock(ScenarioDto) {
            getDisplay() >> Mock(DisplayRequest) {
                getId() >> "scrchld1"
            }
            getApplicantAnswers() >> Mock(Map) {
                get("ai15_4") >> new ApplicantAnswer(true, "Ярослав")
                get("ai15_5") >> new ApplicantAnswer(true, "Васин")
            }
        }
        fieldComponent = testFieldComponent()
    }

    def "Should have correct initial state"() {
        expect:
        component.getDefaultAnswer(fieldComponent) == null
        component.isCycled()
        component.getType() == ComponentType.PassportLookup
    }

    def "Should have initial value as empty component response"() {
        when:
        def initialValue = component.getInitialValue(
                fieldComponent, scenarioDtoMock, serviceDescriptorMock
        )

        def cycledInitialValue = component.getCycledInitialValue(fieldComponent, _ as Map)

        then:
        initialValue == ComponentResponse.empty()
        cycledInitialValue == ComponentResponse.empty()
    }

    def "Can validate via common validation mechanism"() {
        when:
        def rule = component.getValidations().first()
        def validateSeries = rule.validate(
                ComponentTestUtil.answerEntry("rfPasportSeries", actual), fieldComponent
        )

        then:
        validateSeries?.getValue() == expected

        where:
        actual || expected
        "0048" || null
        ""     || "Серия паспорта не задана"
        null   || "Серия паспорта не задана"
    }

    def 'Can validate after submit'() {
        given:
        Map<String, String> incorrectAnswers = [:]

        when:
        entry = AnswerUtil.createAnswerEntry("key",
                "{" + "\"rfPasportSeries\": \"${rfPasportSeries}\"," +
                        " \"rfPasportNumber\": \"${rfPasportNumber}\"" + "}")

        component.validateAfterSubmit(incorrectAnswers, entry, fieldComponent)

        then:
        incorrectAnswers['key'] == errorMessage

        where:
        rfPasportSeries | rfPasportNumber || errorMessage
        "0048"          | "15162342"      || null
        "0048"          | ""              || "Номер паспорта не задан"
        ""              | "15162342"      || "Серия паспорта не задана"
        ""              | ""              || "Серия и номер паспорта не заданы"
    }

    def "Should do valid post processing"() {
        given:
        entry = AnswerUtil.createAnswerEntry('passportRf',
                '{\"rfPasportSeries\": \"0048\", \"rfPasportNumber\": \"15162342\"}'
        )

        expect:
        component.postProcess(entry, scenarioDtoMock, fieldComponent)
        entry.value.value == '' +
                '{"rfPasportSeries":"0048",' +
                '"rfPasportNumber":"15162342",' +
                '"oid":"4815162342",' +
                '"snils":"000-461-532 57",' +
                '"firstName":"Ярослав",' +
                '"lastName":"Васин",' +
                '"middleName":"Николаевич",' +
                '"birthDate":"1979-02-12",' +
                '"gender":"М",' +
                '"exists":true' +
                '}'
    }

    def "Should add ESIA data to cycled item"() {
        given:
        CycledApplicantAnswerItem cycledApplicantAnswerItemMock = new CycledApplicantAnswerItem("test_id")

        when:
        component.addToCycledItemEsiaData(
                fieldComponent,
                new ApplicantAnswer(true, "{\"value\":\"some_value\", \"rfPasportSeries\":\"0048\", \"rfPasportNumber\":\"15162342\"}"),
                cycledApplicantAnswerItemMock
        )

        then:
        cycledApplicantAnswerItemMock.getEsiaData()['value'] == 'some_value'
        cycledApplicantAnswerItemMock.getEsiaData()['middleName'] == null
        cycledApplicantAnswerItemMock.getEsiaData()['rfPasportSeries'] == '0048'
        cycledApplicantAnswerItemMock.getEsiaData()['rfPasportNumber'] == '15162342'
    }

    def "Throws an error while check person by answers is failed"() {
        given:
        PersonWithAge person = testPerson()

        scenarioDtoMock = Mock(ScenarioDto) {
            getDisplay() >> Mock(DisplayRequest) {
                getId() >> "scrchld1"
            }
            getApplicantAnswers() >> Mock(Map) {
                get("ai15_4") >> null
                get("ai15_5") >> null
            }
        }

        when:
        component.checkPersonByAnswers(person, scenarioDtoMock, fieldComponent)

        then:
        thrown(FormBaseWorkflowException)
    }

    def "Can check person by answers"() {
        given:
        PersonWithAge person = testPerson()

        when:
        component.checkPersonByAnswers(person, scenarioDtoMock, fieldComponent)

        then:
        notThrown(FormBaseWorkflowException)
    }

    static def testFieldComponent() {
        new FieldComponent(attrs:
                [fields              : [
                        [
                                mask     : ["/\\d/", "/\\d/", "/\\d/", "/\\d/"],
                                fieldName: 'rfPasportSeries',
                                label    : 'Серия',
                                type     : "input",
                                regexp   : "^[0-9]{4}\$",
                                errorMsg : "Поле должно содержать 4 цифры"

                        ],
                        [
                                mask     : ["/\\d/", "/\\d/", "/\\d/", "/\\d/", "/\\d/", "/\\d/"],
                                fieldName: 'rfPasportNumber',
                                label    : 'Номер',
                                type     : "input",
                                regexp   : "^[0-9]{6}\$",
                                errorMsg : "Поле должно содержать 6 цифр"
                        ]
                ],
                 compare_rows        : [firstName: "ai15_4", lastName: "ai15_5"],
                 compare_rows_screens: ["scrchld1"]
                ])
    }

    static def testPerson() {
        def person = new PersonWithAge()
        person.setOid("4815162342")
        person.setSnils("000-461-532 57")
        person.setFirstName("Ярослав")
        person.setLastName("Васин")
        person.setMiddleName("Николаевич")
        person.setBirthDate("12.02.1979")
        person.setGender("М")
        return person
    }
}
