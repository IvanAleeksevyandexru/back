package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import lombok.Data;

import java.util.Map;

/** Класс для Использования в компоненте. */
@Data
public class SopDataItem {
    private String title;
    private String value;
    private Map<String, Object> attributeValues;
}
