package ru.gosuslugi.pgu.fs.component.dictionary.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.atc.idecs.refregistry.ws.ListRefItemsResponse;
import ru.atc.idecs.refregistry.ws.RefItem;

import java.io.Serializable;
import java.util.Map;

/** Один элемент, формируемый по элементу ответа {@link RefItem}, возвращаемый на UI. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentItem {
    private String value;
    private String title;
    private Map<String, Object> attributeValues;
}