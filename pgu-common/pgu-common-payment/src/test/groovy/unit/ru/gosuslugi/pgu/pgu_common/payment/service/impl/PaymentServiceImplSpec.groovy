package unit.ru.gosuslugi.pgu.pgu_common.payment.service.impl

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentInfo
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentStatusInfo
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentStatusInfoWrapper
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentsInfo
import ru.gosuslugi.pgu.pgu_common.payment.service.impl.PaymentServiceImpl
import spock.lang.Specification

import java.lang.reflect.ParameterizedType

class PaymentServiceImplSpec extends Specification {

    PaymentServiceImpl service
    def restTemplateMock = Mock(RestTemplate)

    String mockUrl = 'mockUrl'
    String pguUrl = 'pguUrl'
    String pguIpshUrl = 'pguIpshUrl'

    Long orderId = 1L
    Long amount = 100

    def setup() {
        service = new PaymentServiceImpl(restTemplateMock)
        service.mockEnabled = true
        service.mockUrl = mockUrl
        service.pguUrl = pguUrl
        service.pguIpshUrl = pguIpshUrl
    }

    def "Can get payment with null orgCode"() {
        given:
        List<PaymentInfo> result

        when:
        restTemplateMock.exchange("${mockUrl}api/lk/v3/orders/listpaymentsinfo", HttpMethod.POST, _ as HttpEntity, _ as ParameterizedTypeReference) >>
                new ResponseEntity(new PaymentsInfo(payment: [new PaymentInfo(amount: 300.0, invitationAddress: "Москва г, Хорошевское ш, д 40А",
                        uin: "1881100000000000020001818", link: "https://lk.gosuslugi.ru/notifications/details/PAYMENT/2406140", title: "Госпошлина за регистрацию автомототранспортных средств и прицепов к ним")]), HttpStatus.OK)
        result = service.getUnusedPaymentsV3(orderId, null, 'token', 'serviceId', 'applicantType', amount)

        then:
        result.size() == 1
        result.get(0).amount == 300.0
    }

        def "Can get payment"() {
        given:
        List<PaymentInfo> result

        when:
        restTemplateMock.exchange("${mockUrl}api/lk/v3/orders/listpaymentsinfo", HttpMethod.POST, _ as HttpEntity, _ as ParameterizedTypeReference) >>
                new ResponseEntity(new PaymentsInfo(payment: [new PaymentInfo(amount: 300.0, invitationAddress: "Москва г, Хорошевское ш, д 40А",
                uin: "1881100000000000020001818", link: "https://lk.gosuslugi.ru/notifications/details/PAYMENT/2406140", title: "Госпошлина за регистрацию автомототранспортных средств и прицепов к ним")]), HttpStatus.OK)
        result = service.getUnusedPaymentsV3(orderId, 'orgCode', 'token', 'serviceId', 'applicantType', amount)

        then:
        result.size() == 1
        result.get(0).amount == 300.0
    }

    def "Can get payment status paid is true"() {
        given:
        PaymentStatusInfo result
        Boolean paid = true

        when:
        restTemplateMock.exchange("${pguUrl}api/lk/v1/paygate/uin/status/{paycode}?orderId={orderId}", HttpMethod.GET, _ as HttpEntity, _ as ParameterizedTypeReference, _ as Map<String, Object>) >>
                new ResponseEntity(new PaymentStatusInfoWrapper(paymentStatus: [new PaymentStatusInfo(paid: paid)]), HttpStatus.OK)
        result = service.getPaymentStatus(orderId, 'token', 'payCode', 'billId')

        then:
        result.paid
    }

    def "Can get payment status paid is false"() {
        given:
        PaymentStatusInfo result
        Boolean paid = false

        when:
        restTemplateMock.exchange("${pguUrl}api/lk/v1/paygate/uin/status/{paycode}?orderId={orderId}", HttpMethod.GET, _ as HttpEntity, _ as ParameterizedTypeReference, _ as Map<String, Object>) >>
                new ResponseEntity(new PaymentStatusInfoWrapper(paymentStatus: [new PaymentStatusInfo(paid: paid)]), HttpStatus.OK)
        result = service.getPaymentStatus(orderId, 'token', 'payCode', 'billId')

        then:
        !result.paid
    }

    def "Can get empty billId"() {
        given:
        PaymentStatusInfo result
        String billId = ''

        when:
        restTemplateMock.exchange("${pguUrl}api/lk/v1/paygate/uin/status/{paycode}?orderId={orderId}", HttpMethod.GET, _ as HttpEntity, _ as ParameterizedTypeReference, _ as Map<String, Object>) >>
                new ResponseEntity(new PaymentStatusInfoWrapper(), HttpStatus.OK)
        result = service.getPaymentStatus(orderId, 'token', 'payCode', billId)

        then:
        !result.paid
    }

