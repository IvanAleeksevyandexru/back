package ru.gosuslugi.pgu.fs.common.service.impl

import org.springframework.util.CollectionUtils
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.service.ListComponentItemUniquenessService
import spock.lang.Specification

import java.util.stream.Collectors

class ListComponentItemUniquenessServiceImplSpec extends Specification {

    static ListComponentItemUniquenessService listComponentItemUniquenessService
    static FieldComponent childrenList
    static FieldComponent repeatableFields
    static List<CycledApplicantAnswerItem> cycledItemsAnswers
    static List<CycledApplicantAnswerItem> cycledItemsAnswersInCycle
    static List<Map<String, String>> repeatableItemsAnswers

    def setupSpec() {
        listComponentItemUniquenessService = new ListComponentItemUniquenessServiceImpl()
        childrenList = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-childrenList.json"), FieldComponent)
        repeatableFields = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-repeatableFields.json"), FieldComponent)
        cycledItemsAnswers = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-cycledItemsAnswers.json"), List)
                .stream()
                .map({ map -> JsonProcessingUtil.fromJson(JsonProcessingUtil.toJson(map), CycledApplicantAnswerItem) })
                .collect(Collectors.toList())
        cycledItemsAnswersInCycle = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-cycledItemsAnswers-inCycle.json"), List)
                .stream()
                .map({ map -> JsonProcessingUtil.fromJson(JsonProcessingUtil.toJson(map), CycledApplicantAnswerItem) })
                .collect(Collectors.toList())
        repeatableItemsAnswers = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-repeatableItemsAnswers.json"), List)
    }

    def "test cycled ChildrenList with failed items uniqueness"() {
        when:
        List<List<Map<String, String>>> uniquenessErrors = listComponentItemUniquenessService.validateCycledItemsUniqueness(
                childrenList, cycledItemsAnswers)
        then:
        !CollectionUtils.isEmpty(uniquenessErrors)
        uniquenessErrors.get(0).size() == 3
        uniquenessErrors.get(1).size() == 3
    }

    def "test cycled ChildrenList with failed items uniqueness inside cycle"() {
        when:
        List<List<Map<String, String>>> uniquenessErrors = listComponentItemUniquenessService.validateCycledItemUniqueness(
                childrenList, cycledItemsAnswersInCycle, 1)
        then:
        !CollectionUtils.isEmpty(uniquenessErrors)
        uniquenessErrors.get(0).size() == 1
        uniquenessErrors.get(0).get(0).get("bd1") == "Проверьте на совпадение со свидетельством о рождении другого ребенка"
        uniquenessErrors.get(0).get(0).get("bd2") == "Проверьте на совпадение со свидетельством о рождении другого ребенка"
    }

    def "test not cycled RepeatableFields with failed items uniqueness"() {
        when:
        List<List<Map<String, String>>> uniquenessErrors = listComponentItemUniquenessService.validateRepeatableFieldsItemsUniqueness(
                repeatableFields, repeatableItemsAnswers)
        then:
        !CollectionUtils.isEmpty(uniquenessErrors)
        uniquenessErrors.get(0).size() == 2
        uniquenessErrors.get(1).size() == 2
    }
}
