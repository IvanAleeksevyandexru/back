package ru.gosuslugi.pgu.fs.common.service


import ru.gosuslugi.pgu.dto.csv.CsvColumnDescription
import ru.gosuslugi.pgu.dto.csv.CsvParseDescription
import ru.gosuslugi.pgu.fs.common.service.impl.CsvParserServiceImpl
import spock.lang.Specification

import java.nio.file.Paths

class CsvParserServiceImplSpec extends Specification {

    private static def path = 'src/test/resources/ru/gosuslugi/pgu/fs/common/service/csv'

    private static def fileEmployeesUtf8        = Paths.get("${path}/employees_utf8.csv").toFile()
    private static def fileEmployeesNoRows      = Paths.get("${path}/employees_no-rows.csv").toFile()
    private static def fileEmployeesWindows1251 = Paths.get("${path}/employees_windows1251.csv").toFile()
    private static def fileEmployeesAscii       = Paths.get("${path}/employees_ascii.csv").toFile()
    private static def fileDevicesUtf8          = Paths.get("${path}/gadgets_utf8.csv").toFile()
    private static def fileDevicesWindows1251   = Paths.get("${path}/gadgets_windows1251.csv").toFile()

    private def service = new CsvParserServiceImpl()

    def 'Should parse CSV file without errors (Employees)'() {
        given:
        def descriptions = employeesColumnsDescriptions()

        when:
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 500, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        result.isSuccess && result.data.size() == 4
        !result.error

        result.data[row].size() == 9

        result.data[row]['Фамилия']         == c1
        result.data[row]['Имя']             == c2
        result.data[row]['Отчество']        == c3
        result.data[row]['Дата рождения']   == c4
        result.data[row]['Номер телефона']  == c5
        result.data[row]['Тип ДУЛ']         == c6
        result.data[row]['Серия']           == c7
        result.data[row]['Номер']           == c8
        result.data[row]['Дата выдачи']     == c9

        where:
        file << [fileEmployeesUtf8, fileEmployeesWindows1251, fileEmployeesUtf8, fileEmployeesWindows1251]

        row << [0, 1, 2, 3]

        c1         | c2        | c3            | c4           | c5           | c6                                | c7     | c8        | c9
        'Кузнецов' | 'Алексей' | 'Геннадиевич' | '18.08.2001' | '9670554737' | 'Паспорт РФ'                      | '7301' | '343870'  | '21.01.2021'
        'Глухих'   | 'Матвей'  | 'Петрович'    | '21.05.1990' | '9083339990' | 'Загранпаспорт'                   | '6602' | '865760'  | '19.03.2011'
        'Петров'   | 'Петр'    | 'Семенович'   | '01.02.1980' | '9083339990' | 'Паспорт РФ'                      | '1122' | '1234567' | '01.02.2000'
        'Гусейнов' | 'Элвин'   | ''            | '30.06.1980' | '9051101919' | 'Паспорт иностранного гражданина' | '7702' | '125687'  | '20.10.2011'
    }

    def 'Should parse CSV file without errors (Devices)'() {
        given:
        def descriptions = devicesColumnsDescriptions()

        when:
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 500, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        result.isSuccess && result.data.size() == 18
        !result.error

        result.data[row].size() == 5

        result.data[row]['Наименование устройства']         == c1
        result.data[row]['Идентификационный номер']         == c2
        result.data[row]['Тип устройства']                  == c3
        result.data[row]['Номер телефона']                  == c4
        result.data[row]['Адрес, где находится устройство'] == c5

        where:
        file << [fileDevicesUtf8, fileDevicesWindows1251, fileDevicesUtf8, fileDevicesWindows1251,
                 fileDevicesUtf8, fileDevicesWindows1251, fileDevicesUtf8, fileDevicesWindows1251,
                 fileDevicesUtf8, fileDevicesWindows1251, fileDevicesUtf8, fileDevicesWindows1251,
                 fileDevicesUtf8, fileDevicesWindows1251, fileDevicesUtf8, fileDevicesWindows1251,
                 fileDevicesUtf8, fileDevicesWindows1251]

        row << [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17]

        c1     | c2 | c3                   | c4           | c5
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869265' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869266' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869330' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869331' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869334' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869335' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869337' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869350' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869352' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869359' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869364' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869377' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869383' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869399' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869411' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869414' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869422' | ''
        'Авто' | '' | 'Устройство ГЛОНАСС' | '9039869426' | ''

    }

    def 'Should parse CSV file with error incorrect encoding (US-ASCII)'() {
        given:
        def descriptions = employeesColumnsDescriptions()
        def file = fileEmployeesAscii

        when:
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 500, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        !result.isSuccess && !result.data
        result.error == 'Загрузите этот файл в кодировке UTF-8 или Windows-1251 или замените на другой'
    }

