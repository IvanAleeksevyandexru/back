package ru.gosuslugi.pgu.fs.common.component.validation.text;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.utils.CardNumberUtil;

import java.util.Map;

public class CardNumberValidationRule extends TextValidationRule {

    @Override
    public String getValidationType() {
        return "cardNumber";
    }

    @Override
    public String getDefaultErrorMessage() {
        return "Такой карты не существует";
    }

    @Override
    protected boolean isValid(Map<Object, Object> params, String value, String key, ScenarioDto scenario) {
        return CardNumberUtil.checkLuhn(value);
    }
}
