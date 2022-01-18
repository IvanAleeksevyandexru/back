package unit.ru.gosuslugi.pgu.fs.service.impl

import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.types.SubHeader
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.impl.DisplayReferenceServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.OrderIdVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableType
import spock.lang.Specification

class DisplayReferenceServiceImplSpec extends Specification {

    DisplayReferenceService service

    private static def orderId = 1234567890

    def setup() {
        def jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
        service = new DisplayReferenceServiceImpl(
                jsonProcessingService,
                Mock(LinkedValuesService),
                Mock(OrderIdVariable) {
                    it.getType() >> VariableType.orderId
                    it.getValue(_ as ScenarioDto) >> orderId.toString()
                }
        )
    }

    def 'Can set field values based on placeholders'() {
        given:
        def display = new DisplayRequest(header: 'Hello world!')
        def scenario = new ScenarioDto(
                orderId: orderId,
                applicantAnswers: [pd1: new ApplicantAnswer(value: "{\"storedValues\":{\"firstName\":\"Michael\",\"lastName\":\"Douglas\"}}")]
        )

        when: 'Display without refs'
        service.processDisplayRefs(display, scenario)

        then:
        display.header == 'Hello world!'
        display.subHeader == null

        when: 'Display header with placeholders'
        display.header = 'Hello ${firstName} ${lastName}!'
        display.attrs = [refs: [firstName: 'pd1.value.storedValues.firstName', lastName: 'pd1.value.storedValues.lastName']]
        service.processDisplayRefs(display, scenario)

        then:
        display.header == 'Hello Michael Douglas!'

        when: 'Display subheader with placeholders'
        display.subHeader = new SubHeader(text: 'Hello ${firstName} ${lastName}! Your orderId: ${orderId}')
        service.processDisplayRefs(display, scenario)

        then:
        display.subHeader.text == "Hello Michael Douglas! Your orderId: ${orderId}"
    }

}
