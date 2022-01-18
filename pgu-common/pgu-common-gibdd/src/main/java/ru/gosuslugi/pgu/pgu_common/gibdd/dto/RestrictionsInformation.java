package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String restrictionType;
    /**
     * Статус
     */
    private String status;
    /**
     * Статус в виде числа
     */
    private String statusIntValue;
    /**
     * Дата наложения ограничения
     */
    private String restrictionDate;
    /**
     * Регион инициатора
     */
    private String initiateRegion;
    /**
     * Подразделение ГИБДД
     */
    private String gibddDepart;
    /**
     * Основание ограничений
     */
    private String mainReason;
    /**
     * Формулировка
     */
    private String restrictionDesc;
    /**
     * Номер исполнительного производства
     */
    private String enforcementProceedingsNumber;
    /**
     * Дата возбуждения исполнительного производства
     */
    private String enforcementProceedingsDate;
    /**
     * Наименование исполнительного производства
     */
    private String enforcementProceedingsName;
    /**
     * Наименование органа, выдавшего исполнительного документа
     */
    private String documentAgency;
    /**
     * Номер исполнительного документа
     */
    private String enforcementProceedingsDocumentNumber;
    /**
     * Дата выдачи исполнительного документа
     */
    private String enforcementProceedingsIssueDate;
    /**
     * Адрес отдела судебных приставов
     */
    private String ospAddress;
}
