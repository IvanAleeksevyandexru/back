package ru.gosuslugi.pgu.fs.component.calendar

import com.fasterxml.jackson.core.type.TypeReference
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry
import ru.gosuslugi.pgu.fs.common.component.input.DateInputComponent
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.service.impl.InitialValueFromImpl
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import spock.lang.Specification

class CalendarInputComponentSpec extends Specification {

    private CalendarInputComponent calendarInput
    private DateInputComponent dateInput
    private ComponentRegistry componentRegistry
    private ScenarioDto scenarioDto
    private ServiceDescriptor screenDescriptor

    private FieldComponent dateInputComponentFirst
    private FieldComponent dateInputComponentSecond
    private FieldComponent calendarInputFieldComponent

    def setup() {
        dateInputComponentFirst = [
                id      : "di1",
                type    : ComponentType.DateInput,
                required: true,
                attrs   : [
                        hint                 : 'дата начала',
                        grid                 : 'grid-col-6 grid-col-12-sm',
                        brokenDateFixStrategy: 'restore',
                        preset_from          : [type: 'REF', value: 'rc1.storedValues.date'],
                        accuracy             : 'year',
                        minDate              : '-130y',
                        maxDate              : 'today',
                        validation           : [
                                [type: 'RegExp', value: '.+', errorMsg: 'Поле должно быть заполнено']
                        ]
                ]
        ]
        dateInputComponentSecond = [
                id      : "di2",
                type    : ComponentType.DateInput,
                required: true,
                attrs   : [
                        hint                 : 'дата окончания',
                        grid                 : 'grid-col-6 grid-col-12-sm',
                        brokenDateFixStrategy: 'restore',
                        accuracy             : 'year',
                        minDate              : '-130y',
                        maxDate              : 'today',
                        validation           : [
                                [type: 'RegExp', value: '.+', errorMsg: 'Поле должно быть заполнено']
                        ]
                ]
        ]
        calendarInputFieldComponent = [
                id   : "ci1",
                type : ComponentType.CalendarInput,
                attrs: [
                        hint                 : 'дата начала',
                        grid                 : 'grid-col-6 grid-col-12-sm',
                        brokenDateFixStrategy: 'restore',
                        components           : ['di1', 'di2'],
                        dateRestrictions     : [
                                [condition: '>=', type: 'const', value: '01.01.2018', forChild: 'di1'],
                                [condition: '<=', type: 'const', value: 'today', forChild: 'di1'],
                                [condition: '>=', type: 'ref', value: 'rc1', forChild: 'di2', precision: 'di1'],
                                [condition: '<=', type: 'ref', value: 'rc1', forChild: 'di2', precision: 'di1', operand: '+', amount: 5, period: 'days'],
                        ]
                ]
        ]

        componentRegistry = new ComponentRegistry()
        calendarInput = new CalendarInputComponent(componentRegistry)
        dateInput = new DateInputComponent(
                new InitialValueFromImpl(
                        new ParseAttrValuesHelper(
                                Mock(VariableRegistry), Mock(JsonProcessingService), Mock(ProtectedFieldService)
                        ),
                        Mock(CalculatedAttributesHelper)
                )
        )

        ComponentTestUtil.setAbstractComponentServices(calendarInput)
        ComponentTestUtil.setAbstractComponentServices(dateInput)

        componentRegistry.setComponents([calendarInput, dateInput])
        screenDescriptor = new ServiceDescriptor(applicationFields: [dateInputComponentFirst, dateInputComponentSecond])
        scenarioDto = new ScenarioDto([applicantAnswers: [
                'rc1': [visited: true, value: '{"storedValues": {"date": "01.01.2011"}}'] as ApplicantAnswer
        ]])
    }

    def 'Should inject child components'() {
        when:
        calendarInput.getInitialValue(calendarInputFieldComponent, scenarioDto, screenDescriptor)

        then:
        (calendarInputFieldComponent.attrs.components as List<?>).stream()
                .allMatch { it -> FieldComponent.isInstance(it) }
        (calendarInputFieldComponent.attrs.components as List<FieldComponent>).stream()
                .allMatch { it -> it.type == ComponentType.DateInput }
    }

    def 'Should return correct initial value calendarInput'() {
        when:
        def result = calendarInput.getInitialValue(calendarInputFieldComponent, scenarioDto, screenDescriptor)

        then:
        result.get() == '{"di2":"","di1":"01.01.2011"}'
    }

    def 'Should return correct initial value dateInput'() {
        when:
        scenarioDto = new ScenarioDto([applicantAnswers: [
                'rc1': [visited: true, value: '{"storedValues": {"date": "2011-11-01"}}'] as ApplicantAnswer
        ]])
        def result = dateInput.getInitialValue(dateInputComponentFirst, scenarioDto, screenDescriptor)

        then:
        result.get() == "11.01.2011"
    }

    def 'Should validate after submit'() {
        Map<String, String> incorrectAnswers = new HashMap<>()

        when:
        calendarInputFieldComponent.attrs.components = [
                JsonProcessingUtil.fromJson(JsonProcessingUtil.toJson(dateInputComponentFirst), new TypeReference<LinkedHashMap<String, Object>>() {}),
                JsonProcessingUtil.fromJson(JsonProcessingUtil.toJson(dateInputComponentSecond), new TypeReference<LinkedHashMap<String, Object>>() {})
        ]
        scenarioDto.display = [components: [calendarInputFieldComponent]]
        calendarInput.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, calendarInputFieldComponent)

        then:
        incorrectAnswers == result

        where:
        entry                                                                                                         || result
        AnswerUtil.createAnswerEntry('ci1', '{"di1": "2021-01-11T21:00:00.000Z", "di2": "2021-01-12T00:00:00.000Z"}') || [:]
        AnswerUtil.createAnswerEntry('ci1', '{"di1": "", "di2": "2021-01-12T00:00:00.000Z"}')                         || [di1: 'Значение не задано']
        AnswerUtil.createAnswerEntry('ci1', '{"di1": "2021-01-11T21:00:00.000Z", "di2": ""}')                         || [di2: 'Значение не задано']
        AnswerUtil.createAnswerEntry('ci1', '{"di1": "", "di2": ""}')                                                 || [di2: 'Значение не задано', di1: 'Значение не задано']
        AnswerUtil.createAnswerEntry('ci1', '')                                                                       || [ci1: 'Значение не задано']
    }
}