    def 'Should parse not exist file with errors'() {
        given:
        def descriptions = ([:] as List<CsvColumnDescription>)
        def file = Paths.get('').toFile()

        when:
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 500, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        notThrown(IOException)

        and:
        !result.isSuccess && !result.data
        result.error == 'Ошибка при парсинге CSV файла'
    }

    def 'Should parse CSV file with errors (No Content)'() {
        given:
        def descriptions = employeesColumnsDescriptions()
        def file = fileEmployeesNoRows

        when:
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 1, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        !result.isSuccess && !result.data
        result.error == 'В файле отсутствуют данные'
    }

    def 'Should parse CSV file with errors (Max Row Count)'() {
        given:
        def descriptions = employeesColumnsDescriptions()
        def file = fileEmployeesUtf8

        when:
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 1, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        !result.isSuccess && !result.data
        result.error == 'Превышено максимально допустимое количество строк в CSV файле. Максимально допустимое количество строк 1'
    }

    def 'Should parse CSV file with errors (Incorrect Headers)'() {
        given:
        def descriptions = employeesColumnsDescriptions()
        def file = fileEmployeesUtf8

        when:
        descriptions << ([name: 'Тест', required: false] as CsvColumnDescription)
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 1, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        !result.isSuccess && !result.data
        result.error == 'Неправильные заголовки столбцов'
    }

    def 'Should parse CSV file with errors (Required Field)'() {
        given:
        def descriptions = employeesColumnsDescriptions()
        def file = fileEmployeesUtf8

        when:
        descriptions[2].required = true /* отчество */
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 500, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        !result.isSuccess && !result.data
        result.error == """Для обязательного поля 'Отчество' в строке 4 не задано значение"""
    }

    def 'Should parse CSV file without errors (Mask Field)'() {
        given:
        def descriptions = employeesColumnsDescriptions()
        def file = fileEmployeesUtf8

        when:
        descriptions[0].mask = '[a-zа-яA-ZА-Я0-9]{0,50}' /* фамилия */
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 500, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        result.isSuccess && result.data.size() == 4
        !result.error
    }

    def 'Should parse CSV file with errors (Mask Field)'() {
        given:
        def descriptions = employeesColumnsDescriptions()
        def file = fileEmployeesUtf8

        when:
        descriptions[0].mask = '[0-9]{20,50}' /* фамилия */
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 500, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        !result.isSuccess && !result.data
        result.error == """Поле 'Фамилия' в строке 1 не соответствует маске '[0-9]{20,50}'"""
    }

    def 'Should parse CSV file with errors (Custom Error Message)'() {
        given:
        def descriptions = employeesColumnsDescriptions()
        def file = fileEmployeesUtf8

        when:
        descriptions[0].mask = '[0-9]{20,50}' /* фамилия */
        descriptions[0].errorMsg = 'Не верное значение' /* фамилия */
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 500, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        !result.isSuccess && !result.data
        result.error == 'Не верное значение'
    }

    def 'Should parse CSV file with errors (Unique Field)'() {
        given:
        def descriptions = employeesColumnsDescriptions()
        def file = fileEmployeesUtf8

        when:
        descriptions[4].unique = true /* телефон */
        def result = service.parse(file, [charset: null, delimiter: null, maxRowsCount: 500, columnDescriptions: descriptions] as CsvParseDescription)

        then:
        !result.isSuccess && !result.data
        result.error == """Поле 'Номер телефона' в строке 3 не уникально"""
    }

    private static def employeesColumnsDescriptions() {
        [[name: 'Фамилия', required: false] as CsvColumnDescription,
         [name: 'Имя', required: false] as CsvColumnDescription,
         [name: 'Отчество', required: false] as CsvColumnDescription,
         [name: 'Дата рождения', required: false] as CsvColumnDescription,
         [name: 'Номер телефона', required: false] as CsvColumnDescription,
         [name: 'Тип ДУЛ', required: false] as CsvColumnDescription,
         [name: 'Серия', required: false] as CsvColumnDescription,
         [name: 'Номер', required: false] as CsvColumnDescription,
         [name: 'Дата выдачи', required: false] as CsvColumnDescription]
    }

    private static def devicesColumnsDescriptions() {
        [[name: 'Наименование устройства', required: false] as CsvColumnDescription,
         [name: 'Идентификационный номер', required: false] as CsvColumnDescription,
         [name: 'Тип устройства', required: false] as CsvColumnDescription,
         [name: 'Номер телефона', required: false] as CsvColumnDescription,
         [name: 'Адрес, где находится устройство', required: false] as CsvColumnDescription]
    }
}
