package unit.ru.gosuslugi.pgu.fs.component

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.SignInfo
import ru.gosuslugi.pgu.dto.esep.CertificateInfoDto
import ru.gosuslugi.pgu.dto.esep.CertificateUserInfoDto
import ru.gosuslugi.pgu.dto.esep.FileCertificatesUserInfoResponse
import ru.gosuslugi.pgu.dto.esep.PrepareSignResponse
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.esep.EsepSignComponent
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl
import spock.lang.Specification

class EsepSignComponentSpec extends Specification {

    EsepSignComponent component
    def restTemplateMock = Mock(RestTemplate)
    def userPersonalDataMock = Mock(UserPersonalData)
    def userOrgDataMock = Mock(UserOrgData)
    def scenarioDtoService = Mock(FormScenarioDtoServiceImpl)

    String signUrl = 'http://signUrl'
    String voshodUrl = 'http://voshodUrl'
    Long userId = 1L
    Long orderId = 1L

    Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry('key', '0')

    def setup() {
        restTemplateMock.exchange("${voshodUrl}/prepareSign", _ as HttpMethod, _ as HttpEntity, _ as Class) >>
            new ResponseEntity(new PrepareSignResponse(operationID: '1', url: signUrl, signedFileInfos: []), HttpStatus.OK)

        userPersonalDataMock.person >> new Person(lastName: 'Иванов', firstName: 'Иван', middleName: 'Иванович', snils: '111-999')
        userPersonalDataMock.userId >> userId

        component = new EsepSignComponent(restTemplateMock, userPersonalDataMock, userOrgDataMock, scenarioDtoService)
        component.voshodUrl = voshodUrl
        component.jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
    }

    def 'Can get initial value'() {
        given:
        def fieldComponent = new FieldComponent()
        def scenarioDto = new ScenarioDto(orderId: orderId)
        def serviceDescriptor = new ServiceDescriptor()
        def result

        when: 'if current url not set'
        component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        thrown(FormBaseException)

        when: 'if signing form not set'
        scenarioDto.currentUrl = 'http://currentUrl.com'
        result = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        !result.get().alreadySigned
        result.get().url == signUrl
        scenarioDto.signInfoMap.get(orderId).url == signUrl

        when: 'if signed form already exists and member not signed'
        restTemplateMock.exchange("${voshodUrl}/getFileCertificatesUserInfo", _ as HttpMethod, _ as HttpEntity, _ as Class) >>
                new ResponseEntity(new FileCertificatesUserInfoResponse(certificateInfoDtoList: []), HttpStatus.OK)
        scenarioDto.signInfoMap = [:]
        scenarioDto.signInfoMap[orderId] = new SignInfo(url: signUrl)

        result = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        !result.get().alreadySigned
        result.get().url == signUrl

        when: 'if signed form already exists and member signed'
        scenarioDto.signInfoMap[orderId].alreadySigned = true
        result = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        result.get().alreadySigned
    }

    def 'Can validate component: If already signed'() {
        given:
        Map<String, String> incorrectAnswers = [:]
        ScenarioDto scenarioDto = new ScenarioDto(orderId: orderId, signInfoMap: Map.of(orderId, new SignInfo(alreadySigned: true)))

        when:
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, Stub(FieldComponent))

        then:
        incorrectAnswers.isEmpty()
    }

    def 'Can validate component: User signed but not preserved'() {
        given:
        Map<String, String> incorrectAnswers = [:]
        ScenarioDto scenarioDto = new ScenarioDto(orderId: orderId, signInfoMap: Map.of(orderId, new SignInfo()))

        when:
        restTemplateMock.exchange("${voshodUrl}/getFileCertificatesUserInfo", _ as HttpMethod, _ as HttpEntity, _ as Class) >>
                new ResponseEntity(new FileCertificatesUserInfoResponse(certificateInfoDtoList: [
                        new CertificateInfoDto(certificateUserInfoList: [
                                new CertificateUserInfoDto(snils: '111 999', commonName: 'Иванов Иван Иванович')
                        ])]), HttpStatus.OK)
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, Stub(FieldComponent))

        then:
        incorrectAnswers.isEmpty()
    }

    def 'Can validate component: Not signed by user'() {
        Map<String, String> incorrectAnswers = [:]
        Map<Long, SignInfo> signInfoMap = new HashMap<>()
        signInfoMap.put(orderId, new SignInfo())
        ScenarioDto scenarioDto = new ScenarioDto(orderId: orderId, signInfoMap: signInfoMap, currentUrl: "https://gosuslugi.ru/600100/1/form")

        when:
        restTemplateMock.exchange("${voshodUrl}/getFileCertificatesUserInfo", _ as HttpMethod, _ as HttpEntity, _ as Class) >>
                new ResponseEntity(new FileCertificatesUserInfoResponse(certificateInfoDtoList: [
                        new CertificateInfoDto(certificateUserInfoList: [
                                new CertificateUserInfoDto(snils: '111 222', commonName: 'Иванов Иван Иванович')
                        ])]), HttpStatus.OK)
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, Stub(FieldComponent))

        then:
        incorrectAnswers.size() == 1
        incorrectAnswers[entry.key] == 'Заявление было подписано чужой подписью'
    }
}
