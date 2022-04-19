package ru.gosuslugi.pgu.fs.common.service.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.cycled.UniqueByAttr;
import ru.gosuslugi.pgu.dto.cycled.UniqueByKey;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponentAttrField;
import ru.gosuslugi.pgu.fs.common.service.ListComponentItemUniquenessService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Сервис определения уникальности элементов списковых компонентов
 * Задача https://jira.egovdev.ru/browse/EPGUCORE-49203 и производные от нее
 */
@Slf4j
@Service
public class ListComponentItemUniquenessServiceImpl implements ListComponentItemUniquenessService {

    @Override
    public boolean initUniqueKeys(FieldComponent fieldComponent, CycledApplicantAnswerItem answerItem) {
        UniqueByAttr uniqueByAttr = JsonProcessingUtil.fromJson(
                JsonProcessingUtil.toJson(fieldComponent.getAttrs().get(FieldComponentUtil.UNIQUE_BY_KEY)),
                UniqueByAttr.class);
        if (uniqueByAttr == null) {
            return false;
        }
        answerItem.getUniqueKeys().clear();
        uniqueByAttr.getKeys().forEach(key -> answerItem.getUniqueKeys().add(""));
        return true;
    }

    @Override
    public List<Boolean> removeUniquenessKeys(FieldComponent cycledComponent, CycledApplicantAnswerItem cycledAnswerItem) {
        UniqueByAttr uniqueByAttr = JsonProcessingUtil.fromJson(
                JsonProcessingUtil.toJson(cycledComponent.getAttrs().get(FieldComponentUtil.UNIQUE_BY_KEY)),
                UniqueByAttr.class);
        List<Boolean> removeKeysFlags = new ArrayList<>();
        if (uniqueByAttr == null) {
            return removeKeysFlags;
        }
        for (int i = 0; i < uniqueByAttr.getKeys().size(); i++) {
            removeKeysFlags.add(i, false);
            for (FieldComponentAttrField uniquenessField : uniqueByAttr.getKeys().get(i).getFields()) {
                if (cycledAnswerItem.getEsiaData().get(uniquenessField.getFieldName()) == null) {
                    cycledAnswerItem.getUniqueKeys().set(i, "");
                    removeKeysFlags.add(i, true);
                    break;
                }
            }
        }
        return removeKeysFlags;
    }

    @Override
    public List<List<Map<String, String>>> validateCycledItemUniqueness(FieldComponent cycledComponent, List<CycledApplicantAnswerItem> answerItems, int indexOfCurrentAnswerItem) {
        UniqueByAttr uniqueByAttr = JsonProcessingUtil.fromJson(
                JsonProcessingUtil.toJson(cycledComponent.getAttrs().get(FieldComponentUtil.UNIQUE_BY_KEY)),
                UniqueByAttr.class);
        if (uniqueByAttr == null) {
            return Collections.emptyList();
        }
        List<List<Map<String, String>>> uniquenessErrors = this.validateCycledItemsUniqueness(cycledComponent, answerItems);
        return uniquenessErrors.isEmpty() ? uniquenessErrors : List.of(uniquenessErrors.get(indexOfCurrentAnswerItem));
    }

    @Override
    public List<List<Map<String, String>>> validateCycledItemsUniqueness(FieldComponent cycledComponent, List<CycledApplicantAnswerItem> answerItems) {
        return validateItemsUniqueness(
                cycledComponent,
                answerItems,
                (answerItem, uniquenessField) -> answerItem.getEsiaData().get(uniquenessField.getFieldName()),
                (answerItem, uniquenessField) -> answerItem.getFieldToId().get(uniquenessField.getFieldName()));
    }

    @Override
    public List<List<Map<String, String>>> validateRepeatableFieldsItemsUniqueness(FieldComponent fieldComponent, List<Map<String, String>> itemsAnswers) {
        List<CycledApplicantAnswerItem> answerItems = AnswerUtil.toCycledApplicantAnswerItem(itemsAnswers);
        for (CycledApplicantAnswerItem answerItem : answerItems) {
            initUniqueKeys(fieldComponent, answerItem);
        }
        return validateItemsUniqueness(
                fieldComponent,
                answerItems,
                (answerItem, uniquenessField) -> answerItem.getItemAnswers().get(uniquenessField.getFieldId()).getValue(),
                (answerItem, uniquenessField) -> uniquenessField.getFieldId());
    }

