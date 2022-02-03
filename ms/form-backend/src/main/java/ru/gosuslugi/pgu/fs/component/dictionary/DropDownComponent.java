package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.common.core.date.model.Accuracy;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService;
import ru.gosuslugi.pgu.fs.utils.ContextJsonUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.DICTIONARY_LIST_KEY;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.REF_KEY;
import static ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService.DICTIONARY_REF_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class DropDownComponent extends AbstractComponent<String> {

    protected final UserPersonalData userPersonalData;

    public static final String YEAR_GEN = "year";
    public static final String YEAR_FIRST_KEY = "first";
    public static final String YEAR_FIRST_ADD_KEY = "add";
    public static final String YEAR_FIRST_VALUE_KEY = "value";
    public static final String YEAR_GEN_KEY = "gen";
    public static final String YEAR_DEFAULT_EMPTY_KEY = "defaultEmpty";
    public static final String YEAR_SORT_KEY = "sort";
    public static final String YEAR_SORT_ASC = "asc";
    public static final String YEAR_SORT_DESC = "desc";
    public static final String LABEL_KEY = "label";
    public static final String CURRENT_YEAR_VALUE = "Current";
    public static final String DAY_VALUE = "day";
    public static final String MONTH_VALUE = "month";
    public static final String OFFSET_VALUE = "offset";
    public static final String DEFAULT_VALUE_ATTR = "defaultValue";
    public static final String DEFAULT_VALUE_ID_KEY = "id";
    public static final String DEFAULT_VALUE_TEXT_KEY = "text";
    public static final String GENDER_KEY = "gender";
    public static final String MAN_GENDER = "M";
    public static final String MAN_GENDER_ATTR_VALUE = "man";
    public static final String WOMAN_GENDER_ATTR_VALUE = "woman";

    private final DictionaryListPreprocessorService dictionaryListPreprocessorService;

    @Override
    public ComponentType getType() {
        return ComponentType.DropDown;
    }

    @Override
    public void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        // обычные справочники, с содержанием dictionaryList
        if (component.getAttrs() != null && component.getAttrs().containsKey(DICTIONARY_LIST_KEY)) {
            if (userPersonalData.getPerson() != null && userPersonalData.getPerson().getGender() != null) {
                List<Map<String, String>> dictionaryList = (List<Map<String, String>>) component.getAttrs().get(DICTIONARY_LIST_KEY);
                String gender = userPersonalData.getPerson().getGender().equalsIgnoreCase(MAN_GENDER) ? MAN_GENDER_ATTR_VALUE : WOMAN_GENDER_ATTR_VALUE;
                dictionaryList = dictionaryList.stream()
                        .filter(it -> !it.containsKey(GENDER_KEY) || it.get(GENDER_KEY).equals(gender))
                        .collect(Collectors.toList());
                component.getAttrs().put(DICTIONARY_LIST_KEY, dictionaryList);
            }
            return;
        }

        //данные приходят из предыдущего компонента
        if(component.getArguments().containsKey(DICTIONARY_REF_KEY)) {
            dictionaryListPreprocessorService.prepareDictionaryListFromComponent(component, scenarioDto);
        }

        // справочники типа "year" - список с генерацией дат по заданным атрибутам
        setReferenceValue(scenarioDto, component);

        if (!CollectionUtils.isEmpty(FieldComponentUtil.getAttrMap(component, YEAR_GEN, true))) {
            addYearsToFieldComponent(component);
        }
    }


    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new RequiredNotBlankValidation("Значение не задано")
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String name = entry.getKey();
        String entryValue = AnswerUtil.getValue(entry);

        if (StringUtils.isEmpty(entryValue)) {
            return;
        }
        Map.Entry<String, String> attributeEntry = ContextJsonUtil.getAttributeInDepth(jsonProcessingService, entry, ORIGINAL_ITEM, LABEL_KEY);
        String labelValue = attributeEntry.getValue();
        validateLabel(name, labelValue, incorrectAnswers, fieldComponent);
    }

    protected void validateLabel(String name, String labelValue, Map<String, String> incorrectAnswers, FieldComponent fieldComponent) {
        Arrays.<Supplier<Map.Entry<String, String>>>asList(
                () -> ValidationUtil.validateRegExp(name, labelValue, fieldComponent),
                () -> ValidationUtil.validateMemberValueOfList(name, labelValue, fieldComponent, "Значение не из списка")
        ).forEach(
                supplier -> {
                    // Если ошибок еще нет, делаем очередную проверку и добавляем ошибку при ненулевом результате
                    if (!incorrectAnswers.containsKey(name)) {
                        Optional.ofNullable(supplier.get()).ifPresent(pair -> incorrectAnswers.put(pair.getKey(), pair.getValue()));
                    }
                }
        );
    }

    /**
     * Возврат текущей даты.
     * Создан как protected-метод в целях переопределения в тестах, чтобы тест стал независимым от текущей даты.
     */
    protected LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    private void setReferenceValue(ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        Optional.ofNullable((Map<String, Object>) fieldComponent.getAttrs().get(YEAR_GEN))
                .ifPresent(map -> {
                    Map<String, Object> firstValueNode = (Map<String, Object>) map.get(YEAR_FIRST_KEY);
                    String reference = (String) firstValueNode.getOrDefault(REF_KEY, "");
                    if (StringUtils.isNotBlank(reference)) {
                        Optional<ApplicantAnswer> answer = Optional.ofNullable(scenarioDto.getApplicantAnswers().get(reference));
                        answer.ifPresent(a -> firstValueNode.put("value", extractYear(a.getValue())));
                        answer = Optional.ofNullable(scenarioDto.getCurrentValue().get(reference));
                        answer.ifPresent(a -> firstValueNode.put("value", extractYear(a.getValue())));
                    }
                });
    }

    private static String extractYear(String value) {
        Object valueObject = AnswerUtil.tryParseToMap(value);
        // Ссылка может быть на DropDown
        if (valueObject instanceof Map) {
            Map<String, Object> valueMap = (Map<String, Object>) valueObject;
            Object o = ((Map<String, Object>) valueMap.getOrDefault(ORIGINAL_ITEM, Collections.emptyMap())).get("label");
            if (o != null) {
                return String.valueOf(o);
            }
        }
        // Ссылка может быть на DateInput
        if (valueObject instanceof String) {
            return DateUtil.cutString(value, Accuracy.YEAR.getName());
        }
        return value;
    }

    private void addYearsToFieldComponent(FieldComponent component) {
        Map<Object, Object> yearAttrMap = FieldComponentUtil.getAttrMap(component, YEAR_GEN, true);
        @SuppressWarnings("unchecked")
        Map<Object, Object> firstValueMap = (Map<Object, Object>) yearAttrMap.get(YEAR_FIRST_KEY);
        int year;
        String valueString = (String) firstValueMap.get(YEAR_FIRST_VALUE_KEY);
        LocalDate now = getCurrentDate();
        year = valueString.equalsIgnoreCase(CURRENT_YEAR_VALUE) ? now.getYear() : Integer.parseInt((String) firstValueMap.get(YEAR_FIRST_VALUE_KEY));
        String dayStr = (String) firstValueMap.get(DAY_VALUE);
        String monthStr = (String) firstValueMap.get(MONTH_VALUE);
        if (StringUtils.isNoneEmpty(dayStr, monthStr)) {
            int day = Integer.parseInt(dayStr);
            int month = Integer.parseInt(monthStr);
            int offset = (Integer) firstValueMap.getOrDefault(OFFSET_VALUE, 0);
            LocalDate date = LocalDate.of(year, month, day);
            year = now.isAfter(date) ? year: year - offset;
        }
        Integer add = (Integer) firstValueMap.getOrDefault(YEAR_FIRST_ADD_KEY, 0);
        int firstValue = year + add;
        LinkedList<Map<String, Integer>> dictionaryList = generateYears(yearAttrMap, firstValue);
        sortYears(yearAttrMap, dictionaryList);
        List<Map<String, String>> stringYearsDictionaryList = dictionaryList.stream().sequential()
                .map(map -> map.entrySet()
                        .stream()
                        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), String.valueOf(entry.getValue())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());
        component.getAttrs().put(DICTIONARY_LIST_KEY, dictionaryList);
        Boolean defaultEmpty = (Boolean) yearAttrMap.get(YEAR_DEFAULT_EMPTY_KEY);
        if (!defaultEmpty) {
            setDefaultValue(component, stringYearsDictionaryList, String.valueOf(firstValue));
        }
    }

    private static LinkedList<Map<String, Integer>> generateYears(Map<Object, Object> yearAttrMap, int firstValue) {
        LinkedList<Map<String, Integer>> dictionaryList = new LinkedList<>();
        dictionaryList.add(Map.of(LABEL_KEY, firstValue));
        Integer gen = (Integer) yearAttrMap.get(YEAR_GEN_KEY);
        if (gen > 0) {
            for (int i = 1; i <= gen; i++) {
                dictionaryList.add(Map.of(LABEL_KEY, firstValue + i));
            }
        }
        if (gen < 0) {
            for (int i = -1; i >= gen; i--) {
                dictionaryList.add(Map.of(LABEL_KEY, firstValue + i));
            }
        }
        return dictionaryList;
    }

    private static void sortYears(Map<Object, Object> yearAttrMap, LinkedList<Map<String, Integer>> dictionaryList) {
        String sort = (String) yearAttrMap.get(YEAR_SORT_KEY);
        if (YEAR_SORT_ASC.equals(sort)) {
            dictionaryList.sort(Comparator.comparing(a -> a.get(LABEL_KEY)));
        }
        if (YEAR_SORT_DESC.equals(sort)) {
            dictionaryList.sort(Comparator.comparing(a -> a.get(LABEL_KEY), Comparator.reverseOrder()));
        }
    }

    private static void setDefaultValue(FieldComponent component, List<Map<String, String>> dictionaryList, String firstValue) {
        int index = 0;
        for (int i = 0; i < dictionaryList.size(); i++) {
            if (dictionaryList.get(i).get(LABEL_KEY).equals(firstValue)) {
                index = i;
                break;
            }
        }
        component.getAttrs().put(DEFAULT_VALUE_ATTR,
                Map.of(DEFAULT_VALUE_ID_KEY, String.format("%s-%s", firstValue, index),
                        DEFAULT_VALUE_TEXT_KEY, firstValue,
                        ORIGINAL_ITEM, Map.of(LABEL_KEY, firstValue)));
    }

}
