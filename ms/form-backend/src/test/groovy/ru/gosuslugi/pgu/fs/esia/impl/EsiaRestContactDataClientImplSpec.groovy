package ru.gosuslugi.pgu.fs.esia.impl

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.web.client.RestTemplate
import ru.atc.carcass.security.rest.model.EsiaAddress
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.fs.esia.EsiaRestContactDataClient
import ru.gosuslugi.pgu.fs.esia.config.EsiaRestProperties
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus

class EsiaRestContactDataClientImplSpec extends Specification {

    MockRestServiceServer mockServer
    @Shared EsiaRestContactDataClient apiClient
    @Shared RestTemplate restTemplate

    def setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate)
    }

    def setupSpec() {
        restTemplate = new RestTemplate()
        def esiaRestProperties = new EsiaRestProperties()
        esiaRestProperties.setUrl("http://localhost:8072/esia-rs/api/public")
        esiaRestProperties.setVersion("v2")

        def userPersonalData = new UserPersonalData()
        userPersonalData.setToken("123")
        userPersonalData.setUserId(1000473509L)

        def UserOrgData userOrgData = new UserOrgData()

        apiClient = new EsiaRestContactDataClientImpl(esiaRestProperties, userPersonalData, userOrgData, restTemplate)
    }

    def 'Test resend code'() {
        given:
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/esia-rs/api/public/v2/prns/1000473509/ctts/14452837/rfrCode")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header("Authorization", "Bearer 123"))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{}"))

        when:
        Boolean result = apiClient.resendCode("14452837")

        then:
        result
        mockServer.verify()
    }

    def 'Test update address'() {
        given:
        String newAddress = JsonFileUtil.getJsonFromFile(this.getClass(), '-address.json')
        EsiaAddress esiaAddress = JsonProcessingUtil.fromJson(newAddress, EsiaAddress.class)

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/esia-rs/api/public/v2/prns/1000473509/addrs")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header("Authorization", "Bearer 123"))
                .andExpect(content().json(newAddress))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(
                                        "{" +
                                                "\"type\": \"PLV\"," +
                                                "\"addressStr\":\"Свердловская Область, Талицкий Район, Зырянка деревня\",\n" +
                                                "\"fiasCode\": \"66-0-000-000-000-000-4236-0000-000\"" +
                                                "}"
                                )
                )

        when:
        EsiaAddress result = apiClient.updateAddress(esiaAddress)

        then:
        result != null
        result.getType() == 'PLV'
        result.getAddressStr() == 'Свердловская Область, Талицкий Район, Зырянка деревня'
        result.getFiasCode() == '66-0-000-000-000-000-4236-0000-000'
        mockServer.verify()
    }
}
