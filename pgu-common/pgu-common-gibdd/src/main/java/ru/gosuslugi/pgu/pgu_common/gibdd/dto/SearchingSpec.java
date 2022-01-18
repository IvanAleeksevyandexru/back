package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import lombok.Data;

/**
 * Розыск спец продукции
 */
@Data
public class SearchingSpec {
    /**
     * Признак розыска спецпродукции
     */
    private Boolean searchingSpecFlag;
    /**
     * Тип спецпродукции
     */
    private String specProductType;
    /**
     * Дата операции
     */
    private String operationDate;
    /**
     * Технологическая операция
     */
    private String techOperation;
    /**
     * Подразделение
     */
    private String subDivision;
}
