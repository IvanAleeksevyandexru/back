package ru.gosuslugi.pgu.fs.component.logic

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.dto.lk.LkDataMessage
import ru.gosuslugi.pgu.fs.service.LkNotifierService
import spock.lang.Specification

class AnalyticNotifierComponentSpec extends Specification {
    private static AnalyticNotifierComponent analyticNotifierComponent
    private static LkNotifierService lkNotifierService


    def setup() {
        lkNotifierService = Mock(LkNotifierService)
        analyticNotifierComponent = new AnalyticNotifierComponent(lkNotifierService)
    }

    def 'component type is ValueCalculator'() {
        expect:
        analyticNotifierComponent.getType() == ComponentType.AnalyticNotifier
    }

    def "messages sent"() {
        given:
        def fieldComponent = getFieldComponent([fieldName1: 'hello', fieldValue1: 'world', fieldMnemonic1: 'mnmnc',
                                                fieldName2: 'goodbye', fieldValue2: 'world', fieldMnemonic2: 'eoi'])
        def scenarioDto = getScenarioDto()

        when:
        analyticNotifierComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        1 * lkNotifierService.sendMessages(12345, [
                new LkDataMessage(
                        fieldName: 'hello',
                        fieldValue: 'world',
                        fieldMnemonic: 'mnmnc'
                ),
                new LkDataMessage(
                        fieldName: 'goodbye',
                        fieldValue: 'world',
                        fieldMnemonic: 'eoi'
                )
        ])
    }

    def "no messages sent with missing arguments"() {
        given:
        def fieldComponent = getFieldComponent([fieldValue1: 'qwer', fieldMnemonic1: 'ty',
                                                fieldName2: 'asdf', fieldMnemonic2: 'gh',
                                                fieldName3: 'zxcv', fieldValue3: 'bn'])
        def scenarioDto = getScenarioDto()

        when:
        analyticNotifierComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        0 * lkNotifierService.sendMessages(_, _)
    }

    def static getFieldComponent(args) {
        new FieldComponent(
                id: 'an',
                type: ComponentType.AnalyticNotifier,
                arguments: args
        )
    }

    def static getScenarioDto() {
        new ScenarioDto(
                orderId: 12345
        )
    }
}
