package ru.gosuslugi.pgu.components.descriptor.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationFieldDto {
    private String fieldName;
    private String label;
    private String type;
    private Map<String, Object> attrs;
}
