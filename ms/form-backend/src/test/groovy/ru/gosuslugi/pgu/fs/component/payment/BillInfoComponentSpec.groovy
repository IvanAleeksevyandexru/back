package ru.gosuslugi.pgu.fs.component.payment

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.components.ComponentAttributes
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.BaseComponent
import ru.gosuslugi.pgu.fs.component.payment.model.BillInfoComponentDto
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfo
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoAttr
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoError
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponse
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponseWrapper
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService
import spock.lang.Specification

class BillInfoComponentSpec extends Specification {
    BillingService billingServiceMock
    UserPersonalData userPersonalData

    def setup() {
        billingServiceMock = Mock(BillingService)
        userPersonalData = Mock(UserPersonalData)
        userPersonalData.getToken() >> "token"
        userPersonalData.getUserId() >> { 1000298933L }
    }

    def 'Can get initial value'() {
        given:
        BaseComponent<BillInfoComponentDto> billInfoComponent = new BillInfoComponent(billingServiceMock,userPersonalData)
        FieldComponent component = new FieldComponent(arguments: Map.of(ComponentAttributes.BILL_ID_ATTR, "billId"))
        BillInfoResponseWrapper billInfoResponseWrapper = new BillInfoResponseWrapper(
                response: new BillInfoResponse(bills:[new BillInfo(billId: '12', billNumber: '18', billName: "someBillName", billDate: "2021-01-31T00:07:20.000+00:00", isPaid: false, amount: 80, addAttrs: [new BillInfoAttr("имя","тайтл","велью")])]), error: new BillInfoError(code: 0))
        billingServiceMock.getBillInfo(_ as String, _ as String) >> billInfoResponseWrapper
        ScenarioDto scenarioDto = new ScenarioDto([applicantAnswers: [
                'rc1': [visited: true, value: '{"storedValues": {"date": "01.01.2011"}}'] as ApplicantAnswer
        ]])

        when:
        def actual = billInfoComponent.getInitialValue(component,scenarioDto)

        then:
        "80" == actual.get().getAmount()
        "18" == actual.get().getBillNumber()
        "12" == actual.get().getBillId()
        "someBillName" == actual.get().getBillName()
        "2021-01-31T00:07:20.000+00:00" == actual.get().getBillDate()

    }

}