    def "Can get reChecked payment status - paiedBillIds"() {
        given:
        PaymentStatusInfo result
        String billId = 'billId'

        when:
        restTemplateMock.exchange("${pguUrl}api/lk/v1/paygate/uin/status/{paycode}?orderId={orderId}", HttpMethod.GET, _ as HttpEntity, _ as ParameterizedTypeReference, _ as Map<String, Object>) >>
                new ResponseEntity(new PaymentStatusInfoWrapper(), HttpStatus.OK)
        restTemplateMock.exchange("${pguIpshUrl}api/pay/v1/bills?billIds=billId&ci=false", HttpMethod.POST, _ as HttpEntity, _ as ParameterizedTypeReference) >>
                new ResponseEntity(response: ["paiedBillIds": ["paiedBillId"]], HttpStatus.OK)
        result = service.getPaymentStatus(orderId, 'token', 'payCode', billId)

        then:
        result.paid
    }

    def "Can get reChecked payment status - empty paiedBillIds"() {
        given:
        PaymentStatusInfo result
        String billId = 'billId'

        when:
        restTemplateMock.exchange("${pguUrl}api/lk/v1/paygate/uin/status/{paycode}?orderId={orderId}", HttpMethod.GET, _ as HttpEntity, _ as ParameterizedTypeReference, _ as Map<String, Object>) >>
                new ResponseEntity(new PaymentStatusInfoWrapper(), HttpStatus.OK)
        restTemplateMock.exchange("${pguIpshUrl}api/pay/v1/bills?billIds=billId&ci=false", HttpMethod.POST, _ as HttpEntity, _ as ParameterizedTypeReference) >>
                new ResponseEntity(response: ["paiedBillIds": []], HttpStatus.OK)
        result = service.getPaymentStatus(orderId, 'token', 'payCode', billId)

        then:
        !result.paid
    }

    def "Can get reChecked payment status - is null paiedBillIds"() {
        given:
        PaymentStatusInfo result
        String billId = 'billId'

        when:
        restTemplateMock.exchange("${pguUrl}api/lk/v1/paygate/uin/status/{paycode}?orderId={orderId}", HttpMethod.GET, _ as HttpEntity, _ as ParameterizedTypeReference, _ as Map<String, Object>) >>
                new ResponseEntity(new PaymentStatusInfoWrapper(), HttpStatus.OK)
        restTemplateMock.exchange("${pguIpshUrl}api/pay/v1/bills?billIds=billId&ci=false", HttpMethod.POST, _ as HttpEntity, _ as ParameterizedTypeReference) >>
                new ResponseEntity(response: ["paiedBillIds": null], HttpStatus.OK)
        result = service.getPaymentStatus(orderId, 'token', 'payCode', billId)

        then:
        !result.paid
    }

    def "Can get reChecked payment status - empty response"() {
        given:
        PaymentStatusInfo result
        String billId = 'billId'

        when:
        restTemplateMock.exchange("${pguUrl}api/lk/v1/paygate/uin/status/{paycode}?orderId={orderId}", HttpMethod.GET, _ as HttpEntity, _ as ParameterizedTypeReference, _ as Map<String, Object>) >>
                new ResponseEntity(new PaymentStatusInfoWrapper(), HttpStatus.OK)
        restTemplateMock.exchange("${pguIpshUrl}api/pay/v1/bills?billIds=billId&ci=false", HttpMethod.POST, _ as HttpEntity, _ as ParameterizedTypeReference) >>
                new ResponseEntity(response: [], HttpStatus.OK)
        result = service.getPaymentStatus(orderId, 'token', 'payCode', billId)

        then:
        !result.paid
    }

    def "Can get reChecked payment status - response is null"() {
        given:
        PaymentStatusInfo result
        String billId = 'billId'

        when:
        restTemplateMock.exchange("${pguUrl}api/lk/v1/paygate/uin/status/{paycode}?orderId={orderId}", HttpMethod.GET, _ as HttpEntity, _ as ParameterizedTypeReference, _ as Map<String, Object>) >>
                new ResponseEntity(new PaymentStatusInfoWrapper(), HttpStatus.OK)
        restTemplateMock.exchange("${pguIpshUrl}api/pay/v1/bills?billIds=billId&ci=false", HttpMethod.POST, _ as HttpEntity, _ as ParameterizedTypeReference) >>
                new ResponseEntity(response: null, HttpStatus.OK)
        result = service.getPaymentStatus(orderId, 'token', 'payCode', billId)

        then:
        !result.paid
    }
}
