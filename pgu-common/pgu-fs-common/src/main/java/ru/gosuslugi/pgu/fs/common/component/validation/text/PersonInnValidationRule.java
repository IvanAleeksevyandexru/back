package ru.gosuslugi.pgu.fs.common.component.validation.text;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.utils.InnUtil;

import java.util.Map;

public class PersonInnValidationRule extends TextValidationRule {

    @Override
    public String getValidationType() {
        return "personInn";
    }

    @Override
    public String getDefaultErrorMessage() {
        return "Некорректный ИНН физического лица";
    }

    @Override
    protected boolean isValid(Map<Object, Object> params, String value, String key, ScenarioDto scenario) {
        return InnUtil.personInnValidation(value);
    }
}
