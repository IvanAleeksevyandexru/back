package ru.gosuslugi.pgu.components;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ChildrenUtilResponse {
    private String presetValue;
    private Map<String, Map<String, Object>> childFullDataMap = new HashMap<>();

    public void addChildData(String childId, Map<String, Object> childDataMap) {
        if (this.childFullDataMap == null) {
            this.childFullDataMap = new HashMap<>();
        }
        childFullDataMap.put(childId, childDataMap);
    }
}
