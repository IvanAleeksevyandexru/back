package unit.ru.gosuslugi.pgu.fs.component

import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
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
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService
import ru.gosuslugi.pgu.fs.service.LkNotifierService
import ru.gosuslugi.pgu.fs.service.impl.NsiDictionaryFilterHelper
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class BarbarbokMapServiceComponentSpec extends Specification {

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

    def barbarbokMapServiceComponent = new BarbarbokMapServiceComponent(
            lkNotifierService,
            calculatedAttributesHelper,
            parseAttrValuesHelper,
            nsiDaDataService,
            userPersonalData,
            dictionaryFilterService,
            nsiDictionaryService,
            nsiDictionaryFilterHelper
    )


    def 'Put bodyRequest attribute to initialValues with replaced placeholders'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.BarbarbokMapService)
        ComponentTestUtil.setAbstractComponentServices(barbarbokMapServiceComponent)
        def scenarioDto = new ScenarioDto()

        when:
        scenarioDto.orderId = 123456
        scenarioDto.serviceCode = 10000
        scenarioDto.applicantAnswers = [test: new ApplicantAnswer(value: "replacedString")]
        fieldComponent.attrs = [
                "extData"         : "{\"key\":\"value\"}",
                "data"            : "xml string with place holder \${test.value}",
                "selectAttributes": ["parentOid", "fullAddressText", "latitude", "longtitude"]

        ]
        def result = barbarbokMapServiceComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        result.get() == "{\"smevConverterRequest\":{\"data\":\"xml string with place holder replacedString\",\"serviceId\":\"10000\",\"extData\":\"{\\\"key\\\":\\\"value\\\"}\"}}"
    }
}
