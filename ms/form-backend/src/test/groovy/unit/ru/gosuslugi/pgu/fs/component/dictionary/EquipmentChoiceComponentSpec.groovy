package unit.ru.gosuslugi.pgu.fs.component.dictionary

import com.fasterxml.jackson.core.type.TypeReference
import ru.atc.idecs.refregistry.ws.ListRefItemsRequest
import ru.atc.idecs.refregistry.ws.ListRefItemsResponse
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.service.impl.ComputeDictionaryItemService
import ru.gosuslugi.pgu.fs.component.dictionary.EquipmentChoiceComponent
import ru.gosuslugi.pgu.fs.component.dictionary.dto.equipment.EquipmentChoiceDto
import ru.gosuslugi.pgu.fs.component.dictionary.dto.equipment.EquipmentItem
import spock.lang.Specification

class EquipmentChoiceComponentSpec extends Specification {
    EquipmentChoiceComponent equipmentChoiceComponent

    def 'Check initial value Опт '() {
        given:
        ComputeDictionaryItemService dictionaryItemServiceMock = Mock(ComputeDictionaryItemService)
        ListRefItemsResponse concActivityKindResponse = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-CONC_ACTIVITY_KIND-response.json"), ListRefItemsResponse.class)
        ListRefItemsResponse concEquipmentCategoryResponse = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-CONC_EQUIPMENT_CATEGORY-response.json"), ListRefItemsResponse.class)
        ListRefItemsResponse concEquipmentNameResponse = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-CONC_EQUIPMENT_NAME-response.json"), ListRefItemsResponse.class)
        List<EquipmentItem> expectedList = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-lastResponse.json"),  new TypeReference<List<EquipmentItem>>() {})

        dictionaryItemServiceMock.getDictionaryItems(_ as ListRefItemsRequest, "CONC_ACTIVITY_KIND") >> concActivityKindResponse
        dictionaryItemServiceMock.getDictionaryItems(_ as ListRefItemsRequest, "CONC_EQUIPMENT_CATEGORY") >> concEquipmentCategoryResponse
        dictionaryItemServiceMock.getDictionaryItems(_ as ListRefItemsRequest, "CONC_EQUIPMENT_NAME") >> concEquipmentNameResponse
        equipmentChoiceComponent = new EquipmentChoiceComponent(dictionaryItemServiceMock)
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component.json"), FieldComponent.class)
        EquipmentChoiceDto expectedResult = new EquipmentChoiceDto(expectedList, null);

        when:
        equipmentChoiceComponent.preProcess(component)

        then:
        EquipmentChoiceDto actual = component.getAttrs().get("result") as EquipmentChoiceDto
        actual != null
        actual.items.size() == expectedResult.items.size()
        actual.getAttrs() == null

        for (i in 0..actual.items.size() - 1) {
            EquipmentItem actualItem = actual.items.get(i)
            EquipmentItem expectedItem = expectedResult.items.get(i)
            actualItem.getAttributeValues().size() == expectedItem.getAttributeValues().size()
            actualItem.getAttributeValues().get("CATEGORY_NAME") == expectedItem.getAttributeValues().get("CATEGORY_NAME")
            actualItem.getAttributeValues().get("MIN_AMOUNT_EQUIPMENT") == expectedItem.getAttributeValues().get("MIN_AMOUNT_EQUIPMENT")
        }
    }

    def 'Check initial value Розница'() {
        given:
        ComputeDictionaryItemService dictionaryItemServiceMock = Mock(ComputeDictionaryItemService)
        ListRefItemsResponse concActivityKindResponse = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-CONC_ACTIVITY_KIND-response.json"), ListRefItemsResponse.class)
        ListRefItemsResponse concServiceTypeResponse = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-CONC_SERVICE_TYPE-response.json"), ListRefItemsResponse.class)
        ListRefItemsResponse concEquipmentCategoryResponse = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-CONC_EQUIPMENT_CATEGORY-response.json"), ListRefItemsResponse.class)
        ListRefItemsResponse concEquipmentNameResponse = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-CONC_EQUIPMENT_NAME-response.json"), ListRefItemsResponse.class)
        List<EquipmentItem> expectedList = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-lastResponse.json"),  new TypeReference<List<EquipmentItem>>() {})
        Set<String> expectedSet = new LinkedHashSet<>(["14688", "14687", "14685", "14684", "14686", "14683", "14682"])
        Map<String, Object> additionalAttrs = Map.of("CONC_SERVICE_TYPE_IDS", expectedSet.toList())
        EquipmentChoiceDto expectedResult = new EquipmentChoiceDto(expectedList, additionalAttrs);

        dictionaryItemServiceMock.getDictionaryItems(_ as ListRefItemsRequest, "CONC_ACTIVITY_KIND") >> concActivityKindResponse
        dictionaryItemServiceMock.getDictionaryItems(_ as ListRefItemsRequest, "CONC_SERVICE_TYPE") >> concServiceTypeResponse
        dictionaryItemServiceMock.getDictionaryItems(_ as ListRefItemsRequest, "CONC_EQUIPMENT_CATEGORY") >> concEquipmentCategoryResponse
        dictionaryItemServiceMock.getDictionaryItems(_ as ListRefItemsRequest, "CONC_EQUIPMENT_NAME") >> concEquipmentNameResponse
        equipmentChoiceComponent = new EquipmentChoiceComponent(dictionaryItemServiceMock)
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component-розница.json"), FieldComponent.class)

        when:
        equipmentChoiceComponent.preProcess(component)

        then:
        EquipmentChoiceDto actual = component.getAttrs().get("result") as EquipmentChoiceDto
        actual != null
        actual.items.size() == expectedResult.items.size()
        actual.getAttrs() != null
        actual.getAttrs().size() == 1
        Set<String> ids = new LinkedHashSet<>((Set<String>) actual.getAttrs().get("CONC_SERVICE_TYPE_IDS"))
        ids != null
        ids.size() == expectedSet.size()
        ids.removeAll(expectedSet);
        ids.isEmpty()

        for (i in 0..actual.items.size() - 1) {
            EquipmentItem actualItem = actual.items.get(i)
            EquipmentItem expectedItem = expectedResult.items.get(i)
            actualItem.getAttributeValues().size() == expectedItem.getAttributeValues().size()
            actualItem.getAttributeValues().get("CATEGORY_NAME") == expectedItem.getAttributeValues().get("CATEGORY_NAME")
            actualItem.getAttributeValues().get("MIN_AMOUNT_EQUIPMENT") == expectedItem.getAttributeValues().get("MIN_AMOUNT_EQUIPMENT")
        }
    }
}
