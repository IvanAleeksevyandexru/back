package ru.gosuslugi.pgu.fs.component.file.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileUploadAnswerDto {
    String id;
    String type;
    List<UploadDto> uploads;
    Integer totalSize;
    Integer totalCount;
}
