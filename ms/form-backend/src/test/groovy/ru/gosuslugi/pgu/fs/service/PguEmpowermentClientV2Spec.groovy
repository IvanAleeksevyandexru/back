package ru.gosuslugi.pgu.fs.service

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.fs.pgu.client.PguEmpowermentClientV2
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguEmpowermentClientImplV2

import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus

class PguEmpowermentClientV2Spec extends Specification {
    RestTemplate restTemplate = new RestTemplate()
    MockRestServiceServer mockServer
    PguEmpowermentClientV2 apiClient

    static final String authToken = "111"
    static final Long oid = 123L

    def setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate)
        apiClient = new PguEmpowermentClientImplV2(restTemplate)
        ReflectionTestUtils.setField(apiClient, "url", "http://localhost:8072/")

    }

    def "testClientV2"() {
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/esia-rs/api/public/v1/pow/obj/123/poweratt")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(JsonProcessingUtil.toJson("{\n" +
                                        "  \"elements\": [\n" +
                                        "    {\n" +
                                        "      \"empowerments\": {\n" +
                                        "        \"elements\": [\n" +
                                        "          {\n" +
                                        "            \"mnemonic\": \"MINISTRYOFMAGIC1111111\"\n" +
                                        "          }\n" +
                                        "        ]\n" +
                                        "      }\n" +
                                        "    }\n" +
                                        "  ]\n" +
                                        "}")))

        when:
        Set<String> resultSet = apiClient.getUserEmpowerment(authToken, oid)

        then:
        resultSet.toString() == "[MINISTRYOFMAGIC1111111]"

        mockServer.verify()
    }
}