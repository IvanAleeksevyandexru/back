package unit.ru.gosuslugi.pgu.fs.pgu.fs


import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.fs.component.file.TerrabyteFileProcessor
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo
import spock.lang.Specification

class TerrabyteFileProcessorSpec extends Specification {

    private TerrabyteFileProcessor terrabyteFileProcessor
    private TerrabyteClient client
    private UserPersonalData userPersonalData
    private byte[] bytes = [0, 0, 0, 0, 0] as byte[]

    def setup() {
        userPersonalData = Stub(UserPersonalData) { it.getUserId() >> { 1L }; it.getToken() >> { "token" } }
        client = Mock(TerrabyteClient)
        terrabyteFileProcessor = TerrabyteFileProcessor.of(client, userPersonalData)
    }

    def "Should process file"() {
        given:
        def fileInfo = getFileInfo()
        def zipName = fileInfo.getFileName().substring(0, fileInfo.getFileName().lastIndexOf("." + fileInfo.getFileExt())) + ".zip"
        def zipMimeType = "application/zip"

        when:
        terrabyteFileProcessor.replaceByZipped(fileInfo)

        then:
        1 * client.getFile(fileInfo, userPersonalData.getUserId(), userPersonalData.getToken()) >> { bytes }
        1 * client.deleteFile(fileInfo, userPersonalData.getUserId(), userPersonalData.getToken())
        1 * client.internalSaveFile(_ as byte[], zipName, fileInfo.getMnemonic(), zipMimeType, fileInfo.getObjectId(), fileInfo.getObjectTypeId())
    }

    static def getFileInfo() {
        [objectId: 764184916, objectTypeId: 1, mnemonic: "fu1.FileUploadComponent.passport.0", fileName: "test.sig", fileUid: 1889442959, fileExt: "sig", fileSize: 100] as FileInfo
    }
}
