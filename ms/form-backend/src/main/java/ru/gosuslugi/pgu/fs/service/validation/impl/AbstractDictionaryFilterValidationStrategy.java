package ru.gosuslugi.pgu.fs.service.validation.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.service.validation.DictionaryFilterValidationStrategy;
import ru.gosuslugi.pgu.fs.utils.ContextJsonUtil;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_FILTER_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICT_FILTER_VALUE_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VALUE_NOT_FOUND_MESSAGE;

@Slf4j
@Service
public abstract class AbstractDictionaryFilterValidationStrategy implements DictionaryFilterValidationStrategy {

    protected static final String NOT_CORRECT_JSON_FORMAT = "Ошибка при формировании условий для проверки корректности выбранного значения";

    @Autowired
    protected JsonProcessingService jsonProcessingService;

    /**
     * Метод проверки выбранного пользователем значения на присутствие в справочнике
     *
     * @param incorrectAnswers отображение ошибок валидации
     * @param entry            отображение id компонента и выбранного пользователем значения
     * @param fieldComponent   компонент
     * @param dictionary       словарь
     */
    protected void validateDictionaryItem(Map<String, String> incorrectAnswers,
                                          Map.Entry<String, ApplicantAnswer> entry,
                                          FieldComponent fieldComponent,
                                          NsiDictionary dictionary) {
        Map.Entry<String, String> selectedEntry = getSelectedValue(entry, fieldComponent);
        String attributeName = selectedEntry.getKey();
        String selectedAttributeValue = selectedEntry.getValue();

        if (Objects.nonNull(dictionary)) {
            Optional<NsiDictionaryItem> item = dictionary.getItem(attributeName, selectedAttributeValue);
            if (item.isEmpty()) {
                incorrectAnswers.put(entry.getKey(), String.format(VALUE_NOT_FOUND_MESSAGE, entry.getValue().getValue()));
            }
        }
    }

    /**
     * Метод для получения выбранного пользователем значения
     *
     * @param entry          отображение id компонента и выбранного пользователем значения
     * @param fieldComponent компонент
     * @return выбранное пользователем значение
     */
    private Map.Entry<String, String> getSelectedValue(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        String attributeName = DICT_FILTER_VALUE_NAME;
        @SuppressWarnings("unchecked") var filters = (List<Map<String, Object>>) fieldComponent.getAttrs().get(DICTIONARY_FILTER_NAME_ATTR);
        if (!CollectionUtils.isEmpty(filters)) {
            for (var filter : filters) {
                Object attr = filter.get("attributeName");
                if (attr instanceof String) {
                    attributeName = (String) attr;
                    break;
                }
            }
        }
        return ContextJsonUtil.getAttributeInDepth(jsonProcessingService, entry, ORIGINAL_ITEM, attributeName);
    }
}
