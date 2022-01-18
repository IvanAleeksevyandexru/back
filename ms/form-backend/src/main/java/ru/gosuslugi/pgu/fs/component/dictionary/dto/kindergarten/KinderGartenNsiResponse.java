package ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.atc.idecs.refregistry.ws.RefItem;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Ответ от справочника по дет.садам в виде map с ключом, в который кладётся известный параметр. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KinderGartenNsiResponse {
    private long total;
    private Map<String, RefItem> items;

    @JsonAnyGetter
    public Map<String, RefItem> getItems() {
        return items;
    }

    @JsonAnySetter
    public void putItems(String key, RefItem value) {
        if (items == null) {
            items = new LinkedHashMap<>();
        }
        items.put(key, value);
    }
}
