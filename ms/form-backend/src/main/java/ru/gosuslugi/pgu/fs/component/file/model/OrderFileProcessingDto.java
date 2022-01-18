package ru.gosuslugi.pgu.fs.component.file.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderFileProcessingDto extends FileUploadAnswerDto {
    private List<Map<String, String>> data;
}
