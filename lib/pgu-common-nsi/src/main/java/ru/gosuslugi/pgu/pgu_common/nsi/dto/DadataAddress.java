package ru.gosuslugi.pgu.pgu_common.nsi.dto;

import lombok.Data;

import java.util.List;


/**
 * Адрес из сервиса Dadata
 */
@Data
public class DadataAddress {

    /**
     * ФИАС код
     */
    private String fiasCode;

    /**
     * Кладр код
     * Код КЛАДР состоит из ССРРРГГГПППАА (13 символов) или ССРРРГГГПППУУУУАА (17 символов), где:
     * СС – код субъекта Российской Федерации;
     * РРР – код района;
     * ГГГ – код города;
     * ППП – код населенного пункта;
     * УУУУ – код улицы;
     * АА – признак актуальности.
     */
    private String numericFiasCode;

    /**
     * Полный адрес строкой
     */
    private String fullAddress;

    /**
     * Индекс адреса
     */
    private String postIndex;

    /**
     * Массив адресных элементов
     */
    private List<DadataAddressElement> elements;
}
