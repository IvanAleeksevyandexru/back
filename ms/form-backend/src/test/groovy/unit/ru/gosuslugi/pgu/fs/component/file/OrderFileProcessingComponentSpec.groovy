package unit.ru.gosuslugi.pgu.fs.component.file

import com.mchange.v1.util.SimpleMapEntry
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.csv.CsvParseDescription
import ru.gosuslugi.pgu.dto.csv.CsvParseResult
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException
import ru.gosuslugi.pgu.fs.common.service.CsvParserService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.component.file.OrderFileProcessingComponent
import ru.gosuslugi.pgu.fs.component.file.model.OrderFileProcessingDto
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo
import spock.lang.Shared
import spock.lang.Specification

class OrderFileProcessingComponentSpec extends Specification {

    OrderFileProcessingComponent component

    TerrabyteClient terabyteClientMock
    UserPersonalData userPersonalDataMock
    CsvParserService csvParserServiceMock

    @Shared
    ScenarioDto scenarioDto

    def bytes = new byte[10]
    def fileInfo = new FileInfo(fileExt: 'csv')
    def componentId = 'c1'
    def incorrectAnswers = new HashMap<>()

    def setup() {
        terabyteClientMock = Mock(TerrabyteClient)
        csvParserServiceMock = Mock(CsvParserService)

        userPersonalDataMock = Mock(UserPersonalData)
        userPersonalDataMock.userId >> 1L
        userPersonalDataMock.token >> 'token'

        component = new OrderFileProcessingComponent(userPersonalDataMock, terabyteClientMock, csvParserServiceMock)
        component.jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
    }

    def setupSpec() {
        scenarioDto = new ScenarioDto(orderId: 1L)
        scenarioDto.setCurrentValue([c1: getApplicantAnswer()])
    }

    def 'Can set value - success parsing'() {
        when:
        terabyteClientMock.getAllFilesByOrderId(_ as Long, _ as Long, _ as String) >> [fileInfo]
        terabyteClientMock.getFile(_ as FileInfo, _ as Long, _ as String) >> bytes
        csvParserServiceMock.parse(_ as File, _ as CsvParseDescription) >> new CsvParseResult([[a: 'a']])
        validateAfterSubmit()

        then:
        def result = JsonProcessingUtil.fromJson(scenarioDto.currentValue.get(componentId).value, OrderFileProcessingDto.class)
        result.data == [[a: 'a']]
    }

    def 'If file not found'() {
        when:
        terabyteClientMock.getAllFilesByOrderId(_ as Long, _ as Long, _ as String) >> []
        validateAfterSubmit()

        then:
        thrown(FormBaseException)
    }

    def 'If parsing error'() {
        when:
        terabyteClientMock.getAllFilesByOrderId(_ as Long, _ as Long, _ as String) >> [fileInfo]
        terabyteClientMock.getFile(fileInfo, _ as Long, _ as String) >> bytes
        csvParserServiceMock.parse(_ as File, _ as CsvParseDescription) >> { throw new FormBaseException('error') }
        validateAfterSubmit()

        then:
        thrown(FormBaseException)
    }

    def validateAfterSubmit() {
        Map.Entry<String, ApplicantAnswer> entry = new SimpleMapEntry('c1', getApplicantAnswer())
        FieldComponent fieldComponent = getFieldComponent()
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)
    }

    def getFieldComponent() {
        new FieldComponent(id: componentId, attrs: new HashMap<String, Object>())
    }

    static def getApplicantAnswer() {
        [visited: true, value: JsonProcessingUtil.toJson(getValue())] as ApplicantAnswer
    }

    static def getValue() {
        [id     : 'fi11',
         type   : 'OrderFileProcessingComponent',
         uploads: [[uploadId: 'csv',
                    value   : [[fileUid             : 1887605055,
                                metaId              : 1879375866,
                                objectId            : 764009105,
                                objectTypeId        : 2,
                                mnemonic            : 'fi11.FileUploadComponent.csv.0',
                                fileName            : 'кривой файл - устройства.csv',
                                fileExt             : 'csv',
                                fileSize            : 38721,
                                mimeType            : 'application/vnd.ms-excel',
                                hasSign             : false,
                                created             : '2021-07-22',
                                updated             : '2021-07-22',
                                realPath            : '33/0/0/0/0/0/377/k3fdhG9C8Vq6',
                                deleted             : false,
                                bucket              : 'epgu202107',
                                nodeId              : 'f_dc',
                                userId              : '1000305301',
                                orgId               : '1000320682',
                                alternativeMimeTypes: [],
                                uploaded            : true
                               ]]
                   ]]
        ]
    }
}
