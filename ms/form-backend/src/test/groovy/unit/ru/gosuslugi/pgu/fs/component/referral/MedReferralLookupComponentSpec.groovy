package unit.ru.gosuslugi.pgu.fs.component.referral

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.component.medicine.MedReferralLookupComponent
import ru.gosuslugi.pgu.fs.component.medicine.mapper.ReferralMapperImpl
import ru.gosuslugi.pgu.fs.component.medicine.model.*
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MedReferralLookupComponentSpec extends Specification {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    MedReferralLookupComponent component
    RestTemplate restTemplateMock
    JsonProcessingService jsonProcessingService
    LinkedValuesService linkedValuesServiceMock
    @Shared
    ScenarioDto scenarioDto
    @Shared
    FieldComponent fieldComponent

    def setup() {
        jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
        restTemplateMock = Mock(RestTemplate)
        linkedValuesServiceMock = Mock(LinkedValuesService)
        component = new MedReferralLookupComponent(restTemplateMock, Stub(UserPersonalData), new ReferralMapperImpl(), Stub(ErrorModalDescriptorService))
        component.linkedValuesService = linkedValuesServiceMock
        component.jsonProcessingService = jsonProcessingService
    }

    def setupSpec() {
        scenarioDto = new ScenarioDto(currentValue: [:] as Map)
        fieldComponent = new FieldComponent(id: 'c1', arguments: [eserviceId: '0000', sessionId:'1c924ed0-62cb-4877-b4d9-a2ea8d1dc7e6'])
    }

    def 'Can set referral info - E-Queue service return valid referrals'() {
        when:
        restTemplateMock.exchange(_ as String, HttpMethod.POST, _ as HttpEntity, MedDictionaryResponse.class) >>
                new ResponseEntity(eQueueServiceValidResponse(), HttpStatus.OK)
        component.postProcess(fieldComponent, scenarioDto, "value")

        then:
        def storedValue = jsonProcessingService.fromJson(scenarioDto.currentValue[fieldComponent.id].value, ReferralNumberComponentDto.class)
        storedValue.statusCode == MedDictionaryResponseCode.SUCCESS
        storedValue.errorCode == 0
        storedValue.errorMessage == 'Operation competed'
        storedValue.referral.referralId == '205206'
        storedValue.referral.referralNumber == '445'
        storedValue.referral.referralTypeId == '6'
        storedValue.referral.referralStartDate == '2020-01-10'
        storedValue.referral.referralEndDate == DATE_TIME_FORMATTER.format(LocalDate.now().plusDays(5))
        storedValue.referral.paymentSourceId == '1'
        storedValue.referral.toMoOid == '1.2.643.5.1.13.13.12.2.16.1179.0.221506'
        storedValue.referral.toMoName == 'Женская консультация, ГАУЗ "Городская поликлиника № 21"'
        storedValue.referral.toServiceId == 'A04.10.002.010'
        storedValue.referral.toServiceName == 'Эхокардиография с допплерографией'
        storedValue.referral.toSpecsId == '34'
        storedValue.referral.toSpecsName == 'врач-кардиолог'
        storedValue.referral.toResourceName == 'Пушкина Анна Ивановна'
        storedValue.referral.fromMoOid == '1.2.643.5.1.13.13.12.2.16.1080.0.368844'
        storedValue.referral.fromMoName == 'Отделение узких специалистов, ГАУЗ "Городская поликлиника №18"'
        storedValue.referral.fromSpecsId == '109'
        storedValue.referral.fromSpecsName == 'врач-терапевт'
        storedValue.referral.fromResourceName == 'Николаева Яна Семеновна'
    }

    def 'Can set referral info - E-Queue service return expired referrals'() {
        when:
        restTemplateMock.exchange(_ as String, HttpMethod.POST, _ as HttpEntity, MedDictionaryResponse.class) >>
                new ResponseEntity(eQueueServiceExpiredResponse(), HttpStatus.OK)
        component.postProcess(fieldComponent, scenarioDto, "value")

        then:
        def storedValue = jsonProcessingService.fromJson(scenarioDto.currentValue[fieldComponent.id].value, ReferralNumberComponentDto.class)
        storedValue.statusCode == MedDictionaryResponseCode.REFERRAL_EXPIRED
        storedValue.errorCode == 0
        storedValue.errorMessage == 'Operation competed'
        storedValue.referral.referralId == '205206'
        storedValue.referral.referralNumber == '445'
        storedValue.referral.referralTypeId == '6'
        storedValue.referral.referralStartDate == '2020-01-10'
        storedValue.referral.referralEndDate == '2021-06-01'
        storedValue.referral.paymentSourceId == '1'
        storedValue.referral.toMoOid == '1.2.643.5.1.13.13.12.2.16.1179.0.221506'
        storedValue.referral.toMoName == 'Женская консультация, ГАУЗ "Городская поликлиника № 21"'
        storedValue.referral.toServiceId == 'A04.10.002.010'
        storedValue.referral.toServiceName == 'Эхокардиография с допплерографией'
        storedValue.referral.toSpecsId == '34'
        storedValue.referral.toSpecsName == 'врач-кардиолог'
        storedValue.referral.toResourceName == 'Пушкина Анна Ивановна'
        storedValue.referral.fromMoOid == '1.2.643.5.1.13.13.12.2.16.1080.0.368844'
        storedValue.referral.fromMoName == 'Отделение узких специалистов, ГАУЗ "Городская поликлиника №18"'
        storedValue.referral.fromSpecsId == '109'
        storedValue.referral.fromSpecsName == 'врач-терапевт'
        storedValue.referral.fromResourceName == 'Николаева Яна Семеновна'
    }

    def 'Can set referral info - E-Queue service return error'() {
        when:
        restTemplateMock.exchange(_ as String, HttpMethod.POST, _ as HttpEntity, MedDictionaryResponse.class) >>
                new ResponseEntity(eQueueServiceErrorResponse(), HttpStatus.OK)
        component.postProcess(fieldComponent, scenarioDto, "value")

        then:
        def storedValue = jsonProcessingService.fromJson(scenarioDto.currentValue[fieldComponent.id].value, ReferralNumberComponentDto.class)
        storedValue.statusCode == MedDictionaryResponseCode.REFERRAL_NOT_FOUND
        storedValue.errorCode == 6
        storedValue.errorMessage == 'NO_DATA:Направление пациента с указанным номером не найдено. Пожалуйста, проверьте корректность введенных выше данных.'
        storedValue.referral == null
    }

    def 'Can set referral info - E-Queue service return empty response'() {
        when:
        restTemplateMock.exchange(_ as String, HttpMethod.POST, _ as HttpEntity, MedDictionaryResponse.class) >>
                new ResponseEntity(null, HttpStatus.OK)
        component.postProcess(fieldComponent, scenarioDto, "value")

        then:
        def e = thrown(ErrorModalException)
        e.message == 'Произошла ошибка загрузки мед. направления из ЛК'
    }

    def 'Can set referral info - catch exception'() {
        when:
        restTemplateMock.exchange(_ as String, HttpMethod.POST, _ as HttpEntity, MedDictionaryResponse.class) >>
                {throw new RestClientException('error')}
        component.postProcess(fieldComponent, scenarioDto, "value")

        then:
        def e = thrown(ErrorModalException)
        e.message == 'Произошла ошибка загрузки мед. направления из ЛК'
    }

    def static eQueueServiceValidResponse() {
        new MedDictionaryResponse(error: [errorDetail: [errorCode: 0, errorMessage: 'Operation competed']],
                items: [
                        new MedDictionaryResponseItem(attributes: [
                                new MedDictionaryResponseAttribute(name: 'referralId', value: '205206'),
                                new MedDictionaryResponseAttribute(name: 'referralNumber', value: '445'),
                                new MedDictionaryResponseAttribute(name: 'referralTypeId', value: '6'),
                                new MedDictionaryResponseAttribute(name: 'referralStartDate', value: '2020-01-10'),
                                new MedDictionaryResponseAttribute(name: 'referralEndDate', value: DATE_TIME_FORMATTER.format(LocalDate.now().plusDays(5))),
                                new MedDictionaryResponseAttribute(name: 'paymentSourceId', value: '1'),
                                new MedDictionaryResponseAttribute(name: 'toMoOid', value: '1.2.643.5.1.13.13.12.2.16.1179.0.221506'),
                                new MedDictionaryResponseAttribute(name: 'toMoName', value: 'Женская консультация, ГАУЗ "Городская поликлиника № 21"'),
                                new MedDictionaryResponseAttribute(name: 'toServiceId', value: 'A04.10.002.010'),
                                new MedDictionaryResponseAttribute(name: 'toServiceName', value: 'Эхокардиография с допплерографией'),
                                new MedDictionaryResponseAttribute(name: 'toSpecsId', value: '34'),
                                new MedDictionaryResponseAttribute(name: 'toSpecsName', value: 'врач-кардиолог'),
                                new MedDictionaryResponseAttribute(name: 'toResourceName', value: 'Пушкина Анна Ивановна'),
                                new MedDictionaryResponseAttribute(name: 'fromMoOid', value: '1.2.643.5.1.13.13.12.2.16.1080.0.368844'),
                                new MedDictionaryResponseAttribute(name: 'fromMoName', value: 'Отделение узких специалистов, ГАУЗ "Городская поликлиника №18"'),
                                new MedDictionaryResponseAttribute(name: 'fromSpecsId', value: '109'),
                                new MedDictionaryResponseAttribute(name: 'fromSpecsName', value: 'врач-терапевт'),
                                new MedDictionaryResponseAttribute(name: 'fromResourceName', value: 'Николаева Яна Семеновна')]),
                        new MedDictionaryResponseItem(attributes: [
                                new MedDictionaryResponseAttribute(name: 'referralId', value: '205206'),
                                new MedDictionaryResponseAttribute(name: 'referralNumber', value: '445'),
                                new MedDictionaryResponseAttribute(name: 'referralTypeId', value: '6'),
                                new MedDictionaryResponseAttribute(name: 'referralStartDate', value: '2020-01-10'),
                                new MedDictionaryResponseAttribute(name: 'referralEndDate', value: DATE_TIME_FORMATTER.format(LocalDate.now().plusDays(5))),
                                new MedDictionaryResponseAttribute(name: 'paymentSourceId', value: '1'),
                                new MedDictionaryResponseAttribute(name: 'toMoOid', value: '1.2.643.5.1.13.13.12.2.16.1179.0.221506'),
                                new MedDictionaryResponseAttribute(name: 'toMoName', value: 'Женская консультация, ГАУЗ "Городская поликлиника № 21"'),
                                new MedDictionaryResponseAttribute(name: 'toServiceId', value: 'A04.10.002.010'),
                                new MedDictionaryResponseAttribute(name: 'toServiceName', value: 'Эхокардиография с допплерографией'),
                                new MedDictionaryResponseAttribute(name: 'toSpecsId', value: '34'),
                                new MedDictionaryResponseAttribute(name: 'toSpecsName', value: 'врач-кардиолог'),
                                new MedDictionaryResponseAttribute(name: 'toResourceName', value: 'Пушкина Анна Ивановна'),
                                new MedDictionaryResponseAttribute(name: 'fromMoOid', value: '1.2.643.5.1.13.13.12.2.16.1080.0.368844'),
                                new MedDictionaryResponseAttribute(name: 'fromMoName', value: 'Отделение узких специалистов, ГАУЗ "Городская поликлиника №18"'),
                                new MedDictionaryResponseAttribute(name: 'fromSpecsId', value: '109'),
                                new MedDictionaryResponseAttribute(name: 'fromSpecsName', value: 'врач-терапевт'),
                                new MedDictionaryResponseAttribute(name: 'fromResourceName', value: 'Николаева Яна Семеновна')])])
    }

    def static eQueueServiceExpiredResponse() {
        new MedDictionaryResponse(error: [errorDetail: [errorCode: 0, errorMessage: 'Operation competed']],
                items: [
                        new MedDictionaryResponseItem(attributes: [
                                new MedDictionaryResponseAttribute(name: 'referralId', value: '205206'),
                                new MedDictionaryResponseAttribute(name: 'referralNumber', value: '445'),
                                new MedDictionaryResponseAttribute(name: 'referralTypeId', value: '6'),
                                new MedDictionaryResponseAttribute(name: 'referralStartDate', value: '2020-01-10'),
                                new MedDictionaryResponseAttribute(name: 'referralEndDate', value: '2021-06-01'),
                                new MedDictionaryResponseAttribute(name: 'paymentSourceId', value: '1'),
                                new MedDictionaryResponseAttribute(name: 'toMoOid', value: '1.2.643.5.1.13.13.12.2.16.1179.0.221506'),
                                new MedDictionaryResponseAttribute(name: 'toMoName', value: 'Женская консультация, ГАУЗ "Городская поликлиника № 21"'),
                                new MedDictionaryResponseAttribute(name: 'toServiceId', value: 'A04.10.002.010'),
                                new MedDictionaryResponseAttribute(name: 'toServiceName', value: 'Эхокардиография с допплерографией'),
                                new MedDictionaryResponseAttribute(name: 'toSpecsId', value: '34'),
                                new MedDictionaryResponseAttribute(name: 'toSpecsName', value: 'врач-кардиолог'),
                                new MedDictionaryResponseAttribute(name: 'toResourceName', value: 'Пушкина Анна Ивановна'),
                                new MedDictionaryResponseAttribute(name: 'fromMoOid', value: '1.2.643.5.1.13.13.12.2.16.1080.0.368844'),
                                new MedDictionaryResponseAttribute(name: 'fromMoName', value: 'Отделение узких специалистов, ГАУЗ "Городская поликлиника №18"'),
                                new MedDictionaryResponseAttribute(name: 'fromSpecsId', value: '109'),
                                new MedDictionaryResponseAttribute(name: 'fromSpecsName', value: 'врач-терапевт'),
                                new MedDictionaryResponseAttribute(name: 'fromResourceName', value: 'Николаева Яна Семеновна')]),
                        new MedDictionaryResponseItem(attributes: [
                                new MedDictionaryResponseAttribute(name: 'referralId', value: '205206'),
                                new MedDictionaryResponseAttribute(name: 'referralNumber', value: '445'),
                                new MedDictionaryResponseAttribute(name: 'referralTypeId', value: '6'),
                                new MedDictionaryResponseAttribute(name: 'referralStartDate', value: '2020-01-10'),
                                new MedDictionaryResponseAttribute(name: 'referralEndDate', value: '2021-06-01'),
                                new MedDictionaryResponseAttribute(name: 'paymentSourceId', value: '1'),
                                new MedDictionaryResponseAttribute(name: 'toMoOid', value: '1.2.643.5.1.13.13.12.2.16.1179.0.221506'),
                                new MedDictionaryResponseAttribute(name: 'toMoName', value: 'Женская консультация, ГАУЗ "Городская поликлиника № 21"'),
                                new MedDictionaryResponseAttribute(name: 'toServiceId', value: 'A04.10.002.010'),
                                new MedDictionaryResponseAttribute(name: 'toServiceName', value: 'Эхокардиография с допплерографией'),
                                new MedDictionaryResponseAttribute(name: 'toSpecsId', value: '34'),
                                new MedDictionaryResponseAttribute(name: 'toSpecsName', value: 'врач-кардиолог'),
                                new MedDictionaryResponseAttribute(name: 'toResourceName', value: 'Пушкина Анна Ивановна'),
                                new MedDictionaryResponseAttribute(name: 'fromMoOid', value: '1.2.643.5.1.13.13.12.2.16.1080.0.368844'),
                                new MedDictionaryResponseAttribute(name: 'fromMoName', value: 'Отделение узких специалистов, ГАУЗ "Городская поликлиника №18"'),
                                new MedDictionaryResponseAttribute(name: 'fromSpecsId', value: '109'),
                                new MedDictionaryResponseAttribute(name: 'fromSpecsName', value: 'врач-терапевт'),
                                new MedDictionaryResponseAttribute(name: 'fromResourceName', value: 'Николаева Яна Семеновна')])])
    }

    def static eQueueServiceErrorResponse() {
        new MedDictionaryResponse(error: [errorDetail: [errorCode: 6, errorMessage: 'NO_DATA:Направление пациента с указанным номером не найдено. Пожалуйста, проверьте корректность введенных выше данных.']], items: [])
    }
}
