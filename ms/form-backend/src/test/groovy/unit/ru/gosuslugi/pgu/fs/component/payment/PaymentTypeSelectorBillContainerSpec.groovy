package unit.ru.gosuslugi.pgu.fs.component.payment

import com.fasterxml.jackson.databind.ObjectMapper
import ru.atc.carcass.security.rest.model.person.Person
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.payment.PaymentTypeSelectorComponent
import ru.gosuslugi.pgu.fs.component.payment.strategy.BillContainerService
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfo
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoError
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponse
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponseWrapper
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityRequest
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService
import ru.gosuslugi.pgu.pgu_common.payment.service.PaymentService
import spock.lang.Specification

class PaymentTypeSelectorBillContainerSpec extends Specification {
    String json = "{\"amountCodes\":[\"sudimost_apostil\"],\"organizationId\":\"12\",\"orderId\":1,\"serviceId\":\"10000000103\",\"serviceCode\":\"10000000103\",\"paymentResponse\":{\"applicantType\":\"FL\",\"billId\":\"12\",\"billNumber\":\"18\",\"state\":\"SUCCESS\",\"organizationRequisites\":{\"name\":{\"name\":\"Name\",\"value\":\"ЗИЦ ГУ МВД России по г. Москве\"},\"payeeINN\":{\"name\":\"PayeeINN\",\"value\":\"7707089101\"},\"kpp\":{\"name\":\"KPP\",\"value\":\"770701001\"},\"bic\":{\"name\":\"BIC\",\"value\":\"004525988\"},\"bankName\":{\"name\":\"BankName\",\"value\":\"ГУ Банка России по ЦФО\"},\"personalAcc\":{\"name\":\"PersonalAcc\",\"value\":\"03100643000000017300\"},\"oktmo\":{\"name\":\"oktmo\",\"value\":\"45382000\"},\"corr\":{\"name\":\"CorrAccount\",\"value\":\"40102810545370000003\"},\"params\":[{\"name\":\"Name\",\"value\":\"ЗИЦ ГУ МВД России по г. Москве\"},{\"name\":\"PayeeINN\",\"value\":\"7707089101\"},{\"name\":\"KPP\",\"value\":\"770701001\"},{\"name\":\"BIC\",\"value\":\"004525988\"},{\"name\":\"BankName\",\"value\":\"ГУ Банка России по ЦФО\"},{\"name\":\"PersonalAcc\",\"value\":\"03100643000000017300\"},{\"name\":\"oktmo\",\"value\":\"45382000\"},{\"name\":\"CorrAccount\",\"value\":\"40102810545370000003\"}]},\"paymentRequisites\":{\"cbc\":{\"name\":\"CBC\",\"value\":\"18810807141011000110\"},\"purpose\":{\"name\":\"Purpose\",\"value\":\"Госпошлина за регистрацию автомототранспортных средств и прицепов к ним\"},\"drawerStatus\":{\"name\":\"DrawerStatus\",\"value\":\"01\"},\"paytReason\":{\"name\":\"PaytReason\",\"value\":\"0\"},\"taxPeriod\":{\"name\":\"TaxPeriod\",\"value\":\"0\"},\"docDate\":{\"name\":\"DocDate\",\"value\":\"0\"},\"taxPaytKind\":{\"name\":\"TaxPaytKind\",\"value\":\"0\"},\"docNumber\":{\"name\":\"DocNumber\",\"value\":\"0\"},\"params\":[{\"name\":\"CBC\",\"value\":\"18810807141011000110\"},{\"name\":\"Purpose\",\"value\":\"Госпошлина за регистрацию автомототранспортных средств и прицепов к ним\"},{\"name\":\"DrawerStatus\",\"value\":\"01\"},{\"name\":\"PaytReason\",\"value\":\"0\"},{\"name\":\"TaxPeriod\",\"value\":\"0\"},{\"name\":\"DocDate\",\"value\":\"0\"},{\"name\":\"TaxPaytKind\",\"value\":\"0\"},{\"name\":\"DocNumber\",\"value\":\"0\"}]},\"requestFullAmount\":\"100\",\"requestSaleAmount\":\"80\"}}"
    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
    LinkedValuesService linkedValuesService = ComponentTestUtil.getLinkedValuesService(jsonProcessingService)

    PaymentTypeSelectorComponent component
    PaymentService paymentServiceMock
    BillingService billingServiceMock
    BillContainerService billContainerServiceMock = new BillContainerService()

    def setup() {
        paymentServiceMock = Mock(PaymentService)
        billingServiceMock = Mock(BillingService) {
            getPaymentPossibleDecision(_ as PaymentPossibilityRequest) >>
                    PaymentPossibilityResponse
                            .builder()
                            .requestFullAmount('100')
                            .requestSaleAmount('80')
                            .state(PaymentPossibilityResponse.PaymentPossibilityRequestState.SUCCESS)
                            .applicantType('FL')
                            .build()
        }
        UserPersonalData userPersonalDataMock = Mock(UserPersonalData) {
            getPerson() >> new Person(citizenshipCode: 'RUS')
            getDocs() >> [new PersonDoc(type: 'RF_PASSPORT', vrfStu: 'VERIFIED', series: '1', number: '1')]
            getToken() >> 'token'
        }

        component = new PaymentTypeSelectorComponent(
                paymentServiceMock,
                billingServiceMock,
                userPersonalDataMock,
                Stub(UserOrgData),
                jsonProcessingService,
                Stub(VariableRegistry),
                billContainerServiceMock
        )
    }

