package ru.gosuslugi.pgu.components.descriptor.attr_factory;

import ru.gosuslugi.pgu.components.descriptor.converter.ConverterType;
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.PlaceholderType;
import ru.gosuslugi.pgu.components.descriptor.placeholder.Reference;
import ru.gosuslugi.pgu.components.descriptor.placeholder.ReferenceAttrs;
import ru.gosuslugi.pgu.components.descriptor.placeholder.SequentialPlaceholder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;

public interface AttrsFactory {

    String REFS_ATTR  = "refs";
    String REFS_PATH  = "path";
    String REFS_CONVERTER  = "converter";

    /**
     * Достаёт рефы и складывает их в {@code Map}
     * @return рефы или пустая {@code Map} рефов
     */
    Map<String, Object> getRefsMap();

    /**
     * Создаются плайсхолдеры
     * @return плейсхолдеры
     */
    List<Placeholder> getPlaceholders();

    /**
     * Создает рефы с их атрибутами и конвертером (поля {@code refs}, {@code path}, {@code converter})
     * @return рефы или пустой список рефов
     */
    default List<Reference> getReferences(Map<String, Object> additionalAttrs) {
        var refsMap = getRefsMap();
        return refsMap.entrySet().stream()
            .map(entry -> getReference(entry.getKey(), entry.getValue(), additionalAttrs))
            .collect(Collectors.toList());
    }


    default List<Placeholder> getPlaceholders(Map<String, String> extraAttrs) {
        if (extraAttrs == null) {
            return Collections.emptyList();
        }

        return extraAttrs.entrySet().stream().map(it -> {
            if (PlaceholderType.sequential.name().equals(it.getValue())) {
                SequentialPlaceholder attr = new SequentialPlaceholder();
                attr.setKey(it.getKey());
                return attr;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

    }

    /**
     * Создает реф с атрибутами и конвертером (поля {@code refs}, {@code path}, {@code converter})
     * @return рефы или пустой список рефов
     */
    default Reference getReference(String key, Object refBody, Map<String, Object> additionalAttrs) {
        Reference reference = new Reference();
        reference.setKey(key);
        if (refBody instanceof String) {
            reference.setPath(refBody.toString());
        }
        if (refBody instanceof Map) {
            Map<String, Object> refMap = (Map<String, Object>) refBody;
            refMap.putAll(additionalAttrs);
            if (refMap.containsKey(REFS_PATH)) {
                reference.setPath(refMap.get(REFS_PATH).toString());
                if (refMap.containsKey(REFS_CONVERTER)) {
                    ReferenceAttrs refAttrs = new ReferenceAttrs();
                    refAttrs.setConverterType(ConverterType.valueOf(refMap.get(REFS_CONVERTER).toString()));
                    refAttrs.setAttrs(refMap);
                    reference.setAttrs(refAttrs);
                }
            }
        }
        return reference;
    }

    default Reference getReference(LinkedValue linkedValue) {
        Map<String, Object> converterSettings = linkedValue.getConverterSettings();
        Object source = linkedValue.getSource();
        if (isNull(converterSettings)) {
            return getReference(linkedValue.getArgument(), source, emptyMap());
        }

        converterSettings.put(REFS_PATH, isNull(source) ? "" : source);
        return getReference(linkedValue.getArgument(), converterSettings, emptyMap());
    }
}
