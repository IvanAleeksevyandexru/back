package ru.gosuslugi.pgu.fs.component.logic

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.BackRestCallResponseDto
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.RestClientRegistry
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto
import ru.gosuslugi.pgu.fs.service.BackRestCallService
import ru.gosuslugi.pgu.fs.service.impl.BackRestCallServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.RestCallServiceImpl
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

class BackRestCallComponentSpec extends Specification {

    private static
    @Shared
    RestTemplate restTemplate
    MockRestServiceServer mockServer
    @Shared
    RestCallComponent restCallComponent
    BackRestCallComponent component
    @Shared
    BackRestCallService restCallService

    def setupSpec() {
        restTemplate = new RestTemplate()
        def restClientRegistry = new RestClientRegistry(restTemplate, Mock(RestTemplateBuilder))
        restCallService = new BackRestCallServiceImpl(restClientRegistry, JsonProcessingUtil.getObjectMapper())
        restCallComponent = new RestCallComponent('http://url_to_service', new RestCallServiceImpl())
        ComponentTestUtil.setAbstractComponentServices(restCallComponent)
    }

    def setup() {
        component = new BackRestCallComponent(new RestCallServiceImpl(), restCallService, Mock(UserPersonalData))
        ComponentTestUtil.setAbstractComponentServices(component)
        mockServer = MockRestServiceServer.createServer(restTemplate)
    }

    def test() {
        given:
        def scenarioDto = new ScenarioDto()
        def fieldComponent = getFieldComponent()
        mockServer.expect(ExpectedCount.once(), requestTo(new URI('https://yandex.ru/search/xml?user=mister-x&key=03.24748491:1334342&filter=strict')))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('<?xml version="1.0" encoding="UTF-8"?><request><query>hello</query><sortby>tm</sortby></request>'))
                .andRespond(withSuccess())

        when:
        def initialValue = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        component.getType() == ComponentType.BackRestCall
        initialValue == ComponentResponse.empty()
        scenarioDto.getApplicantAnswers().size() == 1
        fieldComponent.getAttrs().isEmpty()
        fieldComponent.getLinkedValues().isEmpty()
        fieldComponent.getArguments().isEmpty()
        mockServer.verify()
    }

    def 'test component with form data'() {
        given:
        def fieldComponent = getFieldComponentWithformData()
        mockServer.expect(ExpectedCount.once(), requestTo(new URI('http://gosuslugi.ru/path')))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('priority=NORMAL&sql=SELECT+*+FROM+users+LIMIT+10'))
                .andRespond(withSuccess())

        when:
        component.getInitialValue(fieldComponent, new ScenarioDto())

        then:
        mockServer.verify()
    }

    def exceptions() {
        given:
        def expectedValue = new BackRestCallResponseDto(status.value(), null)

        def restTemplate = Stub(RestTemplate) {
            it.exchange(_, _, _, _) >> { throw new HttpClientErrorException(status) }
        }

        def restClientRegistry = Stub(RestClientRegistry) {
            it.getRestTemplate(-1) >> restTemplate
        }

        def restCallService = new BackRestCallServiceImpl(restClientRegistry, JsonProcessingUtil.getObjectMapper())

        def request = new RestCallDto()
        request.setMethod(HttpMethod.POST.toString())

        when:
        def result = restCallService.sendRequest(request)

        then:
        result.getStatusCode() == expectedValue.getStatusCode()

        where:
        status                           | type
        HttpStatus.REQUEST_TIMEOUT       | ExternalServiceException.class
        HttpStatus.INTERNAL_SERVER_ERROR | ExternalServiceException.class
    }

    def static getFieldComponent() {
        [
                id          : 'brc1',
                type        : 'BackRestCall',
                attrs       : [
                        method         : 'POST',
                        url            : 'https://yandex.ru',
                        path           : '/search/xml',
                        esia_auth      : false,
                        body           : '<?xml version="1.0" encoding="UTF-8"?><request><query>${queryArg}</query><sortby>tm</sortby></request>',
                        headers        : ['Content-Type': 'application/x-www-form-urlencoded', 'Accept-Version': '1'],
                        cookies        : ['u': '1000298933', 'acc_t': 'eyJ2ZXIiOjEsInR5cCI6IkpXVCIs'],
                        timeout        : -1,
                        queryParameters: ['user': 'mister-x', 'key': '03.24748491:1334342', 'filter': 'strict'] as LinkedHashMap<String, String>
                ] as Map,
                linkedValues: [["argument": "someNumber", "defaultValue": "100500"]] as List<LinkedValue>,
                arguments   : [queryArg: 'hello']
        ] as FieldComponent
    }

    def static getFieldComponentWithformData() {
        [
                id          : 'brc2',
                type        : 'BackRestCall',
                attrs       : [
                        method   : 'POST',
                        url      : 'http://gosuslugi.ru',
                        path     : '/path',
                        esia_auth: false,
                        formData : ['sql': 'SELECT * FROM users LIMIT 10', 'priority': 'NORMAL'] as Map,
                        headers  : ['Content-Type': 'application/x-www-form-urlencoded', 'Accept-Version': '1']
                ] as Map,
                linkedValues: [["argument": "someNumber", "defaultValue": "100500"]] as List<LinkedValue>
        ] as FieldComponent
    }
}
