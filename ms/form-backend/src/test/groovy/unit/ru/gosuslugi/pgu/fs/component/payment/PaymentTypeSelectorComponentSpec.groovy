package unit.ru.gosuslugi.pgu.fs.component.payment

import com.fasterxml.jackson.databind.ObjectMapper
import ru.atc.carcass.security.rest.model.person.Person
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.Expression
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.payment.PaymentTypeSelectorComponent
import ru.gosuslugi.pgu.fs.component.payment.model.PayerInfo
import ru.gosuslugi.pgu.fs.component.payment.strategy.BillContainerService
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentInfo
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityRequest
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService
import ru.gosuslugi.pgu.pgu_common.payment.service.PaymentService
import spock.lang.Specification

class PaymentTypeSelectorComponentSpec extends Specification {

    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
    LinkedValuesService linkedValuesService = ComponentTestUtil.getLinkedValuesService(jsonProcessingService)

    PaymentTypeSelectorComponent component
    PaymentService paymentServiceMock
    BillingService billingServiceMock
    BillContainerService billContainerServiceMock = Mock(BillContainerService) {
        applyBillContainer(_ as FieldComponent, _ as ScenarioDto, _ as PaymentTypeSelectorComponent) >> false
    }

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

    def 'Can filter actions by option showOnlyForUnusedPayments - unused payments exists'() {
        given:
        FieldComponent field = createFieldComponent()
        ScenarioDto scenario = new ScenarioDto(orderId: 1, display: new DisplayRequest(components: []))

        when:
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> []
        component.preProcess(field, scenario)

        then:
        Map states = field.attrs['states'] as Map
        List successActions = states['SUCCESS']['actions'] as List
        successActions.size() == 1
        successActions[0]['label'] == 'По квитанции'

        List billPaidActions = states['BILL_PAID']['actions'] as List
        billPaidActions.size() == 1
        billPaidActions[0]['label'] == 'Подать заявление'
    }

    def 'Can filter actions by option showOnlyForUnusedPayments - no unused payments'() {
        given:
        FieldComponent field = createFieldComponent()
        ScenarioDto scenario = new ScenarioDto(orderId: 1, display: new DisplayRequest(components: []))

        when:
        paymentServiceMock.getUnusedPaymentsV3(_ as Long, _ as String, _ as String, _ as String, _ as String, _ as Long) >> [new PaymentInfo(amount: 80)]
        component.preProcess(field, scenario)

        then:
        Map states = field.attrs['states'] as Map
        List successActions = states['SUCCESS']['actions'] as List
        successActions.size() == 2
        successActions[0]['label'] == 'По квитанции'
        successActions[1]['label'] == 'Ранее оплаченная пошлина'
    }

    def 'When page is refreshed - we send the same data'() {
        given:
        FieldComponent field = createFieldComponent()
        ScenarioDto scenario = new ScenarioDto(orderId: 1, display: new DisplayRequest(components: [new FieldComponent(id: field.id, attrs: [a1: 'old value'])]))

        when:
        component.preProcess(field, scenario)

        then:
        field.attrs['a1'] == 'old value'
    }

