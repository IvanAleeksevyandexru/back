package ru.gosuslugi.pgu.fs.component.file

import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.service.TerrabyteService
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo
import spock.lang.Specification

class PhotoEditorComponentTest extends Specification {

    def "test update File Parameters"() {
        given:
        TerrabyteService terrabyteService = Mock()
        PhotoEditorComponent component = new PhotoEditorComponent(terrabyteService)
        ComponentTestUtil.setAbstractComponentServices(component)
        FieldComponent fieldComponent = new FieldComponent(attrs: ["uploads": [[:]]])
        ScenarioDto scenarioDto = new ScenarioDto(display: new DisplayRequest(components: [null]))

        when:
        component.process(fieldComponent, scenarioDto, new ServiceDescriptor());

        then:
        1 * terrabyteService.getFileInfoFromUserAnswer(_ as Map, _, _) >> new FileInfo(mnemonic: 'TST', objectId: 1, objectTypeId: 2, fileName: 'filename')
        assert fieldComponent.getAttrs().get("uploads") == [['name': 'filename', 'objectId': '1', 'objectType': '2', 'mnemonic': 'TST']]
    }

}
