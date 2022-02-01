package ru.gosuslugi.pgu.fs.component.payment;

import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.CheckBoxEnabledValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.EmptyOr;
import ru.gosuslugi.pgu.fs.common.component.validation.RegExpValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;
import static ru.gosuslugi.pgu.components.descriptor.attr_factory.AttrsFactory.REFS_ATTR;

/**
 * Компонент "Расчетный счет"
 */
@Component
@RequiredArgsConstructor
public class CheckingAccountComponent extends AbstractComponent<String> {

    private final static String BIK_REF = "bik";
    private final static String BIK_DICT_REF = "bik_dict";
    private final List<Integer> WEIGHT_COEFFICIENTS = List.of(7, 1, 3);

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component) {
        return ComponentResponse.of(component.getValue());
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String bik = getBikValue(scenarioDto, fieldComponent);
        String checkingAccount = AnswerUtil.getValue(entry);
        if (!isValid(bik, checkingAccount)) {
            String NOT_VALID_ACCOUNT_NUMBER_ERROR = "Введите реквизиты вручную, посмотрев БИК и корреспондентский счет в деталях счета в интернет-банке";
            incorrectAnswers.put(entry.getKey(), NOT_VALID_ACCOUNT_NUMBER_ERROR);
        }
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new CheckBoxEnabledValidation(new RequiredNotBlankValidation("Значение не задано")),
                new CheckBoxEnabledValidation(new EmptyOr(new RegExpValidation()))
        );
    }

    @Override
    public ComponentType getType() {
        return ComponentType.CheckingAccount;
    }

    private String getBikValue(ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        Map<String, String> map = (Map<String, String>) fieldComponent.getAttrs().get(REFS_ATTR);
        if (map.containsKey(BIK_REF)
                && scenarioDto.getCurrentValue().containsKey(map.get(BIK_REF))
                && !StringUtils.isEmpty(scenarioDto.getCurrentValue().get(map.get(BIK_REF)).getValue())) {

            return scenarioDto.getCurrentValue().get(map.get(BIK_REF)).getValue();
        }

        if (map.containsKey(BIK_DICT_REF)
                && scenarioDto.getCurrentValue().containsKey(map.get(BIK_DICT_REF))
                && !StringUtils.isEmpty(scenarioDto.getCurrentValue().get(map.get(BIK_DICT_REF)).getValue())) {

            try {
                JSONObject valueJson = new JSONObject(scenarioDto.getCurrentValue().get(map.get(BIK_DICT_REF)).getValue());
                JSONObject value = valueJson.getJSONObject(ORIGINAL_ITEM);
                return (String) value.get("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Валидация номера расчетного счета
     *
     * @return Признак валидности
     */
    public boolean isValid(String bik, String checkingAccount) {
        if (StringUtils.isEmpty(bik) || bik.length() < 9 || StringUtils.isEmpty(checkingAccount) || checkingAccount.length() != 20) {
            return false;
        }

        // Условный номер кредитной организации (7 - 9 разряды БИК) + Номер лицевого счета
        List<Integer> items = bik.substring(6, 9).concat(checkingAccount).chars().map(Character::getNumericValue).boxed().collect(Collectors.toList());
        // Повторный расчет суммы младших разрядов произведений с учетом полученного значения контрольного ключа
        int sum = calculateLSB(items);

        return sum % 10 == 0;
    }

    /**
     * Сумма младших разрядов
     *
     * @param items Условный номер кредитной организации + Номер лицевого счета
     * @return Сумма младших разрядов
     */
    private int calculateLSB(List<Integer> items) {
        return IntStream.range(0, items.size())
                .mapToObj(index -> WEIGHT_COEFFICIENTS.get(index % 3) * items.get(index) % 10)
                .reduce(0, Integer::sum);
    }
}
