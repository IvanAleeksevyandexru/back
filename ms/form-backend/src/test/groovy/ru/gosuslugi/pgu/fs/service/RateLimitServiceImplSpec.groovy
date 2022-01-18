package ru.gosuslugi.pgu.fs.service

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitRequest
import ru.gosuslugi.pgu.fs.config.properties.RateLimitProperties
import ru.gosuslugi.pgu.fs.service.ratelimit.impl.RateLimitServiceImpl
import ru.gosuslugi.pgu.fs.service.ratelimit.impl.RateLimitServiceStub
import spock.lang.Specification

class RateLimitServiceImplSpec extends Specification {
    static String pguUrl = 'http://pgu-dev-fednlb.test.gosuslugi.ru/ratelimit-api/internal/api/ratelimit/'

    RateLimitServiceImpl service
    RestTemplate restTemplate
    RateLimitProperties properties
    MockRestServiceServer mockServer



    def setup() {
        restTemplate = new RestTemplate()
        properties = new RateLimitProperties()
        properties.setPguUrl(pguUrl)
        properties.setLimit(1000)
        properties.setTtl(86400)
        mockServer = MockRestServiceServer.createServer(restTemplate)
        service = new RateLimitServiceImpl(restTemplate,properties)

    }

    @SuppressWarnings("GroovyAccessibility")
    def 'Success response test'() {
        given:
        def key = 'someuserkey'

        mockServer.expect(ExpectedCount.once(),
                MockRestRequestMatchers.requestTo(new URI(pguUrl + "v1" + '/api-check?key=' + key
                        +'&lim=' + properties.limit + '&ttl=' + properties.ttl)))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess())

        when:
        def result = service.apiCheck(key, "Исчерпаны ошибки")

        then:
        result == null
        noExceptionThrown()
        mockServer.verify()
    }

    @SuppressWarnings("GroovyAccessibility")
    def 'Too many requests test'() {
        given:
        def key = 'userkey_' + (Math.abs(new Random().nextInt() % Integer.MAX_VALUE) + 1) as String
        def limit = '1'

        mockServer.expect(ExpectedCount.once(),
                MockRestRequestMatchers.requestTo(new URI(pguUrl + "v1" + '/api-check?key=' + key
                        +'&lim=' + properties.limit + '&ttl=' + properties.ttl)))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.TOO_MANY_REQUESTS))

        when:
        service.apiCheck(new RateLimitRequest(), key)

        then:
        thrown(ExternalServiceException)
        mockServer.verify()
    }


    def 'When property mock.ratelimit enabled test'() {
        given:
        def key = 'userkey_' + (Math.abs(new Random().nextInt() % Integer.MAX_VALUE) + 1) as String

        def service = new RateLimitServiceStub()

        when:
        service.apiCheck(new RateLimitRequest(), key)

        then:
        noExceptionThrown()
    }

}
