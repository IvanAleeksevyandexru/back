package ru.gosuslugi.pgu.pgu_common.nsi.dto;

import lombok.Data;

@Data
public class DadataAddressElement {

    /**
     * Уровень ФИАС.
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
    private Integer level;

    /**
     * ФИАС код компонента
     */
    private String fiasCode;

    /**
     * Код КЛАДР
     */
    private String kladrCode;

    /**
     * Название элемента(название города или номер дома)
     */
    private String data;

    /**
     * Тип элемента адреса, например "город"
     */
    private String type;

    /**
     * Сокращенный тип элемента адреса, например "г"
     */
    private String shortType;

    /**
     * Кладр код
     * @see DadataAddress
     */
    private String numericFiasCode;
}
