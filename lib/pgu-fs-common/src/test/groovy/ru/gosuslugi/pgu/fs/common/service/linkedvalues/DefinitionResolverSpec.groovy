package ru.gosuslugi.pgu.fs.common.service.linkedvalues


import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.dto.descriptor.types.ValueDefinition
import ru.gosuslugi.pgu.fs.common.service.impl.DefinitionResolver
import spock.lang.Specification

import java.time.LocalDate

class DefinitionResolverSpec extends Specification {

    def 'it should work when calling type resolving functions'() {
        given:
        def strategy = [] as DefinitionResolver
        def valueDefinition = [
                arg1       : argument,
                expression1: expression
        ] as ValueDefinition
        def linkedValue = [definition: valueDefinition] as LinkedValue
        def scenarioDto = [
                applicantAnswers: [
                        'rc1': [value: '{"root":{"array":[{"key":"value1"},{"key":"value2"},{"key":"value3"}]}}'] as ApplicantAnswer
                ] as Map<String, ApplicantAnswer>
        ] as ScenarioDto

        when:
        def result = strategy.compute(linkedValue, scenarioDto)

        then:
        result == expected

        where:
        argument              | expression                     || expected
        '1'                   | 'arg1'                         || 1
        '\'text\''            | 'arg1'                         || 'text'
        '{"key":"value"}'     | 'arg1'                         || [key: 'value'] as Map<String, Object>
        '\'{"key":"value"}\'' | 'asMap(arg1)'                  || [key: 'value'] as Map<String, Object>
        '\'[1,2,3]\''         | 'asList(arg1)'                 || [1, 2, 3] as List<Integer>
        '{"key":"value"}'     | 'asJson(arg1)'                 || '{"key":"value"}'
        '\'[1,2,3]\''         | 'asJson(asList(arg1))'         || '[1,2,3]'
        '\'2021-01-01\''      | 'asDate(arg1)'                 || LocalDate.parse('2021-01-01')
        '\'01.01.2021\''      | 'asDate(arg1, \'dd.MM.yyyy\')' || LocalDate.parse('2021-01-01')
        '1'                   | 'asString(arg1)'               || '1'
    }

    def 'it should work when calling \'size()\' function on list argument'() {
        given:
        def strategy = [] as DefinitionResolver
        def valueDefinition = [
                arg1       : 'asList(\'${rc1.root.array}\')',
                expression1: 'arg1.size()'
        ] as ValueDefinition
        def linkedValue = [definition: valueDefinition] as LinkedValue
        def scenarioDto = [
                applicantAnswers: [
                        'rc1': [value: '{"root":{"array":[{"key":"value1"},{"key":"value2"},{"key":"value3"}]}}'] as ApplicantAnswer
                ] as Map<String, ApplicantAnswer>
        ] as ScenarioDto

        when:
        def result = strategy.compute(linkedValue, scenarioDto)

        then:
        result == 3
    }

    def 'it should work when calling \'get()\' function on list argument'() {
        given:
        def strategy = [] as DefinitionResolver
        def valueDefinition = [
                arg1       : 'asList(\'${rc1.root.array}\')',
                expression1: 'arg1.get(0)'
        ] as ValueDefinition
        def linkedValue = [definition: valueDefinition] as LinkedValue
        def scenarioDto = [
                applicantAnswers: [
                        'rc1': [value: '{"root":{"array":[{"key":"value1"},{"key":"value2"},{"key":"value3"}]}}'] as ApplicantAnswer
                ] as Map<String, ApplicantAnswer>
        ] as ScenarioDto

        when:
        def result = strategy.compute(linkedValue, scenarioDto)

        then:
        result == [key: 'value1'] as Map<String, String>
    }

