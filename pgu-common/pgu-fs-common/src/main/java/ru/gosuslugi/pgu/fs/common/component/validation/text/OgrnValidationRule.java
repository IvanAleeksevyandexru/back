package ru.gosuslugi.pgu.fs.common.component.validation.text;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.utils.OgrnUtil;

import java.util.Map;

public class OgrnValidationRule extends TextValidationRule {
    @Override
    public String getValidationType() {
        return "ogrn";
    }

    @Override
    public String getDefaultErrorMessage() {
        return "Некорректный ОГРН";
    }

    @Override
    protected boolean isValid(Map<Object, Object> params, String value, String key, ScenarioDto scenario) {
        return OgrnUtil.checkOgrn(false, value);
    }
}
