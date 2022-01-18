package ru.gosuslugi.pgu.fs.component.file

import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.AttachmentInfo
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.service.TerrabyteService
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo
import spock.lang.Specification

class PhotoUploadComponentTest extends Specification {

    def 'it should be correct component type'() {
        given:
        def terrabyteService = Mock(TerrabyteService)
        def component = new PhotoUploadComponent(terrabyteService)

        when:
        ComponentType result = component.getType()

        then:
        result == ComponentType.PhotoUploadComponent
    }

    def 'it should have an empty initial value when argument uploadedFile is empty'() {
        given:
        def scenarioDto = Mock(ScenarioDto)
        def terrabyteService = Mock(TerrabyteService)
        def component = new PhotoUploadComponent(terrabyteService)
        def fieldComponent = fieldComponent()

        when:
        fieldComponent.attrs['uploadedFile'] = [] as HashMap<String, Object>
        def result = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        result == ComponentResponse.empty()
    }

    def 'it should correctly pre-process'() {
        given:
        def scenarioDto = Mock(ScenarioDto)
        def terrabyteService = Mock(TerrabyteService)
        def component = new PhotoUploadComponent(terrabyteService)
        def fieldComponent = fieldComponent()
        def passportFileInfo = passportFileInfo()

        when:
        terrabyteService.getAllFilesInfoForOrderId(scenarioDto) >> { [passportFileInfo] as List<FileInfo> }
        component.preProcess(fieldComponent, scenarioDto)

        then:
        fieldComponent.attrs['uploadedFile']['mnemonic'] == passportFileInfo.mnemonic
        fieldComponent.attrs['uploadedFile']['name'] == passportFileInfo.fileName
    }

    def 'it should not invoke terrabyte service on component pre-processing state while uploadedFile attribute is empty'() {
        given:
        def scenarioDto = Mock(ScenarioDto)
        def terrabyteService = Mock(TerrabyteService)
        def component = new PhotoUploadComponent(terrabyteService)
        def fieldComponent = fieldComponent()

        when:
        fieldComponent.attrs['uploadedFile'] = [] as HashMap<String, Object>
        component.preProcess(fieldComponent, scenarioDto)

        then:
        0 * terrabyteService.getAllFilesInfoForOrderId(scenarioDto)
    }

    def 'it should pre-process correctly even if the terrabyte service did not find the file'() {
        given:
        def scenarioDto = Mock(ScenarioDto)
        def terrabyteService = Mock(TerrabyteService)
        def component = new PhotoUploadComponent(terrabyteService)
        def fieldComponent = fieldComponent()

        when:
        terrabyteService.getAllFilesInfoForOrderId(scenarioDto) >> { [] as List<FileInfo> }
        component.preProcess(fieldComponent, scenarioDto)

        then:
        fieldComponent.attrs['uploadedFile']['mnemonic'] == 'pu1.PhotoUploadComponent.passport_photo.0'
    }

    def 'it should validate correctly (positive)'() {
        given:
        def scenarioDto = [] as ScenarioDto
        def terrabyteService = Mock(TerrabyteService)
        def component = new PhotoUploadComponent(terrabyteService)
        def fieldComponent = fieldComponent()
        def passportFileInfo = passportFileInfo()
        def incorrectAnswers = [] as HashMap<String, String>
        def answerEntry = AnswerUtil.createAnswerEntry('pu1', '{}')

        when:
        scenarioDto.setAttachmentInfo([] as HashMap<String, List<AttachmentInfo>>)
        scenarioDto.setCurrentValue([pu1: validApplicantAnswer()] as HashMap<String, ApplicantAnswer>)

        terrabyteService.getAllFilesInfoForOrderId(scenarioDto) >> { [passportFileInfo] as List<FileInfo> }

        component.preProcess(fieldComponent, scenarioDto)
        component.validateAfterSubmit(incorrectAnswers, answerEntry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.isEmpty()
        scenarioDto.attachmentInfo['pu1'] == [
                [
                        uploadId      : 'passport_photo',
                        uploadMnemonic: 'pd5.PhotoUploadComponent.passport_photo.0',
                        uploadFilename: 'passport.jpg',
                        objectId      : 1527044157,
                        objectTypeId  : 2
                ] as AttachmentInfo] as LinkedList<AttachmentInfo>
    }

    def 'it should validate correctly (negative) (empty current value)'() {
        given:
        def scenarioDto = [] as ScenarioDto
        def terrabyteService = Mock(TerrabyteService)
        def component = new PhotoUploadComponent(terrabyteService)
        def fieldComponent = fieldComponent()
        def passportFileInfo = passportFileInfo()
        def incorrectAnswers = [] as HashMap<String, String>
        def answerEntry = AnswerUtil.createAnswerEntry('pu1', '{}')

        when:
        scenarioDto.setAttachmentInfo([] as HashMap<String, List<AttachmentInfo>>)
        scenarioDto.setCurrentValue([pu1: ''] as HashMap<String, ApplicantAnswer>)

        terrabyteService.getAllFilesInfoForOrderId(scenarioDto) >> { [passportFileInfo] as List<FileInfo> }

        component.preProcess(fieldComponent, scenarioDto)
        component.validateAfterSubmit(incorrectAnswers, answerEntry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers['pu1'] == 'Ошибка формата данных для загрзуки файлов. Попробуйте позднее'
    }

    private static def fieldComponent() {
        [
                id   : 'pu1',
                type : ComponentType.PhotoUploadComponent,
                attrs: [uploadedFile: [uploadId: 'passport_photo', mnemonic: '', objectType: 2, name: '', fileType: ['JPEG', 'JPG', 'PNG', 'BMP'], maxSize: 5242880]]
        ] as FieldComponent
    }

    private static def passportFileInfo() {
        [
                objectId    : 1527044157,
                objectTypeId: 2,
                mnemonic    : "pu1.PhotoUploadComponent.passport_photo.0",
                fileName    : "passport.jpg",
                fileUid     : 1889442961,
                fileExt     : "jpg",
                fileSize    : 35158
        ] as FileInfo
    }

    private static def validApplicantAnswer() {
        [
                visited: true,
                value  : """{
                               "mnemonic":"pd5.PhotoUploadComponent.passport_photo.0",
                               "name":"passport.jpg",
                               "objectType":2,
                               "objectId":1527044157,
                               "mimeType":"image/jpeg",
                               "fileUid":1889442961,
                               "metaId":6441329873,
                               "objectTypeId":2,
                               "fileName":"passport.jpg",
                               "fileExt":"jpg",
                               "fileSize":35158,
                               "hasSign":false,
                               "created":"2021-01-01",
                               "updated":"2021-01-01",
                               "realPath":"38/0/0/0/0/0/1293/5WI0p5yxieVN",
                               "deleted":false,
                               "bucket":"epgu202110",
                               "nodeId":"f_dc",
                               "userId":1000305301,
                               "alternativeMimeTypes":[],
                               "s3EndpointCode":"epgu3"
                             }"""
        ] as ApplicantAnswer
    }
}
