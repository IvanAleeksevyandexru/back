package unit.ru.gosuslugi.pgu.fs.service.impl

import com.jayway.jsonpath.DocumentContext
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.components.descriptor.attr_factory.AttrsFactory
import ru.gosuslugi.pgu.dto.*
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswers
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.impl.ComponentReferenceServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.UserCookiesServiceImpl
import spock.lang.Specification

class ComponentReferenceServiceImplSpec extends Specification {

    ComponentReferenceService service
    def timezoneUserService = new UserCookiesServiceImpl()
    def request = [] as MockHttpServletRequest

    def setup() {
        RequestContextHolder.setRequestAttributes([request] as ServletRequestAttributes)
        def jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
        def linkedValuesService = new LinkedValuesService() {
            @Override
            void fillLinkedValues(FieldComponent fieldComponent, ScenarioDto scenarioDto, DocumentContext... externalContexts) {}

            @Override
            void fillLinkedValues(DisplayRequest displayRequest, ScenarioDto scenarioDto) {

            }

            @Override
            String getValue(LinkedValue linkedValue, ScenarioDto scenarioDto, AttrsFactory attrsFactory, DocumentContext... externalContexts) {
                return null
            }
        }
        service = new ComponentReferenceServiceImpl(jsonProcessingService, timezoneUserService, linkedValuesService)
    }

    def cleanup() {
        timezoneUserService.setUserTimezone(null)
    }

    def 'Can get form for empty component attribute'() {
        given:
        def component = new FieldComponent()
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states.isEmpty()
    }

    def 'Can get form for component without placeholders'() {
        given:
        def component = new FieldComponent(
                label: 'label1',
                value: 'value1')
        def scenarioDto = new ScenarioDto()

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label1'
        component.value == 'value1'
    }

