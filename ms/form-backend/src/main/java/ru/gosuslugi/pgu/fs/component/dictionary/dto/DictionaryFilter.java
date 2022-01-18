package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gosuslugi.pgu.fs.utils.AttributeValueTypes;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiFilterCondition;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryFilter {
    String attributeName;
    NsiFilterCondition condition;
    String value;
    AttributeValueTypes valueType;
}
