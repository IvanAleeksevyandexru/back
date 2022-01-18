package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

@Data
public class MapServiceAttributes {

    @JsonAlias("dictionaryFilter")
    private List<DictionaryFilter> dictionaryFilters;

    private List<DictionaryFilter> secondaryDictionaryFilter;

    private AttrValue addressString;
    private List<PresetField> fields;
    private boolean validationOn = true;
    private String dictionaryType;
    private AttrValue personalData;

    private Set<String> federalLevelUnitsFias;
    private Set<String> districtLevelUnitsFias;

    private List<MvdFilter> mvdFilters;

    public boolean useSecondFilter() {
        if (isNull(secondaryDictionaryFilter) || secondaryDictionaryFilter.isEmpty()) {
            return false;
        }
        dictionaryFilters = secondaryDictionaryFilter;
        return true;
    }
}
