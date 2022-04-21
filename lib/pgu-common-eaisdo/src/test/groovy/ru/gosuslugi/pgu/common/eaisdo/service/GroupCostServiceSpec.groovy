package ru.gosuslugi.pgu.common.eaisdo.service


import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import spock.lang.Shared
import spock.lang.Specification

class GroupCostServiceSpec extends Specification {

    @Shared
    RestTemplate restTemplate
    MockRestServiceServer mockServer
    GroupCostService apiClient

    static String serviceUrl = "http://localhost:8072/"
    static String servicePath
    static FieldComponent component
    static String EAISDO_API_ERROR

    def setupSpec() {
        restTemplate = new RestTemplate()

        def fieldErrorString = GroupCostService.class.getDeclaredField("EAISDO_API_ERROR")
        fieldErrorString.setAccessible(true)
        EAISDO_API_ERROR = fieldErrorString.get(null)

        def fieldServicePath = GroupCostService.class.getDeclaredField("EAISDO_API_PATH")
        fieldServicePath.setAccessible(true)
        servicePath = fieldServicePath.get(null)
    }

    def setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate)
        apiClient = new GroupCostService(restTemplate)
        ReflectionTestUtils.setField(apiClient, "serviceUrl", serviceUrl)
        component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component.json"), FieldComponent)
    }

    def "service code is eaisdoGroupCostRequest"() {
        expect:
        apiClient.getServiceCode() == "eaisdoGroupCostRequest"
    }

    def "when response is successful and body contains good xml"() {
        given:
        mockServer.expect(ExpectedCount.once(),
                MockRestRequestMatchers.requestTo(new URI(serviceUrl + servicePath)))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators
                        .withSuccess("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<groupCostFreeOfChargeResponse xmlns=\"http://epgu.aisdopobr.ru/dataexchanges/group-cost/1.0.0\">\n  <orderAllowed>true</orderAllowed>\n  <message><![CDATA[Обучение по программе оплачивается за счет заявителя. Ежемесячное стоимость обучения в настоящий момент не установлено, уточняйте у администратора организации.]]></message>\n  <certificateBalance>\n    <registryBalance>2</registryBalance>\n    <registryBookedAmount>1</registryBookedAmount>\n  </certificateBalance>\n</groupCostFreeOfChargeResponse>",
                                MediaType.APPLICATION_XML))

        when:
        String responseString = apiClient.sendRequest(component)
        Map map = JsonProcessingUtil.fromJson(responseString, Map) as HashMap<String, Object>

        then:
        (map.get("responseData") as LinkedHashMap).get("type") == "GroupCostFreeOfChargeResponse"
        ((map.get("responseData") as LinkedHashMap).get("value") as LinkedHashMap).get("orderAllowed") == true
        new ArrayList(map.get("errorList") as Collection).size() == 0

        mockServer.verify()
    }

    def "when response is successful and body contains bad xml then catch JAXBException"() {
        given:
        mockServer.expect(ExpectedCount.once(),
                MockRestRequestMatchers.requestTo(new URI(serviceUrl + servicePath)))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess("Bad xml", MediaType.APPLICATION_XML))

        when:
        String responseString = apiClient.sendRequest(component)

        then:
        responseString == EAISDO_API_ERROR
        mockServer.verify()
    }

    def "when BadRequest then get externalRequestError"() {
        given:
        mockServer.expect(ExpectedCount.once(),
                MockRestRequestMatchers.requestTo(new URI(serviceUrl + servicePath)))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withBadRequest())

        when:
        String responseString = apiClient.sendRequest(component)

        then:
        responseString == EAISDO_API_ERROR
        mockServer.verify()
    }
}