package unit.ru.gosuslugi.pgu.fs.component.logic

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.BackRestCallResponseDto
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.component.logic.BackRestCallComponent
import ru.gosuslugi.pgu.fs.component.logic.RestCallComponent
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto
import ru.gosuslugi.pgu.fs.service.BackRestCallService
import ru.gosuslugi.pgu.fs.service.RestCallService
import spock.lang.Specification

class BackRestCallComponentSpec extends Specification {

    @SuppressWarnings("GroovyAccessibility")
    def test() {
        given:
        def scenarioDto = new ScenarioDto()
        def restCallService = Stub(BackRestCallService) {
            it.sendRequest(_ as RestCallDto) >> new BackRestCallResponseDto(200, Map.<String, Object> of("key", "value"))
        }
        def component = new BackRestCallComponent(Stub(RestCallService), restCallService, Mock(UserPersonalData))
        component.jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
        def fieldComponent = getFieldComponent()

        when:
        component.preProcess(fieldComponent, scenarioDto)
        def initialValue = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        component.getType() == ComponentType.BackRestCall
        initialValue == ComponentResponse.empty()
        scenarioDto.getApplicantAnswers().get(fieldComponent.getId()).getValue() == '{"statusCode":200,"errorMessage":null,"response":{"key":"value"}}'
        fieldComponent.getAttrs().isEmpty()
        fieldComponent.getLinkedValues().isEmpty()
        fieldComponent.getArguments().isEmpty()
    }

    def exceptions() {
        given:
        def restCallService = Stub(BackRestCallService) {
            it.sendRequest(_ as RestCallDto) >> { throw new ExternalServiceException(_ as String) }
        }
        def component = new BackRestCallComponent(Stub(RestCallService), restCallService, Mock(UserPersonalData))

        when:
        component.preProcess(getFieldComponent(), new ScenarioDto())

        then:
        thrown(ExternalServiceException.class)
    }

    def static getFieldComponent() {
        [
                id          : 'brc1',
                attrs       : [
                        method   : 'POST',
                        path     : '/',
                        esia_auth: false
                ] as Map,
                linkedValues: [["argument": "defaultArgument", "defaultValue": "100500"]] as List<LinkedValue>,
                arguments   : [queryArg: 'hello']
        ] as FieldComponent
    }
}