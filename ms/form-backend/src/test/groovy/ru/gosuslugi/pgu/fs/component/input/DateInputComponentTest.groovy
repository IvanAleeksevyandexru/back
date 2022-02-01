package ru.gosuslugi.pgu.fs.component.input

import ru.gosuslugi.pgu.common.core.date.util.DateUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.input.DateInputComponent
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import spock.lang.Shared
import spock.lang.Specification

import java.time.OffsetDateTime

class DateInputComponentTest extends Specification {

    @Shared
    InitialValueFromService initialValueFromService

    def setupSpec() {
        initialValueFromService = ComponentTestUtil.getInitialValueFromService()
    }

    def 'correct component type test'() {
        given:
        def component = new DateInputComponent(Mock(InitialValueFromService))

        expect:
        component.getType() == ComponentType.DateInput
    }

    def 'initial value test'() {
        given:
        def component = new DateInputComponent(initialValueFromService)
        def serviceDescriptor = Mock(ServiceDescriptor)

        def scenarioDto = new ScenarioDto()
        scenarioDto.setCurrentValue(Map.of())
        scenarioDto.setApplicantAnswers(applicantAnswers())

        when:
        def initialValue = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        initialValue && initialValue.get() == value

        where:
        fieldComponent                           | value
        presettedFieldComponent("dateIssue")     | '09.22.2021'
        presettedFieldComponent("incorrectDate") | '02.28.2021'
        presettedFieldComponent("wrongAnswer")   | ""
    }

    def 'validation rules test'() {
        given:
        FieldComponent fieldComponent = fieldComponent()

        def scenarioDto = new ScenarioDto()
        scenarioDto.setApplicantAnswers(applicantAnswers())

        def component = new DateInputComponent(initialValueFromService)
        ComponentTestUtil.setAbstractComponentServices(component)

        when:
        def entry = ComponentTestUtil.answerEntry(fieldComponent.getId(), value)
        fieldComponent.getAttrs()["validation"] = List.of(validation)
        fieldComponent.setRequired(required)
        Map<String, String> incorrectAnswers = component.validate(entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers != null
        incorrectAnswers.size() == size
        incorrectAnswers.get(fieldComponent.getId()) == text

        where:
        value            | size | text                                   | required | validation
        null             | 1    | "Значение не задано"                   | true     | [] as HashMap
        ''               | 1    | "Значение не задано"                   | true     | [] as HashMap
        null             | 0    | null                                   | false    | [] as HashMap
        ''               | 0    | null                                   | false    | [] as HashMap
        '2019-12-31'     | 0    | null                                   | true     | [] as HashMap
        '2020-12-81'     | 1    | "DateInput field has incorrect " +
                "format according to accuracy"         | true     | [] as HashMap
        '2013-06-14'     | 1    | "Дата не равна 2021-09-22"             | true     | [type: "equalsDate", ref: "dateIssue", errorMsg: "Дата не равна 2021-09-22"]
        '2021-09-22'     | 0    | null                                   | true     | [type: "equalsDate", ref: "dateIssue"]
        '2013-06-14'     | 1    | "Дата меньше 2021-09-22"               | true     | [type: "minDate", ref: "dateIssue", errorMsg: "Дата меньше 2021-09-22"]
        '2021-12-31'     | 0    | null                                   | true     | [type: "minDate", ref: "dateIssue"]
        '2021-12-31'     | 1    | "Дата больше 2021-09-22"               | true     | [type: "maxDate", ref: "dateIssue", errorMsg: "Дата больше 2021-09-22"]
        '2021-12-31'     | 1    | "Значение не прошло валидацию maxDate" | true     | [type: "maxDate", ref: "dateIssue"]
        '2013-06-14'     | 0    | null                                   | true     | [type: "maxDate", ref: "dateIssue"]
        getCurrentDate() | 0    | null                                   | true     | [type: "equalsDate", ref: "today"]
        '2000-01-01'     | 1    | "Не сегодня"                           | true     | [type: "equalsDate", ref: "today", errorMsg: "Не сегодня"]
        '2050-01-01'     | 0    | null                                   | true     | [type: "minDate", ref: "today"]
        '2000-01-01'     | 1    | "Не ранее, чем сегодня"                | true     | [type: "minDate", ref: "today", errorMsg: "Не ранее, чем сегодня"]
        '2000-01-01'     | 0    | null                                   | true     | [type: "maxDate", ref: "today"]
        '2050-01-01'     | 1    | "Позже текущей даты"                   | true     | [type: "maxDate", ref: "today", errorMsg: "Позже текущей даты"]
    }

    private static String getCurrentDate() {
        DateUtil.fromOffsetDateTimeToEsiaFormat(OffsetDateTime.now().toString())
    }

    private static Map<String, ApplicantAnswer> applicantAnswers() {
        return [
                "dateIssue"    : new ApplicantAnswer(value: '2021-09-22', visited: true),
                "incorrectDate": new ApplicantAnswer(value: '31.02.2021', visited: true),
                "wrongAnswer"  : new ApplicantAnswer(value: 'abracadabra', visited: true),
        ]
    }

    private static def fieldComponent(String presetFromValue = null) {
        [
                id   : "di1",
                type : ComponentType.DateInput,
                attrs: [
                        validation: [] as ArrayList
                ]
        ] as FieldComponent
    }

    private static def presettedFieldComponent(String presetFromValue = null) {
        [
                id   : 'di1',
                type : ComponentType.DateInput,
                attrs: [
                        preset_from: [
                                type: "REF", value: presetFromValue
                        ]
                ]
        ] as FieldComponent
    }
}
