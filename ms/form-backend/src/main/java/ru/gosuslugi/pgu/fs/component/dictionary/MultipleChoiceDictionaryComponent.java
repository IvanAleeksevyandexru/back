package ru.gosuslugi.pgu.fs.component.dictionary;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.common.core.exception.NsiExternalException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.DictionaryType;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VALUE_ATTR;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.DICTIONARY_LIST_KEY;

/**
 * Компонент "Список с возможностью множественного выбора"
 * @see <a href="https://confluence.egovdev.ru/pages/viewpage.action?pageId=184951863">Список с возможностью множественного выбора</a>
 */
@Component
public class MultipleChoiceDictionaryComponent extends DropDownComponent {

    /** Имя атрибута со списком значений в applicantAnswer передаётся его содержимое). */
    private static final String LIST_ATTR = "list";
    /** Имя атрибута для количества значений. */
    private static final String AMOUNT_ATTR = "amount";

    private final NsiDictionaryService nsiDictionaryService;
    private final DictionaryFilterService dictionaryFilterService;

    public MultipleChoiceDictionaryComponent(UserPersonalData userPersonalData, NsiDictionaryService nsiDictionaryService, DictionaryFilterService dictionaryFilterService, DictionaryListPreprocessorService dictionaryListPreprocessorService) {
        super(userPersonalData, dictionaryListPreprocessorService);
        this.nsiDictionaryService = nsiDictionaryService;
        this.dictionaryFilterService = dictionaryFilterService;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.MultipleChoiceDictionary;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> presetValues = new HashMap<>(dictionaryFilterService.getInitialValue(component, scenarioDto));
        return presetValues.isEmpty() ? super.getInitialValue(component, scenarioDto) : ComponentResponse.of(jsonProcessingService.toJson(presetValues));
    }

    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry,
                                    ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        List<Object> entryValues;
        if (AnswerUtil.tryParseToMap(entry.getValue().getValue()) instanceof Map) {
            Map<Object, Object> entryMap = AnswerUtil.toMap(entry, true);
            entryValues = (List<Object>) entryMap.get(LIST_ATTR);
            if (CollectionUtils.isEmpty(entryValues)) {
                return;
            }
            Integer amount = (Integer) entryMap.get(AMOUNT_ATTR);
            if (amount == null) {
                incorrectAnswers.put(entry.getKey(), String.format("Не найдено значение %s в компоненте", AMOUNT_ATTR));
                return;
            }
            if (amount != entryValues.size()) {
                incorrectAnswers.put(entry.getKey(), String.format("Список %s выбранных элементов содержит %d элементов, а в атрибуте %s передаётся %d", LIST_ATTR, entryValues.size(), AMOUNT_ATTR, amount));
                return;
            }
        } else {
            entryValues = AnswerUtil.toList(entry, true);
            if (CollectionUtils.isEmpty(entryValues)) {
                return;
            }
        }

        // обрабатываем справочники типа NSI и LIST, а YEAR и неопознанные игнорируем
        DictionaryType dictionaryType = getDictionaryType(fieldComponent);
        if (dictionaryType == DictionaryType.NSI) {
            Object dictionary = fieldComponent.getAttrs().get(DICTIONARY_NAME_ATTR);
            String dictionaryName = fieldComponent.getAttrs().get(DICTIONARY_NAME_ATTR).toString();
            // проверяем гендерный компонент или обычный
            if (dictionary instanceof List) {
                dictionaryName = userPersonalData.getPerson().getGender().equals(MAN_GENDER) ?
                        ((List<?>) dictionary).get(0).toString() : ((List<?>) dictionary).get(1).toString();
            }
            List<String> values = entryValues.stream().map(it -> getDictionaryValue(it, VALUE_ATTR)).collect(Collectors.toList());

            // приходится перебирать по одному так как не все справочники поддерживают union-predicates
            for (String value : values) {
                try {
                    Optional<NsiDictionaryItem> dictionaryItem = nsiDictionaryService.getDictionaryItemByValue(dictionaryName, VALUE_ATTR, value);
                    if (dictionaryItem.isEmpty()) {
                        incorrectAnswers.put(entry.getKey(), String.format("Не найдено значение %s в справочнике НСИ %s", value, dictionaryName));
                        return;
                    }
                } catch (NsiExternalException e) {
                    String message = e.getValueMessage();
                    incorrectAnswers.put(entry.getKey(), "Справочник " + dictionaryName + ", значение " + value + ". Сообщение: " + message);
                    return;
                }

            }
        } else if (dictionaryType == DictionaryType.LIST) {
            String name = entry.getKey();
            entryValues.forEach(it -> validateLabel(name, getDictionaryValue(it, LABEL_KEY), incorrectAnswers, fieldComponent));
        }
    }

    /**
     * Определение источника значений для множественного выбора.
     * Значения отображаются в модальном окне как несколько чекбоксов.
     * @param fieldComponent компонент
     * @return тип источника данных
     */
    private DictionaryType getDictionaryType(FieldComponent fieldComponent) {
        if (fieldComponent.getAttrs().containsKey(DICTIONARY_NAME_ATTR)) {
            return DictionaryType.NSI;
        }

        if (fieldComponent.getAttrs().containsKey(DICTIONARY_LIST_KEY)) {
            return DictionaryType.LIST;
        }

        if (fieldComponent.getAttrs().containsKey(YEAR_GEN)) {
            return DictionaryType.YEAR;
        }

        return null;
    }

    /**
     *  Получение значения из currentValue, передаваемому с UI.
     * @param item объект, содержащийся в currentValue
     * @param attr имя атрибута
     * @return значение атрибута
     */
    private String getDictionaryValue(Object item, String attr) {
        Map<String, Object> map = (Map<String, Object>) item;
        Map<String, Object> originalItemMap = (Map<String, Object>) map.getOrDefault(ORIGINAL_ITEM, Map.of());
        return originalItemMap.getOrDefault(attr, "").toString();
    }
}
