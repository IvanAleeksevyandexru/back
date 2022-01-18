package ru.gosuslugi.pgu.fs.component.logic.model;

import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Data
public class RestCallDto {
    private String method;
    private String url;
    private String body;
    private Long timeout;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();
    private MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
}