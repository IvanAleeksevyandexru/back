package ru.gosuslugi.pgu.fs.service

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.dto.PguTimer
import ru.gosuslugi.pgu.fs.service.timer.impl.TimerClientImpl
import ru.gosuslugi.pgu.fs.service.timer.model.TimerRequestParameters
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus

class TimerClientImplSpec extends Specification {

    RestTemplate restTemplate = new RestTemplate()
    MockRestServiceServer mockServer
    TimerClient apiClient

    static final String timerUUID = "6b249b4-f114-4e74-a0cd-3ede8c19b941"
    static final String timerCode = "ResendInvitation5m"
    static final String authToken = "111"

    def setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate)
        apiClient = new TimerClientImpl(restTemplate, "http://localhost:8072")
    }

    def "testGet"() {

        given:
        String code = "testcode"
        int duration = 15

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/api/timer-service/v1/ResendInvitation5m/6b249b4-f114-4e74-a0cd-3ede8c19b941?uuid=6b249b4-f114-4e74-a0cd-3ede8c19b941")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.header("Cookie", "acc_t=" + authToken))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"code\":\"" + code + "\", \"duration\":" + duration + "}")
                )

        TimerRequestParameters parameters = new TimerRequestParameters(timerUUID, timerUUID, timerCode)

        when:
        PguTimer dto = apiClient.getTimer(parameters, authToken)

        then:
        dto.getCode() == code
        dto.getDuration() == duration
        mockServer.verify()
    }
}