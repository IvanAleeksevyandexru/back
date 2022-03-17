package ru.gosuslugi.pgu.fs.service.validation.impl;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DictType {

    dictionary,
    childrenClubsApi,
    nsiSuggest,
    lkApi,
    schoolDictionaryUrl,
    schoolSearchUrl
    ;

    private static final Map<String, DictType> dictTypes =
            Stream.of(values()).collect(Collectors.toMap(Enum::name, Function.identity()));

    public static DictType fromStringOrDefault(String dictType, DictType defaultType) {
        return dictTypes.getOrDefault(dictType, defaultType);
    }
}
