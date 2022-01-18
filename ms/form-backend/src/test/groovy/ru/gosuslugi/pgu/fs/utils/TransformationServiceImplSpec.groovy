package ru.gosuslugi.pgu.fs.utils

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.draft.model.DraftHolderDto
import ru.gosuslugi.pgu.dto.StatusInfo
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationBlock
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOperation
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOptions
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationRule
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.transformation.service.impl.TransformationFallbackToScreen
import ru.gosuslugi.pgu.fs.transformation.service.impl.TransformationRegistry
import ru.gosuslugi.pgu.fs.transformation.service.impl.TransformationServiceImpl
import spock.lang.Shared
import spock.lang.Specification
import ru.gosuslugi.pgu.core.lk.model.order.Order

import java.text.SimpleDateFormat

class TransformationServiceImplSpec extends Specification {
    @Shared
    DraftHolderDto origin
    @Shared
    DraftHolderDto expected
    @Shared
    JsonProcessingService jsonProcessingService
    @Shared
    TransformationServiceImpl transformationService
    @Shared
    Order order

    @Shared
    TransformationRule firstRule
    @Shared
    TransformationRule secondRule

    def setupSpec() {
        MainDescriptorService mainDescriptorService = Mock(MainDescriptorService.class)
        ServiceDescriptor serviceDescriptor = Mock(ServiceDescriptor.class)
        serviceDescriptor.getScreenDescriptorById(_ as String) >> Optional.empty()
        mainDescriptorService.getServiceDescriptor(_ as String) >> serviceDescriptor
        order = new Order()
        origin = JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), "_origin.json"),
                DraftHolderDto.class)
        expected = JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), "_expected.json"),
                DraftHolderDto.class)
        jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
        def registry = new TransformationRegistry()
        registry.setTransformations(Arrays.asList(new TransformationFallbackToScreen(jsonProcessingService, mainDescriptorService)))
        transformationService = new TransformationServiceImpl(jsonProcessingService, registry)

        firstRule = new TransformationRule()
        firstRule.setOperation(TransformationOperation.fallbackToScreen)
        firstRule.setSpec(Map.of('screen', 'SCRowner_approv_YN'))

        secondRule = new TransformationRule()
        secondRule.setOperation(TransformationOperation.fallbackToScreen)
        secondRule.setSpec(Map.of('screen', 'BAD_SCRowner_approv_YN'))
    }

    def 'Test transformation'() {
        given:
        def statusInfo = new StatusInfo(2038701564L, 1L, new SimpleDateFormat("yyyy-MM-ddXXX").parse("2021-01-01+00:00"))
        when:
        def result = transformationService.transform(transformation, statusInfo, order, origin)

        then:
        result.isTransformed() == transformed
        result.getDraftHolderDto() == compareWith

        where:
        transformation                          | transformed | compareWith
        new TransformationBlock (null, new TransformationOptions(),List.of(firstRule)) | true | expected
        new TransformationBlock (null,new TransformationOptions(), List.of(firstRule, secondRule)) | false       | origin
    }
}
