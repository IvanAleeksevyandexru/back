package ru.gosuslugi.pgu.fs.component.input.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocInputDto {

    private String series;
    private String number;
    private String date;
    private String emitter;
    private String expirationDate;
    private String issueId;

}
