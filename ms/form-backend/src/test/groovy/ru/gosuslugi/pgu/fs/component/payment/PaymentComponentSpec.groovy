package ru.gosuslugi.pgu.fs.component.payment

import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentDetailsDto
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentStatusInfo
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService
import ru.gosuslugi.pgu.pgu_common.payment.service.PaymentService
import spock.lang.Specification

class PaymentComponentSpec extends Specification {

    String PAYMENT_SCR_ID = "pay1"
    String UIN = "0316373317011700000057047"
    String UIN_WITH_PRIOR = "PRIOR0316373317011700000057047"
    String CURRENT_ANSWER_WITH_UIN = "{\"uin\":\"%s\",\"amount\":\"359\",\"amountWithoutDiscount\":\"512\",\"paymentPurpose\":\"Госпошлина ЗАГС\",\"receiver\":\"CНИЛС 00069600068\",\"billId\":15464543}"
    String CURRENT_ANSWER = "{\"amount\":\"359\",\"amountWithoutDiscount\":\"512\",\"paymentPurpose\":\"Госпошлина ЗАГС\",\"receiver\":\"CНИЛС 00069600068\",\"billId\":15464543}"

    PaymentComponent paymentComponent
    PaymentService paymentServiceStub = Stub(PaymentService)

    def setup() {
        paymentComponent = new PaymentComponent(
                paymentServiceStub,
                Stub(UserPersonalData),
                Stub(BillingService)
        )
    }

    def 'Check validation if goNextAfterUIN is true and uin exists' () {
        given:
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry = getCurrentAnswerWithUIN(UIN)
        ScenarioDto scenarioDto = new ScenarioDto()
        FieldComponent fieldComponent = getPaymentScr(true)

        when:
        paymentComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)
        PaymentDetailsDto paymentDetailsDto = JsonProcessingUtil.fromJson(fieldComponent.getValue(), PaymentDetailsDto.class)

        then:
        CollectionUtils.isEmpty(incorrectAnswers)
        StringUtils.hasText(paymentDetailsDto.getAmount())
        StringUtils.hasText(paymentDetailsDto.getAmountWithoutDiscount())
        StringUtils.hasText(paymentDetailsDto.getPaymentPurpose())
        StringUtils.hasText(paymentDetailsDto.getReceiver())
        paymentDetailsDto.getBillId() != null
    }

    def 'Check validation if goNextAfterUIN is true and uin exists with PRIOR' () {
        given:
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry = getCurrentAnswerWithUIN(UIN_WITH_PRIOR)
        ScenarioDto scenarioDto = new ScenarioDto()
        FieldComponent fieldComponent = getPaymentScr(true)

        when:
        paymentComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        CollectionUtils.isEmpty(incorrectAnswers)
    }

    def 'Check validation if goNextAfterUIN is true and uin does not exist' () {
        given:
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry = getCurrentAnswer()
        ScenarioDto scenarioDto = new ScenarioDto()
        FieldComponent fieldComponent = getPaymentScr(true)

        when:
        paymentComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.get(PAYMENT_SCR_ID).equalsIgnoreCase("Отсутствует UIN")
    }

    def 'Check validation if goNextAfterUIN is false and uin does not exist' () {
        given:
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry = getCurrentAnswer()
        ScenarioDto scenarioDto = new ScenarioDto()
        FieldComponent fieldComponent = getPaymentScrWithArguments(false)

        when:
        paymentComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.get(PAYMENT_SCR_ID).equalsIgnoreCase("Оплата должна быть произведена")
    }

    def 'Check validation if no attr goNextAfterUIN and uin does not exist' () {
        given:
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry = getCurrentAnswer()
        ScenarioDto scenarioDto = new ScenarioDto()
        FieldComponent fieldComponent = getPaymentScrWithArguments(null)

        when:
        paymentComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.get(PAYMENT_SCR_ID).equalsIgnoreCase("Оплата должна быть произведена")
    }

    def getPaymentStatusPaied() {
        PaymentStatusInfo paymentStatusInfo  = new PaymentStatusInfo()
        paymentStatusInfo.setPaid(true)
        paymentStatusInfo.setUin(UIN)
        return paymentStatusInfo
    }

    def getCurrentAnswer() {
        ApplicantAnswer applicantAnswer = new ApplicantAnswer()
        applicantAnswer.setVisited(true)
        applicantAnswer.setValue(CURRENT_ANSWER)
        HashMap<String, ApplicantAnswer> map = new HashMap()
        map.put(PAYMENT_SCR_ID, applicantAnswer)
        return map.entrySet()[0]
    }

    def getCurrentAnswerWithUIN(String uin) {
        ApplicantAnswer applicantAnswer = new ApplicantAnswer()
        applicantAnswer.setVisited(true)
        applicantAnswer.setValue(String.format(CURRENT_ANSWER_WITH_UIN, uin))
        HashMap<String, ApplicantAnswer> map = new HashMap()
        map.put(PAYMENT_SCR_ID, applicantAnswer)
        return map.entrySet()[0]
    }

    def getPaymentScr(Boolean goNextAfterUIN) {
        FieldComponent fieldComponent = new FieldComponent();
        fieldComponent.setId(PAYMENT_SCR_ID)
        fieldComponent.setAttrs(new HashMap<String, Object>())
        if (goNextAfterUIN != null) {
            fieldComponent.getAttrs().put("goNextAfterUIN", goNextAfterUIN)
        }
        return fieldComponent
    }

    def getPaymentScrWithArguments(Boolean goNextAfterUIN) {
        FieldComponent fieldComponent = new FieldComponent();
        fieldComponent.setId(PAYMENT_SCR_ID)
        fieldComponent.setAttrs(new HashMap<String, Object>())
        if (goNextAfterUIN != null) {
            fieldComponent.getAttrs().put("goNextAfterUIN", goNextAfterUIN)
        }
        fieldComponent.addArgument("1", "test1")
        return fieldComponent
    }
}
