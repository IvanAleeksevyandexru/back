package ru.gosuslugi.pgu.fs.common.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.csv.CsvColumnDescription;
import ru.gosuslugi.pgu.dto.csv.CsvParseDescription;
import ru.gosuslugi.pgu.dto.csv.CsvParseResult;
import ru.gosuslugi.pgu.fs.common.service.CsvParserService;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис парсинга CSV файлов
 */
@Slf4j
@Service
public class CsvParserServiceImpl implements CsvParserService {

    private static final List<Character> POSSIBLE_DELIMITERS = List.of(';', '|', ',', '\t');
    private static final String PARSING_ERROR = "Ошибка при парсинге CSV файла";
    private static final String NO_DATA_ERROR = "В файле отсутствуют данные";
    private static final String INCORRECT_HEADERS = "Неправильные заголовки столбцов";
    private static final String REQUIRED_ROW_EMPTY_ERROR = "Для обязательного поля '%s' в строке %d не задано значение";
    private static final String UNIQUE_COLUMN_ERROR = "Поле '%s' в строке %d не уникально";
    private static final String MASK_ERROR = "Поле '%s' в строке %d не соответствует маске '%s'";
    private static final String MAX_ROWS_COUNT_ERROR = "Превышено максимально допустимое количество строк в CSV файле. Максимально допустимое количество строк %d";
    private static final String UNSUPPORTED_FILE_ENCODING = "Загрузите этот файл в кодировке UTF-8 или Windows-1251 или замените на другой";
    private static final String CHARSET_WIN_1251 = "windows-1251";

    /**
     * Парсит файл и проверяет данные на соответсвия правилам.
     * При первом невыполнении соответствия происходит завершение метода с информацией об ошибке.
     * В результат парсинга попадают только те колонки для которых есть правила.
     *
     * @param file             Файл
     * @param parseDescription Правила к данным из файла
     * @return Результат парсинга
     */
    @Override
    public CsvParseResult parse(File file, CsvParseDescription parseDescription) {
        Charset charset = StringUtils.isEmpty(parseDescription.getCharset())
                ? getCharset(file)
                : Charset.forName(parseDescription.getCharset());
        // завершаем если файл имеет неподдерживаемую кодировку
        if (!StandardCharsets.UTF_8.equals(charset) && !Charset.forName(CHARSET_WIN_1251).equals(charset)) {
            return new CsvParseResult(false, UNSUPPORTED_FILE_ENCODING);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {

            CSVParser records = CSVFormat.EXCEL
                    .withFirstRecordAsHeader()
                    .withIgnoreEmptyLines()
                    .withDelimiter(StringUtils.isEmpty(parseDescription.getDelimiter())
                            ? getDelimiter(file)
                            : parseDescription.getDelimiter().charAt(0))
                    .parse(reader);

            List<CSVRecord> cachedRecords = records.getRecords();
            List<String> headers = records.getHeaderNames();
            List<CsvColumnDescription> columnDescriptions = parseDescription.getColumnDescriptions();

            List<String> expectedColumnNames = columnDescriptions.stream()
                    .map(CsvColumnDescription::getName)
                    .collect(Collectors.toList());

            // завершаем если колонки имеют не корректные кол-во, порядок или наименование
            if (!expectedColumnNames.equals(headers)) {
                return new CsvParseResult(false, INCORRECT_HEADERS);
            }

            // завершаем если отсутствуют строки
            if (cachedRecords.isEmpty()) {
                return new CsvParseResult(false, NO_DATA_ERROR);
            }

            // проверяем на кол-во строк
            if (Objects.nonNull(parseDescription.getMaxRowsCount())
                    && (cachedRecords.size() > parseDescription.getMaxRowsCount())) {
                return new CsvParseResult(
                        false,
                        String.format(MAX_ROWS_COUNT_ERROR, parseDescription.getMaxRowsCount())
                );
            }

            List<Map<String, String>> data = new ArrayList<>();
            Map<Integer, Set<String>> uniqueValues = new HashMap<>();
            int recordsCount = 0;
            for (CSVRecord record : cachedRecords) {
                // проверяем значения и заполняем результат
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < columnDescriptions.size(); i++) {
                    String value = record.get(i);
                    if (Objects.nonNull(value)) {
                        value = value.trim();
                    }
                    // проверяем на обязательность
                    if (columnDescriptions.get(i).isRequired() && StringUtils.isEmpty(value)) {
                        return new CsvParseResult(
                                false,
                                String.format(REQUIRED_ROW_EMPTY_ERROR, headers.get(i), ++recordsCount)
                        );
                    }
                    // проверяем на уникальность
                    if (columnDescriptions.get(i).isUnique() && columnHasDuplicate(uniqueValues, i, value)) {
                        return new CsvParseResult(
                                false,
                                String.format(UNIQUE_COLUMN_ERROR, headers.get(i), ++recordsCount)
                        );
                    }
                    // проверяем по маске
                    if (!StringUtils.isEmpty(columnDescriptions.get(i).getMask())
                            && !StringUtils.isEmpty(value)
                            && !value.matches(columnDescriptions.get(i).getMask())) {
                        return new CsvParseResult(
                                false,
                                !StringUtils.isEmpty(columnDescriptions.get(i).getErrorMsg())
                                        ? columnDescriptions.get(i).getErrorMsg()
                                        : String.format(MASK_ERROR, headers.get(i), ++recordsCount, columnDescriptions.get(i).getMask())
                        );
                    }
                    map.put(columnDescriptions.get(i).getName(), value);
                }
                data.add(map);
                recordsCount++;
            }
            return new CsvParseResult(data);
        } catch (IOException e) {
            log.error(PARSING_ERROR, e);
            return new CsvParseResult(false, PARSING_ERROR);
        }
    }

    private boolean columnHasDuplicate(Map<Integer, Set<String>> uniqueValues, int columnIndex, String value) {
        Set<String> columnValues = uniqueValues.getOrDefault(columnIndex, new HashSet<>());
        uniqueValues.put(columnIndex, columnValues);
        return !columnValues.add(value);
    }

    private Charset getCharset(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            AutoDetectReader reader = new AutoDetectReader(inputStream);
            Charset charset = reader.getCharset();
            if (!StandardCharsets.UTF_8.equals(charset) && !Charset.forName(CHARSET_WIN_1251).equals(charset)){
                charset = StandardCharsets.US_ASCII;
                CharsetDetector detector = new CharsetDetector();
                detector.setText(IOUtils.toByteArray(new FileInputStream(file)));
                CharsetMatch charsetMatch = detector.detect();
                if ("ru".equals(charsetMatch.getLanguage())) {
                    charset = Charset.forName(CHARSET_WIN_1251);
                }
            }
            return charset;
        } catch (TikaException | IOException e) {
            log.error("Ошибка получения кодировки", e);
        }
        return StandardCharsets.UTF_8;
    }

    private Character getDelimiter(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            for (Character character : POSSIBLE_DELIMITERS) {
                if (line.contains(character.toString())) {
                    return character;
                }
            }
        } catch (IOException e) {
            log.error("Ошибка чтения файла", e);
        }
        return POSSIBLE_DELIMITERS.get(0);
    }
}
