package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import lombok.Data;

import java.util.Map;

/** Класс для использования в компоненте как generic. */
@Data
public class FssDataItem {
    private String title;
    private String value;
    private Map<String, String> attributeValues;
}
