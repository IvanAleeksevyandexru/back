package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Розыск спец продукции
 */
@Data
public class SearchingSpec {

    /**
     * Признак розыска спецпродукции
     */
    @Schema(description = "Признак розыска спецпродукции")
    private Boolean searchingSpecFlag;

    /**
     * Тип спецпродукции
     */
    @Schema(description = "Тип спецпродукции")
    private String specProductType;

    /**
     * Дата операции
     */
    @Schema(description = "Дата операции")
    private String operationDate;

    /**
     * Технологическая операция
     */
    @Schema(description = "Технологическая операция")
    private String techOperation;

    /**
     * Подразделение
     */
    @Schema(description = "Подразделение")
    private String subDivision;

}
