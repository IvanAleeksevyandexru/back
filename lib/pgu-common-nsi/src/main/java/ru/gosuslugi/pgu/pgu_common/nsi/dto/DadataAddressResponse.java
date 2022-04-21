package ru.gosuslugi.pgu.pgu_common.nsi.dto;

import lombok.Data;

/**
 * Ответ, получаемый от сервиса Dadata
 */
@Data
public class DadataAddressResponse {
    /** Frontend main field */
    private String text;

    private DadataError error;

    /**
     * Код проверки:
     *
     * 0 - Адрес распознан уверенно
     * 2 - Адрес пустой или заведомо «мусорный»
     * 1 - Остались «лишние» части
     * 3 - Есть альтернативные варианты
     */
    private Integer dadataQc;

    /**
     * Код пригодности к рассылке:
     *
     * 0 - Да. Пригоден для почтовой рассылки
     * 10 - Под вопросом. Дома нет в ФИАС
     * 5 - Под вопросом. Нет квартиры. Подходит для юридических лиц или частных владений
     * 8 - Под вопросом. До почтового отделения — абонентский ящик или адрес до востребования. Подходит для писем, но не для курьерской доставки.
     * 9 - Под вопросом. Сначала проверьте, правильно ли Дадата разобрала исходный адрес
     * 1 - Нет. Нет региона
     * 2 - Нет. Нет города
     * 3 - Нет. Нет улицы
     * 4 - Нет. Нет дома
     * 6 - Нет. Адрес неполный
     * 7 - Нет. Иностранный адрес
     */
    private Integer dadataQcComplete;

    /**
     * Уточняют вероятность успешной доставки письма:
     *
     * 2 - Высокая. Дом найден в ФИАС
     * 10 - Высокая. Дом не найден в ФИАС, но есть на картах
     * 10 - Средняя. Дом не найден в ФИАС, но есть похожий на картах
     * 10 - Низкая. Дом не найден в ФИАС и на картах
     */
    private Integer dadataQcHouse;

    /**
     * Нераспознанная часть адреса
     */
    private String unparsedParts;

    /**
     * Уровень ФИАС до которого распознан адрес.
     *
     * 0 — страна
     * 1 — регион
     * 3 — район
     * 4 — город
     * 5 — район города
     * 6 — населенный пункт
     * 7 — улица
     * 8 — дом
     * 65 — планировочная структура
     * 90 — доп. территория
     * 91 — улица в доп. территории
     * -1 — иностранный или пустой
     */
    private Integer fiasLevel;

    /**
     * Индекс адреса
     */
    private String postalCode;

    /**
     * Абонентский ящик
     */
    private String postalBox;

    private DadataAddress address ;

    /**
     * Код КЛАДР региона
     */
    private String regionKladrId;

    /**
     * ОКАТО код
     */
    private String okato;

    /**
     * Код ИФНС для физических лиц
     */
    private String tax_office;

    /**
     * ОКТМО код
     */
    private String oktmo;

    /**
     * Ширина координат
     */
    private String geo_lat;

    /**
     * Долгота координат
     */
    private String geo_lon;
}
