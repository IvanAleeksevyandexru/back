package ru.gosuslugi.pgu.fs.transformation

import ru.gosuslugi.pgu.core.lk.model.order.Order
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.draft.model.DraftHolderDto
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.transformation.service.impl.TransformationFallbackToScreen
import spock.lang.Shared
import spock.lang.Specification

class TransformationFallbackToScreenSpec extends Specification {

    @Shared
    DraftHolderDto origin

    @Shared
    DraftHolderDto expected

    @Shared
    MainDescriptorService mainDescriptorService

    @Shared
    TransformationFallbackToScreen transformation

    def setupSpec() {
        origin = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "_origin.json"), DraftHolderDto)
        expected = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "_expected.json"), DraftHolderDto)
        mainDescriptorService = Mock(MainDescriptorService) {
            getServiceDescriptor(_ as String) >> Mock(ServiceDescriptor) {
                getScreenDescriptorById(_ as String) >> Optional.empty()
            }
        }
        transformation = new TransformationFallbackToScreen(
                new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper()),
                mainDescriptorService
        )
    }

    def "test transformation"() {
        given:
        Map<String, Object> map = new HashMap<>()
        map.put("screen", "SCRowner_approv_YN")

        when:
        TransformationResult result = transformation.transform(origin, new Order(), (Map<String, Object>) spec)

        then:
        result.isTransformed() == transformed
        JsonProcessingUtil.toJson(expect) == JsonProcessingUtil.toJson(result.getDraftHolderDto())

        where:
        spec                                       | expect   | transformed
        Map.of("screen", "SCRowner_approv_YN")     | expected | true
        // no screen
        Map.of()                                   | origin   | false
        // screen is absent
        Map.of("screen", "Bad_SCRowner_approv_YN") | origin   | false

    }
}
