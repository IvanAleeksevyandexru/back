package ru.gosuslugi.pgu.fs.component.file.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadDto {

    String uploadId;
    List<FileInfo> value;
    /** Название файла для склейки нескольких файлов в один PDF */
    String pdfFileName;
}
