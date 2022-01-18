package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.csv.CsvParseDescription;
import ru.gosuslugi.pgu.dto.csv.CsvParseResult;

import java.io.File;

public interface CsvParserService {

    CsvParseResult parse(File file, CsvParseDescription parseDescription);
}
