package unit.ru.gosuslugi.pgu.fs.component

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.BaseComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.component.userdata.MaritalStatusInput
import ru.gosuslugi.pgu.fs.pgu.client.PguMaritalClient
import ru.gosuslugi.pgu.fs.pgu.dto.MaritalResponseItem
import ru.gosuslugi.pgu.fs.pgu.dto.MaritalStatusInputResponseDto
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiSuggestDictionaryItem
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService
import spock.lang.Specification
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil

class MaritalStatusComponentSpec extends Specification {

    PguMaritalClient pguMaritalClientMock
    DictionaryFilterService dictionaryFilterServiceMock
    UserPersonalData userPersonalData
    NsiDictionaryService nsiDictionaryServiceMock
    MockRestServiceServer mockServer
    RestTemplate restTemplate = new RestTemplate()

    def setup() {
        userPersonalData = Mock(UserPersonalData)
        userPersonalData.getPerson() >> new Person()
        userPersonalData.getToken() >> "token"
        userPersonalData.getUserId() >> { 1000298933L }
        pguMaritalClientMock = Mock(PguMaritalClient)
        dictionaryFilterServiceMock = Mock(DictionaryFilterService)
        nsiDictionaryServiceMock = Mock(NsiDictionaryService)
        mockServer = MockRestServiceServer.createServer(restTemplate)
    }

    def "GetInitialValue - checking certificate is found and validated by nsi-suggest"() {
        given:
        BaseComponent<MaritalStatusInputResponseDto> maritalStatusInputComponent = new MaritalStatusInput(
                userPersonalData, pguMaritalClientMock, dictionaryFilterServiceMock, nsiDictionaryServiceMock)
        def fieldComponent = new FieldComponent(id: "ai1", type: ComponentType.MaritalStatusInput, attrs: [documentType: 'MARRIED_CERT'], required: true)
        List<MaritalResponseItem> maritalResponseItemList = [new MaritalResponseItem(
                actDate: '01.03.2016', actNo: 'R7750064', issuedBy: 'Орган ЗАГС Москвы', series: '01', number: '02', issueDate: '03.03.2003')]
        pguMaritalClientMock.getMaritalStatusCertificate(_ as String, _ as Long, _ as String) >> maritalResponseItemList
        Map<String, Object> nsiSuggestOriginalItem =
                JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-ZAGS_ALL.json"), new TypeReference<Map<String, Object>>() {
                })
        nsiDictionaryServiceMock.getNsiDictionaryItemByValue(_ as String, _ as String, _ as String) >> nsiSuggestOriginalItem

        Map originalItemMap = new LinkedHashMap()
        originalItemMap.put('total', "1")
        originalItemMap.put('lastPage', "false")
        originalItemMap.put('code', "R7750064")
        originalItemMap.put('fullname', "Орган ЗАГС Москвы")
        def nsiSuggestDictionaryItem = new NsiSuggestDictionaryItem()
        nsiSuggestDictionaryItem.setOriginalItem(originalItemMap)
        nsiSuggestDictionaryItem.setId('R7750064')
        nsiSuggestDictionaryItem.setText('Орган ЗАГС Москвы')
        def maritalStatusInputResponseDto = new MaritalStatusInputResponseDto()
        maritalStatusInputResponseDto.setAct_rec_date('2016-03-01')
        maritalStatusInputResponseDto.setAct_rec_number('R7750064')
        maritalStatusInputResponseDto.setAct_rec_registrator(nsiSuggestDictionaryItem)
        maritalStatusInputResponseDto.setSeries('01')
        maritalStatusInputResponseDto.setNumber('02')
        maritalStatusInputResponseDto.setIssueDate('2003-03-03')

        def expected = maritalStatusInputResponseDto

        when:
        def actual = maritalStatusInputComponent.getInitialValue(fieldComponent)

        then:
        actual != null
        def actualDto = actual.get()
        expected.act_rec_date == actualDto.act_rec_date
        expected.act_rec_number == actualDto.act_rec_number
        expected.act_rec_registrator.id == actualDto.act_rec_registrator.id
        expected.act_rec_registrator.text == actualDto.act_rec_registrator.text
        expected.act_rec_registrator.originalItem.get('fullname') ==  actualDto.act_rec_registrator.originalItem.get('fullname')
        expected.act_rec_registrator.originalItem.get('code') == actualDto.act_rec_registrator.originalItem.get('code')

        expected.series == actualDto.series
        expected.number == actualDto.number
        expected.issueDate == actualDto.issueDate
    }

    def "GetInitialValue - no any certificates"() {
        given:
        BaseComponent<MaritalStatusInputResponseDto> component = new MaritalStatusInput(
                userPersonalData, pguMaritalClientMock, dictionaryFilterServiceMock, nsiDictionaryServiceMock)
        def fieldComponent = new FieldComponent(id: "ai1", type: ComponentType.MaritalStatusInput, attrs: [documentType: 'MARRIED_CERT'], required: true)
        List<MaritalResponseItem> certificatelist = []
        pguMaritalClientMock.getMaritalStatusCertificate(_ as String, _ as Long, _ as String) >> certificatelist

        when:
        def actual = component.getInitialValue(fieldComponent)

        then:
        actual == ComponentResponse.empty()
    }

    def "GetInitialValue - checking ZAGS not found in nsi-suggest"() {
        given:
        BaseComponent<MaritalStatusInputResponseDto> component = new MaritalStatusInput(
                userPersonalData, pguMaritalClientMock, dictionaryFilterServiceMock, nsiDictionaryServiceMock)
        def fieldComponent = new FieldComponent(id: "ai1", type: ComponentType.MaritalStatusInput, attrs: [documentType: 'MARRIED_CERT'], required: true)
        List<MaritalResponseItem> maritalResponseItemList = [new MaritalResponseItem(actDate: '01.03.2016', actNo: 'R7750064', issuedBy: 'Несуществующий', series: '01', number: '02', issueDate: '03.03.2003')]
        pguMaritalClientMock.getMaritalStatusCertificate(_ as String, _ as Long, _ as String) >> maritalResponseItemList

        when:
        def actual = component.getInitialValue(fieldComponent)

        then:
        actual != null
        def actualDto = actual.get()
        actualDto.act_rec_registrator == null
    }

}
