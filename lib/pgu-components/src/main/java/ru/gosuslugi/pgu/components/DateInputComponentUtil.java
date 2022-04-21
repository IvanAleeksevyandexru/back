package ru.gosuslugi.pgu.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.date.model.Accuracy;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.VALIDATION_ARRAY_KEY;
import static ru.gosuslugi.pgu.components.RegExpUtil.REG_EXP_ERROR_MESSAGE;
import static ru.gosuslugi.pgu.components.ValidationUtil.VALIDATION_TYPE;

@Slf4j
public class DateInputComponentUtil {
    private final static String EUROPEAN_DATE_PATTERN = "dd.MM.yyyy";
    private final static DateTimeFormatter EUROPEAN_DATE_FORMATTER = DateTimeFormatter.ofPattern(EUROPEAN_DATE_PATTERN);
    private final static String ISO_DATE_PATTERN = "yyyy-MM-dd";
    private final static DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_PATTERN);

    public final static String EQUALS_VALIDATION_TYPE = "equalsDate";
    public final static String MIN_VALIDATION_TYPE = "minDate";
    public final static String MAX_VALIDATION_TYPE = "maxDate";
    public final static String VALUE_REF = "ref";
    public final static String DATE_VALUE = "value";
    public final static String VALIDATION_ERROR_MSG = "Значение не прошло валидацию %s";

    public final static Map<String, DateTimeFormatter> FORMATTER_MAP = Map.of(
            EUROPEAN_DATE_PATTERN, EUROPEAN_DATE_FORMATTER,
            ISO_DATE_PATTERN, ISO_DATE_FORMATTER
    );
    public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public final static DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("MM.dd.yyyy");

    public final static String ACCURACY_ATTR = "accuracy";
    private final static Map<String, BiPredicate<OffsetDateTime, OffsetDateTime>> VALIDATION_PREDICATES = new HashMap<>();


    static {
        VALIDATION_PREDICATES.put(EQUALS_VALIDATION_TYPE, OffsetDateTime::isEqual);
        VALIDATION_PREDICATES.put(MIN_VALIDATION_TYPE, (applicantValue, validationValue) ->
                applicantValue.isAfter(validationValue) || applicantValue.isEqual(validationValue));
        VALIDATION_PREDICATES.put(MAX_VALIDATION_TYPE, (applicantValue, validationValue) ->
                applicantValue.isBefore(validationValue) || applicantValue.isEqual(validationValue));
    }


    public static List<Map<String, String>> getValidationDateList(FieldComponent fieldComponent) {
        List<Map<String, String>> validationDateList = FieldComponentUtil.getStringList(fieldComponent, FieldComponentUtil.VALIDATION_ARRAY_KEY, true)
                .stream()
                .filter(
                        validationRule ->
                                EQUALS_VALIDATION_TYPE.equalsIgnoreCase(validationRule.get(VALIDATION_TYPE))
                                        || MIN_VALIDATION_TYPE.equalsIgnoreCase(validationRule.get(VALIDATION_TYPE))
                                        || MAX_VALIDATION_TYPE.equalsIgnoreCase(validationRule.get(VALIDATION_TYPE))
                )
                .collect(Collectors.toList());

        List<Map<String, String>> equalsValidation = validationDateList.stream()
                .filter(m -> EQUALS_VALIDATION_TYPE.equalsIgnoreCase(m.get(VALIDATION_TYPE)))
                .collect(Collectors.toList());

        if (!equalsValidation.isEmpty()) {
            validationDateList = equalsValidation;
        }
        return validationDateList;
    }

    public static List<String> validate(List<Map<String, String>> validationDateList,
                                        FieldComponent fieldComponent,
                                        String value) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isEmpty(value)) {
            return result;
        }

        OffsetDateTime parsedDate;
        try {
            parsedDate = DateUtil.parseDate(value, getAccuracy(fieldComponent));
        } catch (DateTimeParseException e) {
            result.add("DateInput field has incorrect format according to accuracy");
            return result;
        }

        validationDateList.forEach(validation ->
                result.addAll(validateWithPredicate(
                        validation,
                        fieldComponent,
                        parsedDate,
                        VALIDATION_PREDICATES.get(validation.get(VALIDATION_TYPE)),
                        validation.get(VALIDATION_TYPE))));

        return result;
    }

    private static String getAccuracy(FieldComponent fieldComponent) {
        String accuracy = (String) fieldComponent.getAttrs().get(ACCURACY_ATTR);
        if (!StringUtils.hasText(accuracy)) {
            accuracy = Accuracy.DAY.getName();
        }
        return accuracy;
    }

    private static List<String> validateWithPredicate(Map<String, String> validation,
                                                      FieldComponent fieldComponent,
                                                      OffsetDateTime value,
                                                      BiPredicate<OffsetDateTime, OffsetDateTime> predicate,
                                                      String validationType) {
        List<String> result = new ArrayList<>();
        OffsetDateTime dateFromValidation = getDateFromValidation(validation, fieldComponent);
        if(!predicate.test(value, dateFromValidation)) {
            result.add(validation.getOrDefault(REG_EXP_ERROR_MESSAGE, String.format((VALIDATION_ERROR_MSG), validationType)));
        }
        return result;
    }

    public static OffsetDateTime getDateFromValidation(Map<String, String> validation, FieldComponent fieldComponent) {
        String valueFromValidation = validation.get(DATE_VALUE);
        valueFromValidation = DateUtil.checkIfDateIsToday(valueFromValidation);
        OffsetDateTime dateFromValidation = DateUtil.parseDate(valueFromValidation, getAccuracy(fieldComponent));
        Optional<String> addPartOptional = Optional.ofNullable(validation.get("add"));
        if (addPartOptional.isPresent()) {
            dateFromValidation = DateUtil.addToDate(addPartOptional.get(), dateFromValidation);
        }
        dateFromValidation = DateUtil.parseDate(dateFromValidation.toString(), getAccuracy(fieldComponent));
        return dateFromValidation;
    }

    /**
     * Парсит заданную строку-дату с помощью указанных в sourceExpectedFormatters паттернов.
     * Если парсинг с помощью какого-то форматтера был успешен, то происходит преобразование даты в новый формат, заданный в targetDateTimeFormatter
     * @param dt дата в строке
     * @param sourceExpectedFormatters мапка форматтеров, в ключе паттерн с форматом
     * @param targetDateTimeFormatter целевой форматтер, в котором нужно вернуть дату
     * @return Возвращает дату в новом формате, либо пустую строку, если это не удалось.
     */
    public static String formatDate(String dt, Map<String, DateTimeFormatter> sourceExpectedFormatters, DateTimeFormatter targetDateTimeFormatter) {
        if (isEmpty(dt)) {
            return dt;
        }
        String resultDate = sourceExpectedFormatters.entrySet().stream().
                map(sourceFormatter -> parseDate(dt, sourceFormatter.getKey(), sourceFormatter.getValue()))
                .filter(Objects::nonNull).findFirst()
                .map(targetDateTimeFormatter::format)
                .orElseGet(() -> {
                    log.info(String.format("Дата %s не распарсилась ни в один заданный формат", dt));
                    return "";
                });
        return resultDate;
    }

    /**
     * Парсит дату с помощью заданного форматтера
     * @param dt дата в строке
     * @param formatterString паттерн-строка ожидаемого формата даты
     * @param sourceExpectedFormatter форматтер, заданный с помощью форматтера formatterString
     * @return Возвращает экземпляр #LocalDate или null в случае неуспеха
     */
    private static LocalDate parseDate(String dt, String formatterString, DateTimeFormatter sourceExpectedFormatter) {
        try {
            return LocalDate.parse(dt, sourceExpectedFormatter);
        } catch (DateTimeParseException e) {
            log.info(String.format("Дата %s не распарсилась в формат %s", dt, sourceExpectedFormatter, formatterString));
        }
        return null;
    }

    public static void setReferenceValue(ScenarioDto scenarioDto, FieldComponent fieldComponent) {
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

    public static void setMinMaxDates(FieldComponent component) {
        List<Map<String, String>> validationList = DateInputComponentUtil.getValidationDateList(component);
        validationList.forEach(validation -> {
            OffsetDateTime dateFromValidation = DateInputComponentUtil.getDateFromValidation(validation, component);
            component.getAttrs().putIfAbsent(validation.get(VALIDATION_TYPE), dateTimeFormatter.format(dateFromValidation));
        });
    }

    public static String convertDate(String dateString) {
        return targetFormatter.format(LocalDate.parse(dateString, dateTimeFormatter));
    }
}