    def 'Can get form for component FieldGroups without placeholders'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'value1'],
                                         [label: 'label2', value: 'value2']]]]])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states.size() == 1
        formDto.states[0].fields.size() == 2

        def group = ((List) component.attrs['fieldGroups'])[0]
        formDto.states[0].groupName == group?.getAt('groupName')

        def state = ((List) group?.getAt('fields'))[0]
        formDto.states[0].fields[0].label == state?.getAt('label')
        formDto.states[0].fields[0].value == state?.getAt('value')
    }

    def 'Can get form for component with not exists placeholder'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${notExistArgument1}',
                value: 'value - ${notExistArgument2}',
                arguments: [argument1: 'value1'])
        def scenarioDto = new ScenarioDto()

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - ${notExistArgument1}'
        component.value == 'value - ${notExistArgument2}'
    }

    def 'Can get form for component with empty placeholder'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${}',
                value: 'value - ${}',
                arguments: [argument1: 'value1'])
        def scenarioDto = new ScenarioDto()

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - ${}'
        component.value == 'value - ${}'
    }

    def 'Can get form for component FieldGroups with not exists placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label2', value: 'Value - ${notExistArgument}']]]]],
                arguments: [argument1: 'value1'])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value - ${notExistArgument}'
    }

    def 'Can get form for component FieldGroups with empty placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label2', value: 'Value - ${}']]]]],
                arguments: [argument1: 'value1'])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value - ${}'
    }

    def 'Can get form for component with argument placeholder'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${argument1}',
                value: 'value - ${argument2}',
                arguments: [argument1: 'value1', argument2: 'value2'])
        def scenarioDto = new ScenarioDto()

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - value1'
        component.value == 'value - value2'
    }

    def 'Can get form for component with repeated argument placeholder'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${argument1} and ${argument1}',
                value: 'value - ${argument1}',
                arguments: [argument1: 'value1'])
        def scenarioDto = new ScenarioDto()

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - value1 and value1'
        component.value == 'value - value1'
    }

    def 'Can get form for component FieldGroups with argument placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name - ${argument1}',
                                 fields   : [
                                         [label: 'label1', value: 'Value with argument - ${argument1}']]]]],
                arguments: [argument1: 'value1'])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].groupName == 'group name - value1'
        formDto.states[0].fields[0].value == 'Value with argument - value1'
    }

    def 'Can get form for component with direct ref'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${pd1.value.storedValues.firstName}',
                value: 'value - ${pd1.value.storedValues.lastName}')
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\",\"lastName\":\"Ivanov\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - Tom'
        component.value == 'value - Ivanov'
    }

    def 'Can get form for component FieldGroups with direct ref'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value with ref - ${pd1.value.storedValues.firstName}']]]]])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\"}}")])

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value with ref - Tom'
    }

    def 'Can get form for component with incorrect direct ref'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${pd1.value.storedValues}',
                value: 'value - ${pd1.value}')
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\",\"lastName\":\"Ivanov\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - {"firstName":"Tom","lastName":"Ivanov"}'
        component.value == 'value - {"storedValues":{"firstName":"Tom","lastName":"Ivanov"}}'
    }

    def 'Can get form for component FieldGroups with incorrect direct ref'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value with ref - ${pd1.value.storedValues}']]]]])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\"}}")])

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value with ref - {"firstName":"Tom"}'
    }

    def 'Can get form for component with ref placeholder'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${pd1}',
                value: 'value - ${pd2}',
                attrs: [
                        refs : [
                                pd1: 'pd1.value.storedValues.firstName',
                                pd2: 'pd1.value.storedValues.lastName']])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\",\"lastName\":\"Ivanov\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - Tom'
        component.value == 'value - Ivanov'
    }

    def 'Can get form for component FieldGroups with ref placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value with ref - ${pd1}']]]],
                        refs       : [pd1: 'pd1.value.storedValues.firstName']])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\"}}")])

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value with ref - Tom'
    }

    def 'Can get form for component with direct ref in argument placeholder'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${argument1}',
                value: 'value - ${argument2}',
                arguments: [argument1: '${pd1.value.storedValues.firstName}', argument2: '${pd1.value.storedValues.lastName}'])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\",\"lastName\":\"Ivanov\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - Tom'
        component.value == 'value - Ivanov'
    }

    def 'Can get form for component FieldGroups with direct ref in argument placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value with ref - ${argument1}']]]]],
                arguments: [argument1: '${pd1.value.storedValues.firstName}'])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\"}}")])

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value with ref - Tom'
    }

    def 'Can get form for component with ref in argument placeholder'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${argument1}',
                value: 'value - ${argument2}',
                attrs: [
                        refs : [
                                pd1: 'pd1.value.storedValues.firstName',
                                pd2: 'pd1.value.storedValues.lastName']],
                arguments: [argument1: '${pd1}', argument2: '${pd2}'])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\",\"lastName\":\"Ivanov\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - Tom'
        component.value == 'value - Ivanov'
    }

    def 'Can get form for component FieldGroups with ref in argument placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value with ref - ${argument1}']]]],
                        refs       : [pd1: 'pd1.value.storedValues.firstName']],
                arguments: [argument1: '${pd1}'])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\"}}")])

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value with ref - Tom'
    }

    def 'Can get form for component with preset ref placeholder'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${reference_to_order_id}',
                value: 'value - ${reference_to_master_order_id}')
        def scenarioDto = new ScenarioDto(masterOrderId: 123L, orderId: 321L)

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - 321'
        component.value == 'value - 123'
    }

    def 'Can get form for component FieldGroups with preset ref placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value with ref - ${reference_to_order_id}, ${reference_to_master_order_id}']]]]])
        def scenarioDto = new ScenarioDto(masterOrderId: 123L, orderId: 321L)

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value with ref - 321, 123'
    }

    def 'Can get form for component FieldGroups with cycled ref placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value - ${nameRef} ${lastNameRef}']],
                                 attrs    : [cycledAnswerId: 'ai7']]],
                        refs       : [nameRef    : 'bd6.value.storedValues.firstName',
                                      lastNameRef: 'bd6.value.storedValues.lastName']])
        def scenarioDto = new ScenarioDto(
                applicantAnswers: [confirm_data: new ApplicantAnswer(value: "1")],
                cycledApplicantAnswers: new CycledApplicantAnswers(
                        currentAnswerId: 'ai7',
                        answers: [
                                new CycledApplicantAnswer(
                                        id: "ai7",
                                        items: [
                                                new CycledApplicantAnswerItem(
                                                        id: "1",
                                                        itemAnswers: [bd6: new ApplicantAnswer(
                                                                value: "{\"storedValues\":{\"firstName\":\"Tom\",\"lastName\":\"Hanks\"}}")]),
                                                new CycledApplicantAnswerItem(
                                                        id: "2",
                                                        itemAnswers: [bd6: new ApplicantAnswer(
                                                                value: "{\"storedValues\":{\"firstName\":\"Michael\",\"lastName\":\"Douglas\"}}")])])]))
        def formDto

        when: "All cycled answers"
        formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states.size() == 2
        formDto.states[0].fields[0].value == 'Value - Tom Hanks'
        formDto.states[1].fields[0].value == 'Value - Michael Douglas'

        when: "Cycled answer with index"
        ((Map)((List) component.attrs.get("fieldGroups"))[0]["attrs"]).put('cycledAnswerIndex', "confirm_data.value")
        formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states.size() == 1
        formDto.states[0].fields[0].value == 'Value - Tom Hanks'
    }

    def 'Can get form for component FieldGroups with cycled ref placeholder with index'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value with ref - ${pd1}']]]],
                        refs       : [pd1: 'pd1.value.storedValues.firstName']])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Tom\"}}")])

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value with ref - Tom'
    }

    def 'Can get form for component FieldGroups with sequential placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups : [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value - ${index}'],
                                         [label: 'label2', value: 'Value - ${index}']]]],
                        placeholders: [index: 'sequential']])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value - 1'
        formDto.states[0].fields[1].value == 'Value - 2'
    }

    def 'Can get form for component FieldGroups with hidden empty groups and fields sequential placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        "hiddenEmptyGroups": true,
                        "hiddenEmptyFields": true,
                        fieldGroups : [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value - ${index}'],
                                         [label: 'label2', value: 'Value - ${index}'],
                                         [label: 'label3', value: '']
                                 ]],
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label4', value: ''],
                                         [label: 'label5', value: '']
                                 ]],
                        ],
                        placeholders: [index: 'sequential']])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states
        formDto.states.size() == 1
        formDto.states[0].fields
        formDto.states[0].fields.size() == 2
        formDto.states[0].fields[0].value == 'Value - 1'
        formDto.states[0].fields[1].value == 'Value - 2'
    }

    def 'Can get form for component FieldGroups with showing empty groups and empty fields sequential placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        "hiddenEmptyGroups": false,
                        "hiddenEmptyFields": false,
                        fieldGroups : [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value - ${index}'],
                                         [label: 'label2', value: 'Value - ${index}'],
                                         [label: 'label3', value: '']
                                 ]],
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label4', value: ''],
                                         [label: 'label5', value: '']
                                 ]],
                        ],
                        placeholders: [index: 'sequential']])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states
        formDto.states.size() == 2
        formDto.states[0].fields
        formDto.states[0].fields.size() == 3
        formDto.states[0].fields[0].value == 'Value - 1'
        formDto.states[0].fields[1].value == 'Value - 2'
        formDto.states[0].fields[2].value == null
        formDto.states[1].fields[0].value == null
        formDto.states[1].fields[1].value == null
    }

    def 'Can get form for component FieldGroups with hidden empty groups and show empty fields sequential placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        "hiddenEmptyGroups": true,
                        "hiddenEmptyFields": false,
                        fieldGroups : [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value - ${index}'],
                                         [label: 'label2', value: 'Value - ${index}'],
                                         [label: 'label3', value: '']
                                 ]],
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label4', value: ''],
                                         [label: 'label5', value: '']
                                 ]],
                        ],
                        placeholders: [index: 'sequential']])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states
        formDto.states.size() == 1
        formDto.states[0].fields
        formDto.states[0].fields.size() == 3
        formDto.states[0].fields[0].value == 'Value - 1'
        formDto.states[0].fields[1].value == 'Value - 2'
        formDto.states[0].fields[2].value == null
    }

    def 'Can get form for component FieldGroups with show empty groups(hidden empty fields overload this value) and hidden empty fields sequential placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        "hiddenEmptyGroups": false,
                        "hiddenEmptyFields": true,
                        fieldGroups : [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value - ${index}'],
                                         [label: 'label2', value: 'Value - ${index}'],
                                         [label: 'label3', value: '']
                                 ]],
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label4', value: ''],
                                         [label: 'label5', value: '']
                                 ]],
                        ],
                        placeholders: [index: 'sequential']])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states
        formDto.states.size() == 1
        formDto.states[0].fields
        formDto.states[0].fields.size() == 2
        formDto.states[0].fields[0].value == 'Value - 1'
        formDto.states[0].fields[1].value == 'Value - 2'
    }

    def 'Can get form component FieldGroups with nested placeholder'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value: ${argument1}']]]]],
                arguments: [argument1: 'argument2 - ${argument2}', argument2: 'argument2Value'])
        def scenarioDto = new ScenarioDto()

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value: argument2 - argument2Value'
    }

    def 'Can get form for component with date ref converter - without user timezone'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${pd1}',
                value: 'value - ${pd2}',
                attrs: [
                        refs : [
                                pd1: [path: 'pd1.value.storedValues.date', converter: 'DATE', format: 'dd MMMM yyyy \'г. в\' HH:mm'],
                                pd2: [path: 'pd1.value.storedValues.date', converter: 'DATE', format: 'yyyy-MMMM-dd \'at\' hh:mm:ss', 'locale': 'en']]])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"date\":\"2020-11-16T16:00:00.000+06:00\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - 16 ноября 2020 г. в 16:00'
        component.value == 'value - 2020-November-16 at 04:00:00'
    }

    def 'Can get form for component with date ref converter - default value'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${pd1}',
                value: 'value - ${pd2}',
                attrs: [
                        refs : [
                                pd1: [path: 'pd1.value.storedValues.date', converter: 'DATE', format: 'dd MMMM yyyy \'г. в\' HH:mm', default: "No value"],
                                pd2: [path: 'pd1.value.storedValues.date', converter: 'DATE', format: 'yyyy-MMMM-dd \'at\' hh:mm:ss', 'locale': 'en']]])
        def scenarioDto = new ScenarioDto(applicantAnswers: [other: new ApplicantAnswer(value: "{\"storedValues\":{\"date\":\"2020-11-16T16:00:00.000+06:00\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - No value'
        component.value == 'value - pd1.value.storedValues.date'

        where:
        defaultValue | label
        'No value'   | 'label - No value'
        ''           | 'label - '
    }

    def 'Can get form for component with spel ref converter'() {
        given:
        def component = new FieldComponent(
                label: '${fName} ${lName}.',
                attrs: [
                        refs : [
                                fName: 'pd1.value.storedValues.firstName',
                                lName: [path: 'pd1.value.storedValues.lastName', converter: 'SPEL', expression: 'charAt(0)']]])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Michael\",\"lastName\":\"Douglas\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'Michael D.'
    }

    def 'Can get form for component with date ref converter - with user timezone'() {
        given:
        timezoneUserService.setUserTimezone("+3")
        def component = new FieldComponent(
                label: 'label - ${pd1}',
                value: 'value - ${pd2}',
                attrs: [
                        refs : [
                                pd1: [path: 'pd1.value.storedValues.date', converter: 'DATE', format: 'dd MMMM yyyy \'г. в\' HH:mm'],
                                pd2: [path: 'pd1.value.storedValues.date', converter: 'DATE', format: 'yyyy-MMMM-dd \'at\' hh:mm:ss', 'locale': 'en']]])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"date\":\"2020-11-16T16:00:00.000+06:00\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - 16 ноября 2020 г. в 13:00'
        component.value == 'value - 2020-November-16 at 01:00:00'
    }

    def 'Can get form for component with date ref converter - date row have incorrect value'() {
        given:
        def component = new FieldComponent(
                label: 'label - ${pd1}',
                value: 'value - ${pd2}',
                attrs: [
                        refs : [
                                pd1: [path: 'pd1.value.storedValues.date', converter: 'DATE', format: 'dd MMMM yyyy \'г. в\' HH:mm'],
                                pd2: [path: 'pd1.value.storedValues.date', converter: 'DATE', format: 'yyyy-MMMM-dd \'at\' hh:mm:ss', 'locale': 'en']]])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"date\":\"2020-11-16T16:00:00.000\"}}")])

        when:
        service.processComponentRefs(component, scenarioDto)

        then:
        component.label == 'label - 16 ноября 2020 г. в 16:00'
        component.value == 'value - 2020-November-16 at 04:00:00'
    }

    def 'Can get form for component FieldGroups with date ref converter'() {
        given:
        def component = new FieldComponent(
                attrs: [
                        fieldGroups: [
                                [groupName: 'group name',
                                 fields   : [
                                         [label: 'label1', value: 'Value with ref - ${pd1}']]]],
                        refs       : [pd1: [path: 'pd1.value.storedValues.date', converter: 'DATE', format: 'dd MMM yyyy \'в\' HH:mm']]])
        def scenarioDto = new ScenarioDto(applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"date\":\"2020-11-16T16:00:00.000+06:00\"}}")])

        when:
        def formDto = service.processFieldGroups(component, scenarioDto).build()

        then:
        formDto.states[0].fields[0].value == 'Value with ref - 16 нояб. 2020 в 16:00'
    }

    /**
     * Специальный тест для
     */
//    def 'Can get form for screen: "Data on applicants" for registration service'() {
//        given:
//
//        when:
//
//        then:
//
//    }

}
