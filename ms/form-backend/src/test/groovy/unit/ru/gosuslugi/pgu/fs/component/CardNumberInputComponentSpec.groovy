package unit.ru.gosuslugi.pgu.fs.component

import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.input.CardNumberInputComponent
import spock.lang.Specification

class CardNumberInputComponentSpec extends Specification {
    CardNumberInputComponent cardNumberInputComponent = new CardNumberInputComponent()

    def 'Can validate card value after submit'() {
        given:
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry('value',
                '{"2200 3307 9345 4721809"}')
        ScenarioDto scenarioDto = new ScenarioDto()
        def fieldComponent = new FieldComponent(
                type: ComponentType.CardNumberInput,
                attrs: [dictionaryType: 'country'])

        when:
        cardNumberInputComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.isEmpty()

        when:
        incorrectAnswers = [:]
        entry = AnswerUtil.createAnswerEntry('value', '["5469 3800 2401 6155"]')
        cardNumberInputComponent.validateAfterSubmit(incorrectAnswers, entry, Stub(ScenarioDto), fieldComponent)

        then:
        incorrectAnswers.isEmpty()

        when:
        incorrectAnswers = [:]
        entry = AnswerUtil.createAnswerEntry('value', '["5439 3800 2401 6155"]')
        cardNumberInputComponent.validateAfterSubmit(incorrectAnswers, entry, Stub(ScenarioDto), fieldComponent)

        then:
        !incorrectAnswers.isEmpty()
        incorrectAnswers['value'] == 'Такой карты не существует'
    }
}
