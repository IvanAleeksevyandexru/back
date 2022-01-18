package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DictionaryFilters {

    private List<List<Map<String, Object>>> filters;
}
