package ru.gosuslugi.pgu.fs.common.exception.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorModalDescriptor {
    String name;
    Map<String, ErrorModalWindow> modals = new HashMap<>();
}
