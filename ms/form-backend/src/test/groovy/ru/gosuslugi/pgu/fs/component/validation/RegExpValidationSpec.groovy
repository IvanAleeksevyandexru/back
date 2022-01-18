package ru.gosuslugi.pgu.fs.component.validation

import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.fs.common.component.validation.RegExpValidation
import spock.lang.Specification

import static ru.gosuslugi.pgu.fs.component.confirm.ConfirmPersonalUserEmailComponent.DEFAULT_EMAIL_MASK

class RegExpValidationSpec extends Specification {

    def "Validate regex values"() {
        given:
        def validator = new RegExpValidation(DEFAULT_EMAIL_MASK, "errorMessage")

        expect:
        assert validator.validate(answerEntry("key", "test@test.ru"), null) == null
        assert validator.validate(answerEntry("key", "test"), null) == entry("key", "errorMessage")
        assert validator.validate(answerEntry("key", null), null) == entry("key", "errorMessage")
    }

    static Map.Entry<String, String> entry(String key, String value) {
        return new AbstractMap.SimpleEntry(key, value)
    }

    static Map.Entry<String, ApplicantAnswer> answerEntry(String key, String value) {
        return new AbstractMap.SimpleEntry(key, new ApplicantAnswer(value: value))
    }
}
