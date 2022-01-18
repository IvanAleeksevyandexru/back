package ru.gosuslugi.pgu.fs.component.dictionary.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Описывает связь атрибутов между рекуррентными nsi-запросами, когда атрибут из ответа одного запроса является атрибутом запроса следующего.  */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeLink {
    public static final AttributeLink NULL = new AttributeLink();
    private String nextAttrName;
    private String previousAttrName;
}
