package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Сведения об ограничениях
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestrictionsInformation {

    /**
     * Вид ограничения
     */
    @Schema(description = "Вид ограничения")
    private String restrictionType;

    /**
     * Статус
     */
    @Schema(description = "Статус")
    private String status;

    /**
     * Статус в виде числа
     */
    @Schema(description = "Статус в виде числа")
    private String statusIntValue;

    /**
     * Дата наложения ограничения
     */
    @Schema(description = "Дата наложения ограничения")
    private String restrictionDate;

    /**
     * Регион инициатора
     */
    @Schema(description = "Регион инициатора")
    private String initiateRegion;

    /**
     * Подразделение ГИБДД
     */
    @Schema(description = "Подразделение ГИБДД")
    private String gibddDepart;

    /**
     * Основание ограничений
     */
    @Schema(description = "Основание ограничений")
    private String mainReason;

    /**
     * Формулировка
     */
    @Schema(description = "Формулировка")
    private String restrictionDesc;

    /**
     * Номер исполнительного производства
     */
    @Schema(description = "Номер исполнительного производства")
    private String enforcementProceedingsNumber;

    /**
     * Дата возбуждения исполнительного производства
     */
    @Schema(description = "Дата возбуждения исполнительного производства")
    private String enforcementProceedingsDate;

    /**
     * Наименование исполнительного производства
     */
    @Schema(description = "Наименование исполнительного производства")
    private String enforcementProceedingsName;

    /**
     * Наименование органа, выдавшего исполнительного документа
     */
    @Schema(description = "Наименование органа, выдавшего исполнительного документа")
    private String documentAgency;

    /**
     * Номер исполнительного документа
     */
    @Schema(description = "Номер исполнительного документа")
    private String enforcementProceedingsDocumentNumber;

    /**
     * Дата выдачи исполнительного документа
     */
    @Schema(description = "Дата выдачи исполнительного документа")
    private String enforcementProceedingsIssueDate;

    /**
     * Адрес отдела судебных приставов
     */
    @Schema(description = "Адрес отдела судебных приставов")
    private String ospAddress;

}
