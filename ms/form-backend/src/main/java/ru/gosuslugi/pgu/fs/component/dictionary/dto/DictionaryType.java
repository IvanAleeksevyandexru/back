package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import ru.gosuslugi.pgu.components.FieldComponentUtil;

/**
 * Тип справочника
 */
public enum DictionaryType {
    /** Источник данных - nsi-справочник. */
    NSI,
    /** Источник данных - хардкодный список в json, задающийся атрибутом {@link FieldComponentUtil#DICTIONARY_LIST_KEY}. */
    LIST,
    /** Источник данных - генератор целых чисел - годов с заданными в json правилами генерации. */
    YEAR
}
