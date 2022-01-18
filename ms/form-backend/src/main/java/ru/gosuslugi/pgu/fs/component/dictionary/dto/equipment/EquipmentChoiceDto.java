package ru.gosuslugi.pgu.fs.component.dictionary.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** Ответ, возвращаемый на UI с элементами из nsi-справочника и дополнительными значениями атрибутов. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentChoiceDto {
    private List<EquipmentItem> items;
    private Map<String, Object> attrs;
}
