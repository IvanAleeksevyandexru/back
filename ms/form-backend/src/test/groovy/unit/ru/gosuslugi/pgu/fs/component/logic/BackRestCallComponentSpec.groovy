package unit.ru.gosuslugi.pgu.fs.component.logic


import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.component.logic.BackRestCallComponent
import ru.gosuslugi.pgu.fs.component.logic.RestCallComponent
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto
import ru.gosuslugi.pgu.fs.service.BackRestCallService
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class BackRestCallComponentSpec extends Specification {

    def test() {
        given:
        def scenarioDto = new ScenarioDto()
        def expectedValue = _ as String
        def restCallComponent = new RestCallComponent(_ as String)
        def restCallService = Stub(BackRestCallService) {
            it.sendRequest(_ as RestCallDto) >> expectedValue
        }
        def component = new BackRestCallComponent(restCallComponent, restCallService)
        def fieldComponent = getFieldComponent()

        when:
        def initialValue = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        component.getType() == ComponentType.BackRestCall
        initialValue == ComponentResponse.empty()
        scenarioDto.getApplicantAnswers().get(fieldComponent.getId()).getValue() == expectedValue
        fieldComponent.getAttrs().isEmpty()
        fieldComponent.getLinkedValues().isEmpty()
        fieldComponent.getArguments().isEmpty()
    }

    def exceptions() {
        given:
        def restCallComponent = new RestCallComponent(_ as String)
        def restCallService = Stub(BackRestCallService) {
            it.sendRequest(_ as RestCallDto) >> { throw new ExternalServiceException(_ as String) }
        }
        def component = new BackRestCallComponent(restCallComponent, restCallService)

        when:
        component.getInitialValue(getFieldComponent(), new ScenarioDto())

        then:
        thrown(ExternalServiceException.class)
    }

    def static getFieldComponent() {
        [
                id          : 'brc1',
                attrs       : [
                        method: 'POST',
                        path  : '/',
                ] as Map,
                linkedValues: [["argument": "defaultArgument", "defaultValue": "100500"]] as List<LinkedValue>,
                arguments   : [queryArg: 'hello']
        ] as FieldComponent
    }
}
