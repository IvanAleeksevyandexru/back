package ru.gosuslugi.pgu.fs.service

import org.junit.Assert
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.fs.pgu.client.PguMaritalClient
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguMaritalClientImpl
import ru.gosuslugi.pgu.fs.pgu.dto.MaritalResponseItem
import spock.lang.Specification
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus

class PguMaritalClientImplSpec extends Specification {
    RestTemplate restTemplate = new RestTemplate()
    MockRestServiceServer mockServer
    PguMaritalClient apiClient

    static final String authToken = "111"
    static final Long oid = 123L
    static final String documentType = "MARRIED_CERT"

    def setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate)
        apiClient = new PguMaritalClientImpl(restTemplate)
        ReflectionTestUtils.setField(apiClient, "url", "http://localhost:8072/")
    }

    def "testMarriageClient"() {
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/digital/api/public/v1/pso/123/doc/MARRIED_CERT")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("[\n" +
                                        "  {\n" +
                                        "    \"issuedBy\": \"загс спб помидоркин\",\n" +
                                        "    \"actNo\": \"3432424\",\n" +
                                        "    \"actDate\": \"05.08.2001\",\n" +
                                        "    \"type\": \"MARRIED_CERT\"\n" +
                                        "  }\n" +
                                        "]"))

        when:
        List<MaritalResponseItem> resultLst = apiClient.getMaritalStatusCertificate(authToken, oid, documentType)

        then:
        Assert.assertEquals(resultLst.get(0).getActDate(), "05.08.2001")
        Assert.assertEquals(resultLst.get(0).getActNo(), "3432424")
        Assert.assertEquals(resultLst.get(0).getIssuedBy(), "загс спб помидоркин")
        mockServer.verify()
    }
}
