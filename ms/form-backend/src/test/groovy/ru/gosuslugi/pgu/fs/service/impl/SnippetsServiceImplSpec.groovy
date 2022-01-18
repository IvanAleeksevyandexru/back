package ru.gosuslugi.pgu.fs.service.impl

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus

class SnippetsServiceImplSpec extends Specification{
    RestTemplate restTemplate = new RestTemplate()
    MockRestServiceServer mockServer
    SnippetsServiceImpl service

    static final String endPoint = "http://localhost:8072/"
    static final Long orderId = 1111l
    static final String body = ""

    def setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate)
        service = new SnippetsServiceImpl(restTemplate)
        ReflectionTestUtils.setField(service, "endpoint", endPoint)
    }

    def "testSnippetService"() {
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/lk-api/internal/api/lk/v1/feed/ORDER/1111/custom-snippet")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                )

        when:
        service.setCustomSnippet(orderId, body)
        then:
        mockServer.verify()
    }
}