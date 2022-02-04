package ru.gosuslugi.pgu.fs.common.exception.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorContent {
    private StatusIcon statusIcon;
    private String header;
    private String helperText;
}
