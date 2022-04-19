package ru.gosuslugi.pgu.pgu_common.nsi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * Объект для возврата поля органа ЗАГС act_rec_registrator в компоненте MaritalStatusInput на фронт
 */
@NoArgsConstructor
@Data
public class NsiSuggestDictionaryItem {

    Map<String,Object> originalItem;
    String id;
    String text;

    public NsiSuggestDictionaryItem(Map<String, Object> originalItem) {
        this.originalItem = originalItem;
        this.id = String.valueOf(originalItem.get("code"));
        this.text = String.valueOf(originalItem.get("fullname"));
    }
}