    static def createFieldComponent() {
        new FieldComponent(id: 'c1',
                arguments: [acountCode: 'acountCode', passportTS: 'passportTS'],
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

    static def getFieldComponentForPayerInfoTest() {
        new FieldComponent(
                id: 'd15',
                type: ComponentType.PaymentTypeSelector,
                label: '',
                linkedValues: [
                        new LinkedValue(
                                argument: 'applicantTypeStringValue',
                                source: 'q2',
                                expressions: [
                                        new Expression(when: 'Представитель физического лица', then: '01,pd9a+pd9b'),
                                        new Expression(when: 'Представитель индивидуального предпринимателя', then: '98,pd11g'),
                                        new Expression(when: 'Представитель юридического лица', then: '99,pd5d+pd5f'),
                                ]),
                        new LinkedValue(
                                argument: 'applicantTypeStringValue',
                                source: 'q9',
                                expressions: [
                                        new Expression(when: 'Представитель физического лица', then: '01,pd9a+pd9b'),
                                        new Expression(when: 'Представитель индивидуального предпринимателя', then: '98,pd11g'),
                                        new Expression(when: 'Представитель юридического лица', then: '99,pd5d+pd5f'),
                                ]),
                        new LinkedValue(
                                argument: 'documentSeries',
                                source: 'pd9a'),
                        new LinkedValue(
                                argument: 'documentNumber',
                                source: 'pd9b'),
                        new LinkedValue(
                                argument: 'ownerDocumentTypeCode',
                                source: 'fai4.originalItem.value'),
                        new LinkedValue(
                                argument: 'specificPayerIdTypeByOwnerDocumentTypeCode',
                                source: 'fai4.originalItem.value',
                                expressions: [
                                        new Expression(when: '8', then: '08')
                                ])
                ],
                value: ''
        )
    }

    def 'test payer id type and num for foreign citizen'() {
        given:
        String docSeries = '123'
        String docNumber = '456'
        PaymentTypeSelectorComponent paymentTypeSelectorComponent = paymentTypeSelector('ENG', 'FID_DOC', docSeries, docNumber)
        FieldComponent fieldComponent = getFieldComponentForPayerInfoTest()
        String applicantType = 'FL'
        Map<String, ApplicantAnswer> currentAnswer
        Map<String, ApplicantAnswer> previousAnswers = new HashMap<>()
        ApplicantAnswer applicantAnswer

        when:
        applicantAnswer = new ApplicantAnswer(true, 'Представитель физического лица')
        currentAnswer = Map.of('q2', applicantAnswer)
        PayerInfo payerInfo = paymentTypeSelectorComponent.getPayerIdTypeAndPayerIdNum(fieldComponent, applicantType, currentAnswer, previousAnswers)

        then:
        payerInfo.getIdNum() == docSeries + docNumber
        payerInfo.getIdType() == PaymentTypeSelectorComponent.DEFAULT_PAYER_ID_TYPE_FOREIGN_CITIZEN;
    }

    def 'test payer id type and num for FL without specific payer id'() {
        given:
        String docSeries = '1234'
        String docNumber = '567890'
        PaymentTypeSelectorComponent paymentTypeSelectorComponent = paymentTypeSelector('RUS', 'RF_PASSPORT', docSeries, docNumber)
        FieldComponent fieldComponent = getFieldComponentForPayerInfoTest()
        String applicantType = 'FL'
        Map<String, ApplicantAnswer> currentAnswer
        Map<String, ApplicantAnswer> previousAnswers
        ApplicantAnswer applicantAnswer
        ScenarioDto scenarioDto

        when: 'pd9a and pd9b are exist'
        applicantAnswer = new ApplicantAnswer(true, 'Представитель физического лица')
        currentAnswer = Map.of('q2', applicantAnswer)
        previousAnswers = Map.of('pd9a', new ApplicantAnswer(visited: true, value: docSeries),
                'pd9b', new ApplicantAnswer(visited: true, value: docNumber))
        scenarioDto = new ScenarioDto(currentValue: currentAnswer)
        linkedValuesService.fillLinkedValues(fieldComponent, scenarioDto)
        PayerInfo payerInfo = paymentTypeSelectorComponent.getPayerIdTypeAndPayerIdNum(fieldComponent, applicantType, currentAnswer, previousAnswers)

        then:
        payerInfo.getIdNum() == docSeries + docNumber
        payerInfo.getIdType() == PaymentTypeSelectorComponent.DEFAULT_PAYER_ID_TYPE_FL;

        when: 'APPLICANT_STRING_VALUE is empty'
        fieldComponent = getFieldComponentForPayerInfoTest()
        applicantAnswer = new ApplicantAnswer(true, 'Представитель физического лица')
        currentAnswer = Map.of('q2', applicantAnswer)
        previousAnswers = Map.of('pd9a', new ApplicantAnswer(visited: true, value: docSeries),
                'pd9b', new ApplicantAnswer(visited: true, value: docNumber))
        payerInfo = paymentTypeSelectorComponent.getPayerIdTypeAndPayerIdNum(fieldComponent, applicantType, currentAnswer, previousAnswers)

        then:
        payerInfo.getIdNum() == docSeries + docNumber
        payerInfo.getIdType() == PaymentTypeSelectorComponent.DEFAULT_PAYER_ID_TYPE_FL;
    }
    def 'test payer id type and num for FL with specific payer id'() {
        given:
        String docSeries = '1234'
        String docNumber = '567890'
        PaymentTypeSelectorComponent paymentTypeSelectorComponent = paymentTypeSelector('RUS', 'RF_PASSPORT', docSeries, docNumber)
        FieldComponent fieldComponent = getFieldComponentForPayerInfoTest()
        String applicantType = 'FL'
        Map<String, ApplicantAnswer> currentAnswer
        Map<String, ApplicantAnswer> previousAnswers
        ApplicantAnswer applicantAnswer
        ScenarioDto scenarioDto

        when: 'pd9a and pd9b are exist'
        applicantAnswer = new ApplicantAnswer(true, 'Представитель физического лица')
        currentAnswer = Map.of('q2', applicantAnswer)
        previousAnswers = Map.of('pd9a', new ApplicantAnswer(visited: true, value: docSeries),
                'pd9b', new ApplicantAnswer(visited: true, value: docNumber))
        scenarioDto = new ScenarioDto(currentValue: currentAnswer)
        linkedValuesService.fillLinkedValues(fieldComponent, scenarioDto)
        PayerInfo payerInfo = paymentTypeSelectorComponent.getPayerIdTypeAndPayerIdNum(fieldComponent, applicantType, currentAnswer, previousAnswers)

        then:
        payerInfo.getIdNum() == docSeries + docNumber
        payerInfo.getIdType() == PaymentTypeSelectorComponent.DEFAULT_PAYER_ID_TYPE_FL;

        when: 'APPLICANT_STRING_VALUE is empty'
        fieldComponent = getFieldComponentForPayerInfoTest()
        applicantAnswer = new ApplicantAnswer(true, 'Представитель физического лица')
        currentAnswer = Map.of('q2', applicantAnswer)
        previousAnswers = Map.of('pd9a', new ApplicantAnswer(visited: true, value: docSeries),
                'pd9b', new ApplicantAnswer(visited: true, value: docNumber))
        payerInfo = paymentTypeSelectorComponent.getPayerIdTypeAndPayerIdNum(fieldComponent, applicantType, currentAnswer, previousAnswers)

        then:
        payerInfo.getIdNum() == docSeries + docNumber
        payerInfo.getIdType() == PaymentTypeSelectorComponent.DEFAULT_PAYER_ID_TYPE_FL;

        when: 'FL_SPECIFIC_PAYERID is exists'
        fieldComponent = getFieldComponentForPayerInfoTest()
        applicantAnswer = new ApplicantAnswer(true, 'Представитель физического лица')
        currentAnswer = Map.of('q2', applicantAnswer)
        previousAnswers = Map.of('pd9a', new ApplicantAnswer(visited: true, value: docSeries),
                'pd9b', new ApplicantAnswer(visited: true, value: docNumber))
        linkedValuesService.fillLinkedValues(fieldComponent, scenarioDto)
        fieldComponent.addArgument('specificPayerIdTypeByOwnerDocumentTypeCode', '08')
        payerInfo = paymentTypeSelectorComponent.getPayerIdTypeAndPayerIdNum(fieldComponent, applicantType, currentAnswer, previousAnswers)

        then:
        payerInfo.getIdNum() == docSeries + docNumber
        payerInfo.getIdType() == PaymentTypeSelectorComponent.FL_SPECIFIC_PAYERID_VALUE;
    }

    def 'test payer id type and num for UL'() {
        given:
        String docSeries = '1234'
        String docNumber = '567890'
        PaymentTypeSelectorComponent paymentTypeSelectorComponent = paymentTypeSelector('RUS', 'RF_PASSPORT', docSeries, docNumber)
        FieldComponent fieldComponent = getFieldComponentForPayerInfoTest()
        String applicantType = 'UL'
        Map<String, ApplicantAnswer> currentAnswer
        Map<String, ApplicantAnswer> previousAnswers
        ApplicantAnswer applicantAnswer
        ScenarioDto scenarioDto

        when:
        applicantAnswer = new ApplicantAnswer(true, 'Представитель юридического лица')
        currentAnswer = Map.of('q2', applicantAnswer)
        previousAnswers = Map.of('pd5d', new ApplicantAnswer(visited: true, value: docSeries),
                'pd5f', new ApplicantAnswer(visited: true, value: docNumber))
        scenarioDto = new ScenarioDto(currentValue: currentAnswer)
        linkedValuesService.fillLinkedValues(fieldComponent, scenarioDto)
        PayerInfo payerInfo = paymentTypeSelectorComponent.getPayerIdTypeAndPayerIdNum(fieldComponent, applicantType, currentAnswer, previousAnswers)

        then:
        payerInfo.getIdType() == PaymentTypeSelectorComponent.DEFAULT_PAYER_ID_TYPE_UL;
    }

    def 'test payer id type and num for IP'() {
        given:
        String docSeries = '1234'
        String docNumber = '567890'
        PaymentTypeSelectorComponent paymentTypeSelectorComponent = paymentTypeSelector('RUS', 'RF_PASSPORT', docSeries, docNumber)
        FieldComponent fieldComponent = getFieldComponentForPayerInfoTest()
        String applicantType = 'IP'
        Map<String, ApplicantAnswer> currentAnswer
        Map<String, ApplicantAnswer> previousAnswers
        ApplicantAnswer applicantAnswer
        ScenarioDto scenarioDto

        when:
        applicantAnswer = new ApplicantAnswer(true, 'Представитель индивидуального предпринимателя')
        currentAnswer = Map.of('q2', applicantAnswer)
        previousAnswers = Map.of('pd11g', new ApplicantAnswer(visited: true, value: docSeries))
        scenarioDto = new ScenarioDto(currentValue: currentAnswer)
        linkedValuesService.fillLinkedValues(fieldComponent, scenarioDto)
        PayerInfo payerInfo = paymentTypeSelectorComponent.getPayerIdTypeAndPayerIdNum(fieldComponent, applicantType, currentAnswer, previousAnswers)

        then:
        payerInfo.getIdType() == PaymentTypeSelectorComponent.DEFAULT_PAYER_ID_TYPE_IP;
    }

    def paymentTypeSelector(citizenshipCode, docType, series, number) {
        UserPersonalData userPersonalDataMock = Mock(UserPersonalData) {
            getPerson() >> new Person(citizenshipCode: citizenshipCode)
            getDocs() >> [new PersonDoc(type: docType, vrfStu: 'VERIFIED', series: series, number: number)]
            getToken() >> 'token'

        }

        new PaymentTypeSelectorComponent(
                Mock(PaymentService),
                Mock(BillingService),
                userPersonalDataMock,
                Stub(UserOrgData),
                jsonProcessingService,
                Stub(VariableRegistry),
                Stub(BillContainerService)
        )
    }
}
