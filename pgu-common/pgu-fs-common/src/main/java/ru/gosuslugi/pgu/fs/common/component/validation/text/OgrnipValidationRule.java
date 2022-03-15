package ru.gosuslugi.pgu.fs.common.component.validation.text;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.utils.OgrnUtil;

import java.util.Map;

public class OgrnipValidationRule extends TextValidationRule {
    @Override
    public String getValidationType() {
        return "ogrnip";
    }

    @Override
    public String getDefaultErrorMessage() {
        return "Некорректный ОГРНИП";
    }

    @Override
    protected boolean isValid(Map<Object, Object> params, String value, String key, ScenarioDto scenario) {
        return OgrnUtil.checkOgrn(true, value);
    }
}
