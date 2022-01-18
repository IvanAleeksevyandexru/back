package ru.gosuslugi.pgu.fs.action.impl

import org.springframework.http.MediaType
import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.action.ActionRequestDto
import ru.gosuslugi.pgu.dto.action.ActionResponseDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService
import spock.lang.Specification


class DownloadBillPdfActionSpec extends Specification {
    private BillingService  billingService = Mock(BillingService)
    private DownloadBillPdfAction downloadBillPdfAction = new DownloadBillPdfAction(billingService)

    def "testInvoke"() {
        given:
        FieldComponent component = new FieldComponent(arguments: Map.of(ComponentAttributes.BILL_ID_ATTR, "billId"))
        DisplayRequest display = new DisplayRequest(components: [component])
        billingService.getBillPdfURI("billId") >> "billPdfURI"
        ActionRequestDto requestDto = new ActionRequestDto(scenarioDto: new ScenarioDto(display: display))

        when:
        ActionResponseDto result = downloadBillPdfAction.invoke(requestDto)

        then:
        result.responseData != null
        result.responseData.get("value") == "billPdfURI"
        result.responseData.get("type") == MediaType.APPLICATION_PDF_VALUE
    }
}
