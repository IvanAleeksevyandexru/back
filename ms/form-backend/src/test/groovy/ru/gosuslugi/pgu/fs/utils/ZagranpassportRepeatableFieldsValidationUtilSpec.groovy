package ru.gosuslugi.pgu.fs.utils

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.draft.model.DraftHolderDto
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import spock.lang.Specification

class ZagranpassportRepeatableFieldsValidationUtilSpec extends Specification {

    private FieldComponent fieldComponent = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "_fieldComponent.json"), FieldComponent.class)

    def 'validateEquals'() {
        given:
        ApplicantAnswer answer = new ApplicantAnswer(value: "[{\"rf51\":\"Каменев\",\"rf52\":\"Иван\",\"rf53\":\"Игоревич\",\"rf54\":\"С\",\"rf55\":\"2020-12-02T00:00:00.000+06:00\"},{\"rf51\":\"Каменев\",\"rf52\":\"Иван\",\"rf53\":\"Игоревич\",\"rf54\":\"С1\",\"rf55\":\"2020-12-16T00:00:00.000+06:00\"}]")
        Map.Entry<String, ApplicantAnswer> entry = new AbstractMap.SimpleEntry("ai17", answer)
        List<Map<String, String>> childrenAnswers = AnswerUtil.toStringMapList(entry, true)
        DraftHolderDto draftHolderDto = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "_draft.json"), DraftHolderDto.class)
        AnswerUtil.setCycleReferenceValue(draftHolderDto.getBody(), fieldComponent)

        when:
        Map.Entry<String, String> result = ZagranpassportRepeatableFieldsValidationUtil.childNameChangeAllFieldsEqual(entry.getKey(), childrenAnswers, fieldComponent)

        then:
        result != null
        result.getKey() == "ai17"
        result.getValue() == "Указанные Вами сведения о предыдущих персональных данных в точности совпадают с текущими персональными данными. Внесите, пожалуйста, информацию об измененных персональных данных."
    }

    def 'validateEqualsMiddleName'() {
        ApplicantAnswer answer = new ApplicantAnswer(value: "[{\"rf51\":\"Каменев\",\"rf52\":\"Иван\",\"rf53\":\"\",\"rf54\":\"С\",\"rf55\":\"2020-12-02T00:00:00.000+06:00\"},{\"rf51\":\"Каменев\",\"rf52\":\"Иван\",\"rf53\":\"Игоревич\",\"rf54\":\"С1\",\"rf55\":\"2020-12-16T00:00:00.000+06:00\"}]")
        Map.Entry<String, ApplicantAnswer> entry = new AbstractMap.SimpleEntry("ai17", answer)
        List<Map<String, String>> childrenAnswers = AnswerUtil.toStringMapList(entry, true)
        DraftHolderDto draftHolderDto = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "_draft_no_middlename.json"), DraftHolderDto.class)
        AnswerUtil.setCycleReferenceValue(draftHolderDto.getBody(), fieldComponent)

        when:
        Map.Entry<String, String> result = ZagranpassportRepeatableFieldsValidationUtil.childNameChangeAllFieldsEqual(entry.getKey(), childrenAnswers, fieldComponent)

        then:
        result != null
        result.getKey() == "ai17"
        result.getValue() == "Указанные Вами сведения о предыдущих персональных данных в точности совпадают с текущими персональными данными. Внесите, пожалуйста, информацию об измененных персональных данных."
    }

    def 'validateNotEquals'() {
        ApplicantAnswer answer = new ApplicantAnswer(value: "[{\"rf51\":\"Каменев\",\"rf52\":\"Иванушка\",\"rf53\":\"Игоревич\",\"rf54\":\"С\",\"rf55\":\"2020-12-02T00:00:00.000+06:00\"},{\"rf51\":\"Каменев\",\"rf52\":\"Иван\",\"rf53\":\"Игоревич\",\"rf54\":\"С1\",\"rf55\":\"2020-12-16T00:00:00.000+06:00\"}]")
        Map.Entry<String, ApplicantAnswer> entry = new AbstractMap.SimpleEntry("ai17", answer)
        List<Map<String, String>> childrenAnswers = AnswerUtil.toStringMapList(entry, true)
        DraftHolderDto draftHolderDto = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "_draft.json"), DraftHolderDto.class)
        AnswerUtil.setCycleReferenceValue(draftHolderDto.getBody(), fieldComponent)

        when:
        Map.Entry<String, String> result = ZagranpassportRepeatableFieldsValidationUtil.childNameChangeAllFieldsEqual(entry.getKey(), childrenAnswers, fieldComponent)

        then:
        result == null
    }

}
