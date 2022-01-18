package unit.ru.gosuslugi.pgu.fs.component.payment

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.payment.PaymentWayComponent
import ru.gosuslugi.pgu.fs.component.payment.model.PaymentWayDto
import spock.lang.Specification

class PaymentWayComponentSpec extends Specification {
    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
    LinkedValuesService linkedValuesService = ComponentTestUtil.getLinkedValuesService(jsonProcessingService)
    PaymentWayComponent paymentWayComponent

    def 'Check radiobuttons initialization' () {
        given:
        def json = JsonFileUtil.getJsonFromFile(this.getClass(), "-childrenClub.json")
        FieldComponent fieldComponent = getFieldComponent(json)
        ScenarioDto scenarioDto = new ScenarioDto(orderId: 1L)
        linkedValuesService.fillLinkedValues(fieldComponent, scenarioDto)
        paymentWayComponent = new PaymentWayComponent()

        when:
        ComponentResponse result = paymentWayComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        result == ComponentResponse.empty()
        List<PaymentWayDto> realResults = (List<PaymentWayDto>)fieldComponent.getAttrs()["paymentWays"]
        List<PaymentWayDto>  expectedResults = [
                new PaymentWayDto(paymentType: 'pfdod_certificate', amount: 1200.00),
                new PaymentWayDto(paymentType: 'private', amount: 1000.00),
                new PaymentWayDto(paymentType: 'budget', programType: 'other')
        ]
        realResults.containsAll(expectedResults)
        expectedResults.containsAll(realResults)
    }

    static def getFieldComponent(String json) {
        LinkedValue linkedValue = new LinkedValue(
                argument: 'value',
                defaultValue: json
        )
        new FieldComponent(
                id: 'd15',
                type: ComponentType.PaymentWay,
                label: 'Оплата',
                linkedValues: [
                        linkedValue
                ],
                attrs: [
                        paymentWays:[]
                ],
                value: ''
        )
    }
}
