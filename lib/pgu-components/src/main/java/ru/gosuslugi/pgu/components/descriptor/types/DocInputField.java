package ru.gosuslugi.pgu.components.descriptor.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocInputField {
    private String fieldName;
    private String label;
    private String type;
    private String hint;
    private boolean required;
    private Map<String, Object> attrs;
}
