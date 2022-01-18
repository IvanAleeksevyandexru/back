package ru.gosuslugi.pgu.fs.component.parser.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CsvParserDto {
    private Boolean isSuccess = false;
    private String errorMsg;
    private List<Map<String, String>> data;
}
