package ru.gosuslugi.pgu.fs.component.file

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo
import spock.lang.Shared
import spock.lang.Specification

import static ru.gosuslugi.pgu.fs.component.file.UploadFileComponent.EMPTY_ANSWER_MESSAGE
import static ru.gosuslugi.pgu.fs.component.file.UploadFileComponent.REQUIRED_FILES_WERE_NOT_FOUND

class UploadFileComponentTest extends Specification {

    private static final String TEST_USER_TOKEN = "token"
    private static final String TEST_COMPONENT_ID = "fu1"
    private static final long TEST_OBJECT_ID = 100

    @Shared
    private ScenarioDto scenarioDto = Stub(ScenarioDto) { it.getOrderId() >> TEST_OBJECT_ID }

    @Shared
    private UserPersonalData userPersonalData = Stub(UserPersonalData) { it.getToken() >> TEST_USER_TOKEN }

    @Shared
    private UploadFileComponent helper

    def 'testNotRequiredFilesEmptyAnswer'() {
        given:
        TerrabyteClient terrabyteClient = Stub(TerrabyteClient) { it.getAllFilesByOrderId(_ as Long, _ as Long, _ as String) >> { getPassportAndSnilsResponse() } }
        helper = new UploadFileComponent(userPersonalData, terrabyteClient)
        ComponentTestUtil.setAbstractComponentServices(helper)
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component-not-required.json"), FieldComponent.class)

        when:
        Map<String, String> errors = helper.validate(entry, scenarioDto, component)

        then:
        errors.isEmpty()

        where:
        entry << Map.of(TEST_COMPONENT_ID, new ApplicantAnswer()).entrySet()
    }

    def 'testRequiredFilesEmptyAnswer'() {
        given:
        TerrabyteClient terrabyteClient = Stub(TerrabyteClient)
        helper = new UploadFileComponent(userPersonalData, terrabyteClient)
        ComponentTestUtil.setAbstractComponentServices(helper)
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component-required.json"), FieldComponent.class)

        when:
        Map<String, String> errors = helper.validate(entry, scenarioDto, component)

        then:
        !errors.isEmpty()
        errors.get(TEST_COMPONENT_ID) == EMPTY_ANSWER_MESSAGE

        where:
        entry << Map.of(TEST_COMPONENT_ID, new ApplicantAnswer()).entrySet()
    }

    def 'testRequiredFilesRequiredAnswer'() {
        given:
        TerrabyteClient terrabyteClient = Stub(TerrabyteClient) { it.getAllFilesByOrderId(_ as Long, _ as Long, _ as String) >> { getPassportResponse() } }
        helper = new UploadFileComponent(userPersonalData, terrabyteClient)
        ComponentTestUtil.setAbstractComponentServices(helper)
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component-required.json"), FieldComponent.class)

        when:
        Map<String, String> errors = helper.validate(entry, scenarioDto, component)

        then:
        errors.isEmpty()

        where:
        entry << Map.of(TEST_COMPONENT_ID, new ApplicantAnswer(true, JsonFileUtil.getJsonFromFile(this.getClass(), "-answer-required.json"))).entrySet()
    }

    def 'testRequiredFilesNotRequiredAnswer'() {
        given:
        TerrabyteClient terrabyteClient = Stub(TerrabyteClient) { it.getAllFilesByOrderId(_ as Long, _ as Long, _ as String) >> { getSnilsResponse() } }
        helper = new UploadFileComponent(userPersonalData, terrabyteClient)
        ComponentTestUtil.setAbstractComponentServices(helper)
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component-required.json"), FieldComponent.class)

        when:
        Map<String, String> errors = helper.validate(entry, scenarioDto, component)

        then:
        !errors.isEmpty()
        errors.get(TEST_COMPONENT_ID) == REQUIRED_FILES_WERE_NOT_FOUND

        where:
        entry << Map.of(TEST_COMPONENT_ID, new ApplicantAnswer(true, JsonFileUtil.getJsonFromFile(this.getClass(), "-answer-notRequired.json"))).entrySet()
    }

    def 'testRequiredFilesRequiredAndNotRequiredAnswer'() {
        given:
        TerrabyteClient terrabyteClient = Stub(TerrabyteClient) { it.getAllFilesByOrderId(_ as Long, _ as Long, _ as String) >> { getPassportAndSnilsResponse() } }
        helper = new UploadFileComponent(userPersonalData, terrabyteClient)
        ComponentTestUtil.setAbstractComponentServices(helper)
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component-required.json"), FieldComponent.class)

        when:
        Map<String, String> errors = helper.validate(entry, scenarioDto, component)

        then:
        errors.isEmpty()

        where:
        entry << Map.of(TEST_COMPONENT_ID, new ApplicantAnswer(true, JsonFileUtil.getJsonFromFile(this.getClass(), "-answer-required-and-notRequired.json"))).entrySet()
    }

    private static def getPassportResponse() {
        [[objectId: 100, objectTypeId: 2, mnemonic: "fu1.FileUploadComponent.passport.0", fileName: "passport.pdf", fileUid: 1889442961, fileExt: "pdf", fileSize: 100,] as FileInfo,
         [objectId: 100, objectTypeId: 2, mnemonic: "fu1.FileUploadComponent.passport.1", fileName: "passport.pdf", fileUid: 1889442961, fileExt: "pdf", fileSize: 100,] as FileInfo,
         [objectId: 100, objectTypeId: 2, mnemonic: "fu1.FileUploadComponent.passport.2", fileName: "passport.pdf", fileUid: 1889442961, fileExt: "pdf", fileSize: 100,] as FileInfo,
         [objectId: 100, objectTypeId: 4, mnemonic: "fu1.FileUploadComponent.signature.0", fileName: "passport.sig", fileUid: 1889442961, fileExt: "sig", fileSize: 100] as FileInfo] as List<FileInfo>
    }

    private static def getSnilsResponse() {
        [[objectId: 100, objectTypeId: 2, mnemonic: "fu1.FileUploadComponent.snils.0", fileName: "snils.pdf", fileUid: 1889442961, fileExt: "pdf", fileSize: 100] as FileInfo,
         [objectId: 100, objectTypeId: 2, mnemonic: "fu1.FileUploadComponent.snils.1", fileName: "snils.pdf", fileUid: 1889442961, fileExt: "pdf", fileSize: 100] as FileInfo,
         [objectId: 100, objectTypeId: 2, mnemonic: "fu1.FileUploadComponent.snils.2", fileName: "snils.pdf", fileUid: 1889442961, fileExt: "pdf", fileSize: 100] as FileInfo,
         [objectId: 100, objectTypeId: 4, mnemonic: "fu1.FileUploadComponent.signature.0", fileName: "snils.sig", fileUid: 1889442961, fileExt: "sig", fileSize: 100] as FileInfo] as List<FileInfo>
    }

    private static def getPassportAndSnilsResponse() {
        getPassportResponse() + getSnilsResponse()
    }
}
