package ru.gosuslugi.pgu.pgu_common.nsi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.common.core.exception.dto.ExternalError;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Тело репсонса от NSI Suggest справочника
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NsiSuggestDictionary {

    private String total;
    private List<Map<String,Object>> items;
    private ExternalError error;

    public Map<String, Object> getItem() {
        return CollectionUtils.isEmpty(items) ? Collections.emptyMap() : items.get(0);
    }

}