    private List<List<Map<String, String>>> validateItemsUniqueness(FieldComponent cycledComponent, List<CycledApplicantAnswerItem> answerItems,
                                                                    BiFunction<CycledApplicantAnswerItem, FieldComponentAttrField, Object> getValueFunc,
                                                                    BiFunction<CycledApplicantAnswerItem, FieldComponentAttrField, String> getIdFunc) {
        UniqueByAttr uniqueByAttr = JsonProcessingUtil.fromJson(
                JsonProcessingUtil.toJson(cycledComponent.getAttrs().get(FieldComponentUtil.UNIQUE_BY_KEY)),
                UniqueByAttr.class);
        if (uniqueByAttr == null) {
            return Collections.emptyList();
        }
        List<Boolean> setKeysFlags = new ArrayList<>();

        for (CycledApplicantAnswerItem item : answerItems) {
            List<Boolean> newSetKeysFlags = setUniqueKeys(uniqueByAttr, item, getValueFunc);
            setKeysFlags = mergeKeysFlags(setKeysFlags, newSetKeysFlags);
        }

        List<List<Map<String, String>>> uniquenessErrors = Stream.generate(
                () -> new ArrayList<Map<String, String>>())
                .limit(answerItems.size())
                .collect(Collectors.toList());

        Map<UniquenessKey, List<Integer>> keyToItemIndexMap = new HashMap<>();
        for (int i = 0; i < setKeysFlags.size(); i++) {
            if (!setKeysFlags.get(i)) {
                continue;
            }
            int keyIndex = i;
            List<String> naturalKeys = answerItems.stream()
                    .map(itemKeys -> itemKeys.getUniqueKeys().get(keyIndex))
                    .filter(s -> !StringUtils.isEmpty(s))
                    .collect(Collectors.toList());
            Set<String> naturalKeysSet = new HashSet<>(naturalKeys);
            if (naturalKeys.size() == naturalKeysSet.size()) {
                continue;
            }
            for (int j = 0; j < answerItems.size(); j++) {
                CycledApplicantAnswerItem answerItem = answerItems.get(j);
                UniquenessKey uniquenessKey = new UniquenessKey(answerItem.getUniqueKeys().get(keyIndex), keyIndex);
                keyToItemIndexMap.putIfAbsent(uniquenessKey, new ArrayList<>());
                keyToItemIndexMap.get(uniquenessKey).add(j);
            }

        }
        for (Map.Entry<UniquenessKey, List<Integer>> keyEntry : keyToItemIndexMap.entrySet()) {
            if (keyEntry.getValue().size() > 1) {
                for (int itemIndex : keyEntry.getValue()) {
                    Map<String, String> duplicateCompIdToMsg = new HashMap<>();
                    for (FieldComponentAttrField uniqueField : uniqueByAttr.getKeys().get(keyEntry.getKey().getIndex()).getFields()) {
                        String componentId = getIdFunc.apply(answerItems.get(itemIndex), uniqueField);
                        duplicateCompIdToMsg.put(componentId, uniqueByAttr.getKeys().get(keyEntry.getKey().getIndex()).getErrorMsg());
                    }
                    uniquenessErrors.get(itemIndex).add(duplicateCompIdToMsg);
                }
            }
        }
        if(uniquenessErrors.stream().allMatch(CollectionUtils::isEmpty)) {
            uniquenessErrors = Collections.emptyList();
        }
        return uniquenessErrors;
    }

    private List<Boolean> setUniqueKeys(UniqueByAttr uniqueByAttr, CycledApplicantAnswerItem answerItem,
                                        BiFunction<CycledApplicantAnswerItem, FieldComponentAttrField, Object> getValueFunc) {
        List<Boolean> setKeysFlags = new ArrayList<>();
        for (int i = 0; i < uniqueByAttr.getKeys().size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            UniqueByKey key = uniqueByAttr.getKeys().get(i);
            setKeysFlags.add(true);
            for (FieldComponentAttrField uniquenessField : key.getFields()) {
                Object value = getValueFunc.apply(answerItem, uniquenessField);
                if (Objects.isNull(value)) {
                    setKeysFlags.set(i, false);
                    break;
                }
                stringBuilder.append(value);
            }
            if (setKeysFlags.get(i)) {
                String newKey = DigestUtils.md5Hex(stringBuilder.toString());
                if (Objects.equals(answerItem.getUniqueKeys().get(i), newKey)) {
                    setKeysFlags.set(i, false);
                    continue;
                }
                answerItem.getUniqueKeys().set(i, newKey);
            }
        }
        return setKeysFlags;
    }

    private List<Boolean> mergeKeysFlags(List<Boolean> setKeysFlags, List<Boolean> setNewKeysFlags) {
        if (setKeysFlags.isEmpty()) {
            return setNewKeysFlags;
        }
        for (int i = 0; i < setKeysFlags.size(); i++) {
            if (!setKeysFlags.get(i)) {
                setKeysFlags.set(i, setNewKeysFlags.get(i));
            }
        }
        return setKeysFlags;
    }

    public void updateDisclaimerForUniquenessErrors(FieldComponent fieldComponent, List<List<Map<String, String>>> uniquenessErrors) {
        if (uniquenessErrors != null && !uniquenessErrors.isEmpty() && fieldComponent.getAttrs().get(FieldComponentUtil.UNIQUE_BY_KEY) != null) {
            UniqueByAttr uniqueByAttr = JsonProcessingUtil.fromJson(
                    JsonProcessingUtil.toJson(fieldComponent.getAttrs().get(FieldComponentUtil.UNIQUE_BY_KEY)),
                    UniqueByAttr.class);

            List<String> uniqueFieldIdsWithErrors = uniquenessErrors.stream()
                    .flatMap(Collection::stream)
                    .flatMap(map -> map.keySet().stream())
                    .distinct()
                    .collect(Collectors.toList());

            Set<String> errorLabels = uniqueByAttr.getKeys().stream()
                    .flatMap(key -> key.getFields().stream())
                    .filter(field -> uniqueFieldIdsWithErrors.contains(field.getFieldId()))
                    .map(FieldComponentAttrField::getLabel)
                    .collect(Collectors.toSet());
            uniqueByAttr.getDisclaimer().setUniquenessErrors(errorLabels);

            fieldComponent.getAttrs().put(FieldComponentUtil.UNIQUE_BY_KEY, uniqueByAttr);
        }
    }

    @Data
    @RequiredArgsConstructor
    private static class UniquenessKey {
        private final String key;
        private final int index;
    }
}