    def 'данные в кэше'() {
        given:
        FieldComponent field = createFieldComponent()
        ScenarioDto scenario = new ScenarioDto(
                orderId: 1,
                display: new DisplayRequest(components: [new FieldComponent(id: "pay1", name: "Информация о платеже", type:"PaymentScr",
                        label:"Информация о платеже")]),
                cachedAnswers: [billContainer: new ApplicantAnswer(true, json)]
        )

        when:
        billingServiceMock.getBillInfo(_ as String , _ as String ) >> new BillInfoResponseWrapper(response: new BillInfoResponse(bills:[]), error: new BillInfoError(code: 0))
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> []

        component.preProcess(field, scenario)

        then:
        field.attrs['state'] == 'SUCCESS'
        Map states = field.attrs['states'] as Map
        List successActions = states['SUCCESS']['actions'] as List
        successActions.size() == 1
        successActions[0]['value'] == '{"saleAmount":"80","amount":"100","billId":"12","billNumber":"18","selected":"По квитанции","hash":"1697404277"}'
    }

    def 'данные в кэше и была оплата'() {
        given:
        FieldComponent field = createFieldComponent()
        ScenarioDto scenario = new ScenarioDto(
                orderId: 1,
                display: new DisplayRequest(components: [new FieldComponent(id: "pay1", name: "Информация о платеже", type:"PaymentScr",
                        label:"Информация о платеже")]),
                cachedAnswers: [billContainer: new ApplicantAnswer(true, json)]
        )

        when:
        billingServiceMock.getBillInfo(_ as String , _ as String ) >>
                new BillInfoResponseWrapper(response: new BillInfoResponse(bills:[new BillInfo(billId: '12', billNumber: '18', isPaid: true, amount: 80)]),
                        error: new BillInfoError(code: 0))
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> []

        component.preProcess(field, scenario)

        then:
        field.attrs['state'] == 'BILL_PAID'
        Map states = field.attrs['states'] as Map
        List valueActions = states['BILL_PAID']['actions'] as List
        valueActions.size() == 1
        valueActions[0]['value'] == '{"saleAmount":"80","amount":"100","billId":"12","billNumber":"18","selected":"Подать заявление","hash":"1697404277"}'
    }

    def 'данных в кэше нет, но есть в ответах - берем данные'() {
        given:
        String hashJson = new StringBuilder(json).insert(1, '\"hash\": \"1259539045\",\"billId\": \"12\",\"saleAmount\": \"180\",\"amount\": \"2100\",')
        FieldComponent field = createFieldComponent()
        ScenarioDto scenario = new ScenarioDto(
                orderId: 1,
                display: new DisplayRequest(components: []),
                applicantAnswers: [c1: new ApplicantAnswer(true, hashJson)]
        )

        when:
        billingServiceMock.getBillInfo(_ as String , _ as String ) >> new BillInfoResponseWrapper(response: new BillInfoResponse(bills:[new BillInfo(billId: '12', billNumber: '18', isPaid: false, amount: 80)]), error: new BillInfoError(code: 0))
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> []
        component.preProcess(field, scenario)

        then:
        field.attrs['state'] == 'SUCCESS'
        Map states = field.attrs['states'] as Map
        List successActions = states['SUCCESS']['actions'] as List
        successActions.size() == 1
        successActions[0]['value'] == '{"saleAmount":"180","amount":"2100","billId":"12","selected":"По квитанции","hash":"1259539045"}'
    }

    def 'данных в кэше нет, но есть в ответах, и была оплата'() {
        given:
        String hashJson = new StringBuilder(json).insert(1, '\"hash\": \"1259539045\",\"billId\": \"12\",\"saleAmount\": \"180\",\"amount\": \"2100\",')
        FieldComponent field = createFieldComponent()
        ScenarioDto scenario = new ScenarioDto(
                orderId: 1,
                display: new DisplayRequest(components: []),
                applicantAnswers: [c1: new ApplicantAnswer(true, hashJson)]
        )

        when:
        billingServiceMock.getBillInfo(_ as String , _ as String ) >> new BillInfoResponseWrapper(response: new BillInfoResponse(bills:[new BillInfo(billId: '12', billNumber: '18', isPaid: true, amount: 80)]), error: new BillInfoError(code: 0))
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> []
        component.preProcess(field, scenario)

        then:
        field.attrs['state'] == 'BILL_PAID'
        Map states = field.attrs['states'] as Map
        List valueActions = states['BILL_PAID']['actions'] as List
        valueActions.size() == 1
        valueActions[0]['value'] == '{"saleAmount":"180","amount":"2100","billId":"12","selected":"Подать заявление","hash":"1259539045"}'
    }

    static def createFieldComponent() {
        new FieldComponent(id: 'c1',
                arguments: [amountCode1: 'sudimost_apostil', organizationId: '12'],
                attrs: [orgCode                  : [orgCode: 'orgCode'],
                        state                    : "SUCCESS",
                        states                   : [
                                SUCCESS  : [actions: [
                                        [label: 'По квитанции', 'type': 'nextStep'],
                                        [label: 'Ранее оплаченная пошлина', 'type': 'nextStep', "showOnlyForUnusedPayments": true]]],
                                BILL_PAID: [actions: [
                                        [label: 'Подать заявление', 'type': 'nextStep']]]],
                        orgRequisitesDictionary  : '1',
                        orgRequisitesDictionaryTx: '1',
                        payRequisitesDictionaryTx: '1',
                        orgRequisitesFilters     : [],
                        payRequisitesFilters     : []])
    }
}
