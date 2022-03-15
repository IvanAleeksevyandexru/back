package ru.gosuslugi.pgu.fs.common.component.validation.text;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogic;
import ru.gosuslugi.pgu.fs.common.utils.SnilsUtil;

import java.util.Map;

public class SnilsValidationRule extends TextValidationRule {

    public static final String TYPE = "snils";

    @Override
    public String getValidationType() {
        return TYPE;
    }

    @Override
    public String getDefaultErrorMessage() {
        return "Неверное значение СНИЛС";
    }

    @Override
    protected boolean isValid(Map<Object, Object> params, String value, String key, ScenarioDto scenario) {
        return SnilsUtil.isMatches(value) && (
            JsonLogic.isTrue(params.get("skipChecksum")) || SnilsUtil.checkSumValidation(value)
        );
    }

}
