package ru.gosuslugi.pgu.fs.pgu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PguServiceCodes {
    String passport;
    String target;
}
