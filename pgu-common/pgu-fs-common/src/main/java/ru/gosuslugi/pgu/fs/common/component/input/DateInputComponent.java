package ru.gosuslugi.pgu.fs.common.component.input;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.components.DateInputComponentUtil;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.utils.ConditionalDisabledComponentUtil;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import static org.apache.commons.lang.StringUtils.isBlank;
import static ru.gosuslugi.pgu.components.DateInputComponentUtil.FORMATTER_MAP;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.VALIDATION_ARRAY_KEY;
import static ru.gosuslugi.pgu.components.ValidationUtil.VALIDATION_TYPE;

/**
 * Класс валидации DateInput полей. Условия для валидации вносятся в поле <b>condition</b>
 * Основные примеры для валидации следующие:
 * <ul>
 *     <li> <10d - введенная дата должна быть не больше от текущей или референсной  -10 дней</li>
 *     <li> >10m - введенная дата должна быть не меньше от текушей или референсной +10 минут</li>
 *     <li> >10m|<10d - введенная дата должна соответствовать нескольким условиям сразу</li>
 * </ul>
 * <p>
 * Также, если указывать ссылку на предыдущую дату в поле <b>ref</b> через идентификатор id, то введенная дата
 * будет сравниваться с указанной датой через ref, иначе будет сравниваться с текущей датой (Date.now())
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class DateInputComponent extends EsiaDataCycledComponent<String> {

    private final static String VALUE_REF = "ref";
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final static DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("MM.dd.yyyy");

    private final InitialValueFromService initialValueFromService;

    @Override
    public ComponentType getType() {
        return ComponentType.DateInput;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        String presetValue = initialValueFromService.getValue(component, scenarioDto);
        if (!isBlank(presetValue)) {
            String date = DateInputComponentUtil.formatDate(presetValue, FORMATTER_MAP, targetFormatter);
            return ComponentResponse.of(date);
        }
        return ComponentResponse.empty();
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        setReferenceValue(scenarioDto, component);
        setMinMaxDates(component);
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if (!ConditionalDisabledComponentUtil.isCheckBoxValidationDisabled(scenarioDto, fieldComponent)) {
            String name = entry.getKey();
            String value = AnswerUtil.getValue(entry);

            DateInputComponentUtil.setReferenceValue(scenarioDto, fieldComponent);

            Arrays.<Supplier<Map.Entry<String, String>>>asList(
                    () -> ValidationUtil.validateRequiredNotBlank(name, value, fieldComponent, "Значение не задано"),
                    () -> validateDate(name, value, fieldComponent)
            ).forEach(
                    supplier -> {

                        // Если ошибок еще нет, делаем очередную проверку и добавляем ошибку при ненулевом результате
                        if (!incorrectAnswers.containsKey(name)) {
                            Optional.ofNullable(supplier.get()).ifPresent(pair -> incorrectAnswers.put(pair.getKey(), pair.getValue()));
                        }
                    }
            );
        }
    }

    private void setReferenceValue(ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        Optional.ofNullable((List<Map<String, String>>) fieldComponent.getAttrs().get(VALIDATION_ARRAY_KEY))
                .orElse(Collections.emptyList()).forEach(v -> {
                    String reference = v.get(VALUE_REF);
                    if (StringUtils.hasText(reference)) {
                        Optional<ApplicantAnswer> answer = Optional.ofNullable(scenarioDto.getApplicantAnswers().get(reference));
                        answer.ifPresent(a -> v.put("value", a.getValue()));
                        answer = Optional.ofNullable(scenarioDto.getCurrentValue().get(reference));
                        answer.ifPresent(a -> v.put("value", a.getValue()));
                    }
                });
    }

    private void setMinMaxDates(FieldComponent component) {
        List<Map<String, String>> validationList = DateInputComponentUtil.getValidationDateList(component);
        validationList.forEach(validation -> {
            OffsetDateTime dateFromValidation = DateInputComponentUtil.getDateFromValidation(validation, component);
            component.getAttrs().putIfAbsent(validation.get(VALIDATION_TYPE), dateTimeFormatter.format(dateFromValidation));
        });
    }

    private static Map.Entry<String, String> validateDate(String name, String value, FieldComponent fieldComponent) {
        Map.Entry<String, String> result = null;
        List<Map<String, String>> validationDateList = DateInputComponentUtil.getValidationDateList(fieldComponent);
        List<String> errors = DateInputComponentUtil.validate(validationDateList, fieldComponent, value);
        if (!errors.isEmpty()) {
            result = new AbstractMap.SimpleEntry<>(name, String.join(", ", errors));
        }
        return result;
    }

    private String convertDate(String dateString) {
        return targetFormatter.format(LocalDate.parse(dateString, dateTimeFormatter));
    }
}
