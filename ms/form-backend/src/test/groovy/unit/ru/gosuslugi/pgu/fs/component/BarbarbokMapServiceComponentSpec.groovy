package unit.ru.gosuslugi.pgu.fs.component


import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.BackRestCallResponseDto
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.impl.ComponentReferenceServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.UserCookiesServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.dictionary.BarbarbokMapServiceComponent
import ru.gosuslugi.pgu.fs.component.logic.BackRestCallComponent
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService
import ru.gosuslugi.pgu.fs.service.LkNotifierService
import ru.gosuslugi.pgu.fs.service.impl.NsiDictionaryFilterHelper
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService
import spock.lang.Specification

class BarbarbokMapServiceComponentSpec extends Specification {

    private static data = '<ns1:InputData><ns1:MedicalOrgListRequest><ns1:RegionCode>79000</ns1:RegionCode><ns1:ApplicationSpec>Терапевтический</ns1:ApplicationSpec><ns1:AgeCategory>взрослое</ns1:AgeCategory><ns1:Address><ns1:FullAddressText>385003, Республика Адыгея, г.Майкоп, ул.Прохладная, д.21, кв.1</ns1:FullAddressText></ns1:Address></ns1:MedicalOrgListRequest></ns1:InputData>'
    private static templateName = 'MedicalOrgList'

    @SuppressWarnings("GroovyAccessibility")
    def 'Put bodyRequest attribute to initialValues with replaced placeholders'() {
        given:
        def lkNotifierService = Mock(LkNotifierService)
        def calculatedAttributesHelper = Mock(CalculatedAttributesHelper)
        def nsiDaDataService = Mock(NsiDadataService)
        def userPersonalData = Mock(UserPersonalData)
        def nsiDictionaryService = Mock(NsiDictionaryService)
        def dictionaryFilterService = Mock(DictionaryFilterService) { it.getInitialValue(_, _) >> { [] as HashMap<String, Object> } }
        def parseAttrValuesHelper = new ParseAttrValuesHelper(Mock(VariableRegistry), Mock(JsonProcessingService), Mock(ProtectedFieldService))
        def jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
        def componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, new UserCookiesServiceImpl(), Mock(LinkedValuesService))
        def nsiDictionaryFilterHelper = new NsiDictionaryFilterHelper(parseAttrValuesHelper, componentReferenceService)

        def backRestCallComponent = Stub(BackRestCallComponent) {
            it.getResponse(_) >> new BackRestCallResponseDto(200, getDictionary())
        }
        def component = new BarbarbokMapServiceComponent(
                lkNotifierService,
                calculatedAttributesHelper,
                parseAttrValuesHelper,
                nsiDaDataService,
                userPersonalData,
                dictionaryFilterService,
                nsiDictionaryService,
                nsiDictionaryFilterHelper,
                backRestCallComponent,
                'https://smev-converter-url')
        component.objectMapper = JsonProcessingUtil.getObjectMapper()

        def fieldComponent = getFieldComponent()
        ComponentTestUtil.setAbstractComponentServices(component)
        def scenarioDto = new ScenarioDto()

        when:
        scenarioDto.orderId = 123456
        scenarioDto.serviceCode = '10000000360'
        def result = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        component.getType() == ComponentType.BarbarbokMapService
        result.get() == '{"barbarbokResponse":{"total":"2","items":[{"value":"123","title":"Городская поликлиника №1","attributeValues":{"fullAddressText":"195197 г. Санкт-Петербург Васенко ул 9"}},{"value":"456","title":"1-е отделение общей врачебной практики","attributeValues":{"fullAddressText":"195197 г. Санкт-Петербург Федосеенко ул 16, литера А"}}]}}'
    }

    def static getFieldComponent() {
        [
                id   : 'brcc1',
                attrs: [
                        data        : data,
                        templateName: templateName
                ] as Map
        ] as FieldComponent
    }

    def static getDictionary() {
        ["total": "2",
         "items": [
                 [
                         "value"          : "123",
                         "title"          : "Городская поликлиника №1",
                         "attributeValues": [
                                 "fullAddressText": "195197 г. Санкт-Петербург Васенко ул 9"
                         ]
                 ] as NsiDictionaryItem,
                 [
                         "value"          : "456",
                         "title"          : "1-е отделение общей врачебной практики",
                         "attributeValues": [
                                 "fullAddressText": "195197 г. Санкт-Петербург Федосеенко ул 16, литера А"
                         ]
                 ] as NsiDictionaryItem
         ] as List<NsiDictionaryItem>
        ]
    }
}
