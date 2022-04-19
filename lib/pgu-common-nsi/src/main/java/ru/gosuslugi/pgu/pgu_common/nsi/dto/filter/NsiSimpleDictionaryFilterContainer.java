package ru.gosuslugi.pgu.pgu_common.nsi.dto.filter;

import lombok.Data;

@Data
public class NsiSimpleDictionaryFilterContainer implements NsiDictionaryFilter {
    private NsiDictionaryFilterSimple simple;
}