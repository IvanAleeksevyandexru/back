package ru.gosuslugi.pgu.fs.component.payment

import com.fasterxml.jackson.databind.ObjectMapper
import ru.atc.carcass.security.rest.model.person.Person
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.*
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService
import spock.lang.Specification

class CreateBillComponentTest extends Specification {
    private static BillingService billingService
    private static CreateBillComponent component
    private static JsonProcessingService jsonProcessingService
    private static FieldComponent fieldComponent
    private static ScenarioDto scenarioDto
    private static UserPersonalData userPersonalData

    private static final String REQUEST_ID = "1"

    def setup() {
        fieldComponent = createFieldComponent()
        scenarioDto = new ScenarioDto()
        userPersonalData = Mock(UserPersonalData) {
            getPerson() >> new Person(citizenshipCode: 'RUS')
            getDocs() >> [new PersonDoc(type: 'RF_PASSPORT', vrfStu: 'VERIFIED', series: '1', number: '1')]
            getToken() >> 'token'
        }
        billingService = Mock(BillingService)
        jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
        component = new CreateBillComponent(new UserOrgData(), jsonProcessingService, userPersonalData, billingService)
    }

    def 'getType returns correct value'() {
        expect:
        component.getType() == ComponentType.CreateBill
    }

    def 'Test getInitialValue returns correct new bill error response'() {
        given:
        billingService.getNewBillNumber(_, _) >> createNewBillResponse(errorCode)

        when:
        def response = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        createNewBillResponse(errorCode)
        response.get() == ComponentResponse.of(jsonProcessingService.toJson(createNewBillResponse(errorCode))).get()

        where:
        errorCode << [1, 2, 3, 4, 5, 6]
    }

    def 'Test getInitialValue returns correct bill status response'() {
        given:
        billingService.getBillInfoByBillNumber(_, _) >> createBillInfoResponse()

        when:
        def response = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        response.get() == expectedResponse
        createNewBillRequestsCount * billingService.getNewBillNumber(_, _) >> createNewBillResponse(0)
        getBillStatusRequestsCount * billingService.getBillStatus(_, _) >> createBillStatusResponse(billStatusErrorCode)

        where:
        billStatusErrorCode || expectedResponse                                                                                                                                              || createNewBillRequestsCount || getBillStatusRequestsCount
        1                   || '{"errorCode":1,"billNumber":"10"}'                                                                                                                           || 2                          || 2
        2                   || '{"errorCode":2,"billNumber":"10"}'                                                                                                                           || 2                          || 2
        3                   || '{"errorCode":3,"billNumber":"10"}'                                                                                                                           || 1                          || 1
        4                   || '{"errorCode":4,"billNumber":"10"}'                                                                                                                           || 1                          || 2
        0                   || '{"response":{"bills":[{"billId":"1","billNumber":"1","billName":"1","billDate":"01.01.2020","isPaid":true,"amount":"1","addAttrs":[]}]},"error":{"code":0}}' || 1                          || 1
    }

    static ImportBillNewResponse createNewBillResponse(Integer errorCode) {
        return new ImportBillNewResponse(errorCode, REQUEST_ID, "")
    }

    static ImportBillStatusResponse createBillStatusResponse(Integer errorCode) {
        return new ImportBillStatusResponse(errorCode, "10")
    }

    static BillInfoResponseWrapper createBillInfoResponse() {
        return new BillInfoResponseWrapper(
                response: new BillInfoResponse(bills: [
                        new BillInfo(
                                billId: '1',
                                billNumber: '1',
                                billName: '1',
                                billDate: '01.01.2020',
                                isPaid: true,
                                amount: '1',
                                addAttrs: []
                        )
                ]),
                error: new BillInfoError(0))
    }

    static FieldComponent createFieldComponent() {
        new FieldComponent(type: ComponentType.CreateBill)
    }
}
