package ru.gosuslugi.pgu.fs.common.component.validation.text;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogic;
import ru.gosuslugi.pgu.fs.common.utils.SnilsUtil;

import java.util.Map;

public class SnilsChildrenValidationRule extends TextValidationRule {

    @Override
    public String getValidationType() {
        return "snils";
    }

    @Override
    public String getDefaultErrorMessage() {
        return SnilsUtil.REPEATED_CHILDREN_SNILS_ERROR_MESSAGE;
    }

    @Override
    protected boolean isValid(Map<Object, Object> params, String value, String key, ScenarioDto scenario) {
        if (scenario == null) {
            return true;
        }
        return !JsonLogic.isTrue(params.get("checkRepeatedChildrenSnils")) || SnilsUtil.checkRepeatedChildrenSnils(key, value, scenario);
    }
}
