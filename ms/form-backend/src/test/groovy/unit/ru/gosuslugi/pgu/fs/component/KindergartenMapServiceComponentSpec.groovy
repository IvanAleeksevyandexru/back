package unit.ru.gosuslugi.pgu.fs.component

import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.impl.ComponentReferenceServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.UserCookiesServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.dictionary.KindergartenMapServiceComponent
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService
import ru.gosuslugi.pgu.fs.service.LkNotifierService
import ru.gosuslugi.pgu.fs.service.impl.NsiDictionaryFilterHelper
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressElement
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService
import spock.lang.Specification

class KindergartenMapServiceComponentSpec extends Specification {

    def lkNotifierService = Mock(LkNotifierService)
    def calculatedAttributesHelper = Mock(CalculatedAttributesHelper)
    def nsiDaDataService = Mock(NsiDadataService)
    def userPersonalData = Mock(UserPersonalData)
    def nsiDictionaryService = Mock(NsiDictionaryService)
    def dictionaryFilterService = Mock(DictionaryFilterService) { it.getInitialValue(_, _) >> { [] as HashMap<String, Object> } }
    def parseAttrValuesHelper = new ParseAttrValuesHelper(Mock(VariableRegistry), Mock(JsonProcessingService), Mock(ProtectedFieldService))
    def jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
    def componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, new UserCookiesServiceImpl(), Mock(LinkedValuesService))
    def nsiDictionaryFilterHelper = new NsiDictionaryFilterHelper(parseAttrValuesHelper, componentReferenceService, jsonProcessingService)

    def kindergartenMapServiceComponent = new KindergartenMapServiceComponent(
            lkNotifierService,
            calculatedAttributesHelper,
            parseAttrValuesHelper,
            nsiDaDataService,
            userPersonalData,
            dictionaryFilterService,
            nsiDictionaryService,
            nsiDictionaryFilterHelper
    )

    def 'Preset value check #1 (No attrs in component description)'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        0 * nsiDaDataService.getAddress(_)
        fieldComponent.value == '{}'
    }

    def 'Preset value check #2 (Data of address attribute attribute not found)'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        fieldComponent.attrs = [addressString: [type: 'REF', value: 'address']]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        0 * nsiDaDataService.getAddress(_)
        fieldComponent.value == '{}'
    }

    def 'Preset value check #3 (Not correct format of external service - no errors, no address - NOT EXPECTED RESULT)'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer(value: 'г. Челябинск')]
        fieldComponent.attrs = [addressString: [type: 'REF', value: 'address']]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        1 * nsiDaDataService.getAddress('г. Челябинск') >> okDaDataResponse()
        thrown(NullPointerException)
    }

    def 'Preset value check #4 (Empty fields list and address normalize get level of region with error)'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer(value: 'г. Москва')]
        fieldComponent.attrs = [addressString: [type: 'REF', value: 'address']]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        1 * nsiDaDataService.getAddress('г. Москва') >> moscowDaDataResponse()
        1 * nsiDaDataService.getAddressByFiasCode(_) >> errorDaDataResponse()
    }

    def 'Preset value check #5 (Empty fields list and address normalize get level of region. No extra values (just standard okato and coordinates))'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer(value: 'г. Москва')]
        fieldComponent.attrs = [addressString: [type: 'REF', value: 'address']]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        1 * nsiDaDataService.getAddress('г. Москва') >> moscowDaDataResponse()
        1 * nsiDaDataService.getAddressByFiasCode(_) >> moscowDaDataResponse()
        fieldComponent.value == '''{"address":"г Москва","geo_lat":"55.7540471","oktmo_territory_11":"45000000000","geo_lon":"37.620405","fias":"0c5b2444-70a0-4932-980c-b4dc0d3f02b5","oktmo_territory_8":"45000000","okato":"45000000000"}'''
    }

    def 'Preset value check #6'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        fieldComponent.attrs = [
                addressString: [type: 'REF', value: 'address'],
                fields       : [
                        [fieldName: 'regCode', label: '', type: 'hidden']
                ]
        ]
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer(value: 'г. Санкт-Петербург')]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        1 * nsiDaDataService.getAddress('г. Санкт-Петербург') >> errorDaDataResponse()
    }

    def 'Preset value check #7 (CorrectResult from dadata and added hidden attr to value attrs list)'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer(value: 'г. Москва')]
        fieldComponent.attrs = [
                addressString: [type: 'REF', value: 'address'],
                fields       : [[fieldName: 'regCode', label: '', type: 'hidden']]
        ]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        1 * nsiDaDataService.getAddress('г. Москва') >> moscowDaDataResponse()
        fieldComponent.value == '''{"address":"г Москва","geo_lat":"55.7540471","oktmo_territory_11":"45000000000","geo_lon":"37.620405","regCode":"R77","fias":"0c5b2444-70a0-4932-980c-b4dc0d3f02b5","oktmo_territory_8":"45000000","okato":"45000000000"}'''
    }

    def 'Preset value check #8 (CorrectResult from dadata and added hidden attr to value attrs list and correct calculate mvd source field)'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer(value: 'г. Москва')]
        fieldComponent.attrs = [
                addressString: [type: 'REF', value: 'address'],
                fields       : [[fieldName: 'regCode', label: '', type: 'hidden']],
                mvdFilters   : [
                        [fiasList: ['27c5bc66-61bf-4a17-b0cd-ca0eb64192d6'], value: ''],
                        [fiasList: [
                                '294277aa-e25d-428c-95ad-46719c4ddb44',
                                '89db3198-6803-4106-9463-cbf781eff0b8',
                                'ed36085a-b2f5-454f-b9a9-1c9a678ee618',
                                '90c7181e-724f-41b3-b6c6-bd3ec7ae3f30',
                                'c2deb16a-0330-4f05-821f-1d09c93331e6',
                                '6d1ebb35-70c6-4129-bd55-da3969658f5d',
                                '1c727518-c96a-4f34-9ae6-fd510da3be03',
                                'e5a84b81-8ea1-49e3-b3c4-0528651be129',
                                'f6e148a1-c9d0-4141-a608-93e3bd95e6c4',
                                '248d8071-06e1-425e-a1cf-d1ff4c4a14a8',
                                'c20180d9-ad9c-46d1-9eff-d60bc424592a'
                        ], value : 'mvd_organization'],
                        [fiasList: ['*'], value: 'fms_5']
                ]
        ]
        scenarioDto.applicantAnswers = ["address": new ApplicantAnswer(value: 'г. Москва')]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        1 * nsiDaDataService.getAddress('г. Москва') >> moscowDaDataResponse()
        fieldComponent.value == '''{"address":"г Москва","geo_lat":"55.7540471","oktmo_territory_11":"45000000000","mvd_source":"fms_5","geo_lon":"37.620405","regCode":"R77","fias":"0c5b2444-70a0-4932-980c-b4dc0d3f02b5","oktmo_territory_8":"45000000","okato":"45000000000"}'''
    }

    def 'Preset value check #9 (Check attrs level of scenario)'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        scenarioDto.orderId = 123456
        fieldComponent.attrs = [
                fields: [
                        [fieldName: 'userId', label: '', type: 'hidden'],
                        [fieldName: 'orderId', label: '', type: 'hidden']
                ]
        ]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        1 * userPersonalData.getUserId() >> 111111111
    }

    def 'Preset value check #10 (Check person attrs level of scenario)'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        scenarioDto.orderId = 123456
        fieldComponent.attrs = [
                fields: [
                        [fieldName: 'userId', label: '', type: 'hidden'],
                        [fieldName: 'orderId', label: '', type: 'hidden']
                ]
        ]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        1 * userPersonalData.getUserId() >> 111111111
    }

    def 'Preset value check #11 (Check attrs person data)'() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def scenarioDto = new ScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        scenarioDto.applicantAnswers = [
                pd: new ApplicantAnswer(value: '''{"storedValues":{"firstName":"Игорь","lastName":"Каменев","middleName":"Витальевич"}}''')
        ]
        fieldComponent.attrs = [
                personalData: [type: 'REF', value: 'pd'],
                fields      : [
                        [fieldName: 'firstName', label: '', type: 'hidden'],
                        [fieldName: 'lastName', label: '', type: 'hidden'],
                        [fieldName: 'middleName', label: '', type: 'hidden']
                ]
        ]
        kindergartenMapServiceComponent.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        fieldComponent.value == '''{"firstName":"Игорь","lastName":"Каменев","middleName":"Витальевич"}'''
    }

    def 'Check validation #1 (Validation OFF)'() {
        given:
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        def scenarioDto = new ScenarioDto()

        when:
        fieldComponent.attrs = [validationOn: false]
        def result = kindergartenMapServiceComponent.validate(initCurrentValue(), scenarioDto, fieldComponent)

        then:
        result.size() == 0
    }

    def 'Check validation #2 (Error of filter attribute - valueType not found)'() {
        given:
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        def scenarioDto = new ScenarioDto()

        when:
        fieldComponent.attrs = [
                dictionaryType  : 'TEST_DICTIONARY',
                dictionaryFilter: [[attributeName: 'ATTR_VAL', condition: 'CONTAINS', value: 'regCode']]
        ]
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer(value: 'г. Москва')]
        kindergartenMapServiceComponent.validate(initCurrentValue(), scenarioDto, fieldComponent)

        then:
        def formBaseException = thrown(FormBaseException)
        formBaseException.message == 'Неверный формат описания условия фильтра'
    }

    def 'Check validation #3 (Check getAttrValueByType)'() {
        given:
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        def scenarioDto = new ScenarioDto()

        when:
        scenarioDto.orderId = 123456
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer(value: 'г. Москва')]
        fieldComponent.attrs = [
                addressString   : [type: 'REF', value: 'address'],
                fields          : [[fieldName: 'regCode', label: '', type: 'hidden']],
                dictionaryType  : 'TEST_DICTIONARY',
                dictionaryFilter: [
                        [attributeName: 'ATTR_PRESET', condition: 'CONTAINS', value: 'regCode', valueType: 'preset'],
                        [attributeName: 'ATTR_VALUE', condition: 'EQUALS', value: '{"asString":"true"}', valueType: 'value'],
                        [attributeName: 'ATTR_ROOT', condition: 'EQUALS', value: 'orderId', valueType: 'root'],
                        [attributeName: 'ATTR_REF', condition: 'EQUALS', value: 'address', valueType: 'ref']
                ]
        ]
        kindergartenMapServiceComponent.validate(initCurrentValue(), scenarioDto, fieldComponent)

        then:
        1 * nsiDaDataService.getAddress('г. Москва') >> moscowDaDataResponse()
        1 * nsiDictionaryService.getDictionaryItemForMapsByFilter('TEST_DICTIONARY', _) >> getDictionaryWithValue()
    }

    def 'Check validation #4 (Value in dictionary not found. Get 0 records after filter)'() {
        given:
        ComponentTestUtil.setAbstractComponentServices(kindergartenMapServiceComponent)
        def fieldComponent = new FieldComponent(type: ComponentType.MapService)
        def scenarioDto = new ScenarioDto()

        when:
        scenarioDto.orderId = 123456
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer(value: 'г. Москва')]
        fieldComponent.attrs = [
                addressString   : [type: 'REF', value: 'address'],
                fields          : [[fieldName: 'regCode', label: '', type: 'hidden']],
                dictionaryType  : 'TEST_DICTIONARY',
                dictionaryFilter: [
                        [attributeName: 'ATTR_PRESET', condition: 'CONTAINS', value: 'regCode', valueType: 'preset'],
                        [attributeName: 'ATTR_VALUE', condition: 'EQUALS', value: '{"asString":"true"}', valueType: 'value'],
                        [attributeName: 'ATTR_ROOT', condition: 'EQUALS', value: 'orderId', valueType: 'root'],
                        [attributeName: 'ATTR_REF', condition: 'EQUALS', value: 'address', valueType: 'ref']
                ]
        ]
        kindergartenMapServiceComponent.validate(initCurrentValue(), scenarioDto, fieldComponent)

        then:
        1 * nsiDaDataService.getAddress('г. Москва') >> moscowDaDataResponse()
        1 * nsiDictionaryService.getDictionaryItemForMapsByFilter('TEST_DICTIONARY', _) >> getEmptyDictionaryItemsList()
    }

    static Map.Entry<String, ApplicantAnswer> initCurrentValue() {
        def answers = [ms: [
                visited: true,
                value  : '''
                {
                  "value": "R7400042",
                  "parentValue": null,
                  "title": "Отдел ЗАГС администрации Коркинского муниципального района Челябинской области",
                  "isLeaf": true,
                  "children": null,
                  "attributes": [
                    { "name": "ZAGS_NAME", "type": "STRING", "value": { "asString": "Отдел ЗАГС администрации Коркинского муниципального района Челябинской области", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "Отдел ЗАГС администрации Коркинского муниципального района Челябинской области" }, "valueAsOfType": "Отдел ЗАГС администрации Коркинского муниципального района Челябинской области" },
                    { "name": "zags_address", "type": "STRING", "value": { "asString": "обл Челябинская, р-н Коркинский, г Коркино, ул Цвиллинга , д.1Б", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "обл Челябинская, р-н Коркинский, г Коркино, ул Цвиллинга , д.1Б" }, "valueAsOfType": "обл Челябинская, р-н Коркинский, г Коркино, ул Цвиллинга , д.1Б" },
                    { "name": "TYPE", "type": "STRING", "value": { "asString": "ZAGS", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "ZAGS" }, "valueAsOfType": "ZAGS" },
                    { "name": "SHOW_ON_MAP", "type": "STRING", "value": { "asString": "true", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "true" }, "valueAsOfType": "true" },
                    { "name": "SOLEMN", "type": "STRING", "value": { "asString": "false", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "false" }, "valueAsOfType": "false" },
                    { "name": "AREA_DESCR", "type": "STRING", "value": { "asString": null, "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": null }, "valueAsOfType": null },
                    { "name": "DATAK", "type": "STRING", "value": { "asString": null, "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": null }, "valueAsOfType": null },
                    { "name": "AREA_NAME", "type": "STRING", "value": { "asString": null, "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": null }, "valueAsOfType": null },
                    { "name": "CODE", "type": "STRING", "value": { "asString": "R7400042", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "R7400042" }, "valueAsOfType": "R7400042" },
                    { "name": "FULLNAME", "type": "STRING", "value": { "asString": "Отдел ЗАГС администрации Коркинского муниципального района Челябинской области", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "Отдел ЗАГС администрации Коркинского муниципального района Челябинской области" }, "valueAsOfType": "Отдел ЗАГС администрации Коркинского муниципального района Челябинской области" },
                    { "name": "ADDRESS", "type": "STRING", "value": { "asString": "обл Челябинская, р-н Коркинский, г Коркино, ул Цвиллинга , д.1Б", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "обл Челябинская, р-н Коркинский, г Коркино, ул Цвиллинга , д.1Б" }, "valueAsOfType": "обл Челябинская, р-н Коркинский, г Коркино, ул Цвиллинга , д.1Б" },
                    { "name": "PHONE", "type": "STRING", "value": { "asString": "(35152)4-49-55", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "(35152)4-49-55" }, "valueAsOfType": "(35152)4-49-55" },
                    { "name": "EMAIL", "type": "STRING", "value": { "asString": "korzags@mail.ru", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "korzags@mail.ru" }, "valueAsOfType": "korzags@mail.ru" },
                    { "name": "PR2", "type": "STRING", "value": { "asString": "true", "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": "true" }, "valueAsOfType": "true" },
                    { "name": "GET_CONSENT", "type": "STRING", "value": { "asString": null, "asLong": null, "asDecimal": null, "asDateTime": null, "asDate": null, "asBoolean": null, "typeOfValue": "STRING", "value": null }, "valueAsOfType": null }
                  ],
                  "attributeValues": {
                    "ZAGS_NAME": "Отдел ЗАГС администрации Коркинского муниципального района Челябинской области",
                    "AREA_NAME": null,
                    "PHONE": "(35152)4-49-55",
                    "SOLEMN": "false",
                    "zags_address": "обл Челябинская, р-н Коркинский, г Коркино, ул Цвиллинга , д.1Б",
                    "EMAIL": "korzags@mail.ru",
                    "GET_CONSENT": null,
                    "PR2": "true",
                    "CODE": "R7400042",
                    "SHOW_ON_MAP": "true",
                    "ADDRESS": "обл Челябинская, р-н Коркинский, г Коркино, ул Цвиллинга , д.1Б",
                    "DATAK": null,
                    "TYPE": "ZAGS",
                    "AREA_DESCR": null,
                    "FULLNAME": "Отдел ЗАГС администрации Коркинского муниципального района Челябинской области"
                  },
                  "idForMap": 21,
                  "center": [ 61.417304, 54.889599 ],
                  "baloonContent": [
                    { "value": "обл Челябинская, р-н Коркинский, г Коркино, ул Цвиллинга , д.1Б", "label": "Адрес" },
                    { "value": "(35152)4-49-55", "label": "Телефон" },
                    { "value": "korzags@mail.ru", "label": "Email" }
                  ],
                  "agreement": true,
                  "expanded": true,
                  "btnName": "Выбрать"
                }'''
        ] as ApplicantAnswer] as Map<String, ApplicantAnswer>
        return answers.entrySet().head()
    }

    static DadataAddressResponse okDaDataResponse() {
        [error: [code: 0, message: '']]
    }

    static DadataAddressResponse errorDaDataResponse() {
        [error: [code: 3, message: 'Error of DADATA']]
    }

    static DadataAddressResponse moscowDaDataResponse() {
        DadataAddressElement daDataAddressElement = [
                level          : 1,
                fiasCode       : '0c5b2444-70a0-4932-980c-b4dc0d3f02b5',
                kladrCode      : '7700000000000',
                data           : 'Москва',
                type           : 'город',
                shortType      : 'г',
                numericFiasCode: '77-0-000-000-000-000-0000-0000-000'
        ]

        DadataAddressResponse daDataResponse = [
                error           : [code: 0, message: 'operation completed'],
                dadataQc        : 0,
                dadataQcComplete: 3,
                dadataQcHouse   : 10,
                unparsedParts   : null,
                fiasLevel       : 1,
                postalCode      : '101000',
                postalBox       : null,
                address         : [
                        fiasCode       : '0c5b2444-70a0-4932-980c-b4dc0d3f02b5',
                        numericFiasCode: null,
                        fullAddress    : 'г Москва',
                        postIndex      : '101000',
                        elements       : [daDataAddressElement]
                ],
                regionKladrId   : null,
                okato           : '45000000000',
                tax_office      : '7700',
                oktmo           : '45000000000',
                geo_lat         : '55.7540471',
                geo_lon         : '37.620405',
        ]
        return daDataResponse
    }

    static NsiDictionary getDictionaryWithValue() {
        [total: 1, items: [[value: 'R7400042', title: 'Значение справочника']]]
    }

    static NsiDictionary getEmptyDictionaryItemsList() {
        [total: 0]
    }
}
