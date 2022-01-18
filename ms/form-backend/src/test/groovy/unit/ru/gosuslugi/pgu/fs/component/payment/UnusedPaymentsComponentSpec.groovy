package unit.ru.gosuslugi.pgu.fs.component.payment

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.component.payment.UnusedPaymentsComponent
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentInfo
import ru.gosuslugi.pgu.pgu_common.payment.service.PaymentService
import spock.lang.Specification

class UnusedPaymentsComponentSpec extends Specification{

    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
    UnusedPaymentsComponent component
    PaymentService paymentServiceMock
    ScenarioDto scenario = new ScenarioDto(orderId: 1L, cachedAnswers: [up1: new ApplicantAnswer(true, '{"amount":"100", "saleAmount":"80"}')])
    FieldComponent fieldComponent = new FieldComponent(id: 'up1', arguments: [amount: '100', saleAmount: '80'], attrs: [orgCode: [orgCode: 'orgCode']])

    def setup() {
        paymentServiceMock = Mock(PaymentService)
        UserPersonalData userPersonalDataMock = Mock(UserPersonalData) {
            getToken() >> 'token'
        }
        component = new UnusedPaymentsComponent(
                paymentServiceMock,
                jsonProcessingService,
                userPersonalDataMock,
                Stub(VariableRegistry)
        )
    }

    def 'Can get no payment'() {
        when:
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> []
        ComponentResponse result = component.getInitialValue(fieldComponent, scenario)

        then:
        result.get().size() == 0
    }

    def 'Can get one payment'() {
        when:
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> [new PaymentInfo(amount: 80)]
        ComponentResponse result = component.getInitialValue(fieldComponent, scenario)

        then:
        result.get().size() == 1
        result.get().get(0).amount == 80
    }

    def 'данных в кэше нет'() {
        when:
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, 80 as Long) >> [new PaymentInfo(amount: 80)]
        ComponentResponse result = component.getInitialValue(fieldComponent, new ScenarioDto(orderId: 1L))

        then:
        thrown NullPointerException
    }

    def 'данных в кэше нет, но есть в ответах - берем данные'() {
        when:
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, 100 as Long) >> [new PaymentInfo(amount: 80)]
        ComponentResponse result = component.getInitialValue(fieldComponent, new ScenarioDto(orderId: 1L, applicantAnswers: [up1: new ApplicantAnswer(true, '{"amount":"100", "saleAmount":"80"}')]))

        then:
        result.get().size() == 1
        result.get().get(0).amount == 80
    }

    def 'Can get same payment'() {
        when:
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> [new PaymentInfo(amount: 100)]
        Map.Entry<String, ApplicantAnswer> answer = AnswerUtil.createAnswerEntry('key', '{"amount": 100, "saleAmount": 80}')
        Map<String, String> incorrectAnswers = [:]
        component.validateAfterSubmit(incorrectAnswers, answer, scenario, fieldComponent)

        then:
        incorrectAnswers.size() == 0
    }

    def 'Can get error with another payment'() {
        when:
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> [new PaymentInfo(amount: 100)]
        Map.Entry<String, ApplicantAnswer> answer = AnswerUtil.createAnswerEntry('key', '{"amount": 200, "saleAmount": 130}')
        Map<String, String> incorrectAnswers = [:]
        component.validateAfterSubmit(incorrectAnswers, answer, scenario, fieldComponent)

        then:
        incorrectAnswers.size() == 1
        incorrectAnswers.get('up1') == 'Выбранный платёж недоступен'
    }
}