    def 'it should work when calling \'get()[\'key\']\' function on list argument with map inside'() {
        given:
        def strategy = [] as DefinitionResolver
        def valueDefinition = [
                arg1       : 'asList(\'${rc1.root.array}\')',
                expression1: 'arg1.get(0)[\'key\']'
        ] as ValueDefinition
        def linkedValue = [definition: valueDefinition] as LinkedValue
        def scenarioDto = [
                applicantAnswers: [
                        'rc1': [value: '{"root":{"array":[{"key":"value1"},{"key":"value2"},{"key":"value3"}]}}'] as ApplicantAnswer
                ] as Map<String, ApplicantAnswer>
        ] as ScenarioDto

        when:
        def result = strategy.compute(linkedValue, scenarioDto)

        then:
        result == 'value1'
    }

    def 'it should work when calling \'anyMatch\' function'() {
        given:
        def strategy = [] as DefinitionResolver
        def valueDefinition = [
                arg1       : 'asList(\'${rc1.root.array}\')',
                expression1: 'anyMatch(arg1, \'key\', \'value1\')'
        ] as ValueDefinition
        def linkedValue = [definition: valueDefinition] as LinkedValue
        def scenarioDto = [
                applicantAnswers: [
                        'rc1': [value: '{"root":{"array":[{"key":"value1"},{"key":"value2"},{"key":"value3"}]}}'] as ApplicantAnswer
                ] as Map<String, ApplicantAnswer>
        ] as ScenarioDto

        when:
        def result = strategy.compute(linkedValue, scenarioDto)

        then:
        result == true
    }

    def 'it should work when calling \'anyMatch\' function (complex)'() {
        given:
        def strategy = [] as DefinitionResolver
        def valueDefinition = [
                arg1       : 'asList(\'${rc1.root.array}\')',
                arg2       : 'asString(\'key\')',
                arg3       : 'asString(\'value1\')',
                arg4       : 'asList(\'${rc1.root.array[?(@.key == \'value1\')]}\').get(0)',
                arg5       : 'asString(\'${rc1.root.array[1]}\')',
                expression1: 'anyMatch(arg1, arg2, arg3) ? asString(arg4) : arg5'
        ] as ValueDefinition
        def linkedValue = [definition: valueDefinition] as LinkedValue
        def scenarioDto = [
                applicantAnswers: [
                        'rc1': [value: '{"root":{"array":[{"key":"value1"},{"key":"value2"},{"key":"value3"}]}}'] as ApplicantAnswer
                ] as Map<String, ApplicantAnswer>
        ] as ScenarioDto

        when:
        def result = strategy.compute(linkedValue, scenarioDto)

        then:
        result == String.valueOf([key: 'value1'] as Map<String, Object>)
    }

    def 'it should work when calling \'noneMatch\' function'() {
        given:
        def strategy = [] as DefinitionResolver
        def valueDefinition = [
                arg1       : 'asList(\'${rc1.root.array}\')',
                expression1: 'noneMatch(arg1, \'key\', \'value4\')'
        ] as ValueDefinition
        def linkedValue = [definition: valueDefinition] as LinkedValue
        def scenarioDto = [
                applicantAnswers: [
                        'rc1': [value: '{"root":{"array":[{"key":"value1"},{"key":"value2"},{"key":"value3"}]}}'] as ApplicantAnswer
                ] as Map<String, ApplicantAnswer>
        ] as ScenarioDto

        when:
        def result = strategy.compute(linkedValue, scenarioDto)

        then:
        result == true
    }

    def 'it should work when calling \'allMatch\' function'() {
        given:
        def strategy = [] as DefinitionResolver
        def valueDefinition = [
                arg1       : 'asList(\'${rc1.root.array}\')',
                expression1: 'anyMatch(arg1, \'key\', \'value\')'
        ] as ValueDefinition
        def linkedValue = [definition: valueDefinition] as LinkedValue
        def scenarioDto = [
                applicantAnswers: [
                        'rc1': [value: '{"root":{"array":[{"key":"value"},{"key":"value"},{"key":"value"}]}}'] as ApplicantAnswer
                ] as Map<String, ApplicantAnswer>
        ] as ScenarioDto

        when:
        def result = strategy.compute(linkedValue, scenarioDto)

        then:
        result == true
    }
}
