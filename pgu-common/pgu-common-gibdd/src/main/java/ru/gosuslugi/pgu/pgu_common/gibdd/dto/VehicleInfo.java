package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Данные о транспортном средстве из Витрины ГИБДД
 * https://jira.egovdev.ru/browse/EPGUCORE-90200 - расширение для 1.4+, включение нормализованных значений
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleInfo {

    /**
     * Статус ТС
     */
    @Schema(description = "Статус ТС")
    private String status;

    /**
     * Статус ТС в виде числа
     */
    @Schema(description = "Статус ТС в виде числа")
    private String statusIntValue;

    /**
     * Юридическая чистота
     */
    @Schema(description = "Юридическая чистота")
    private Boolean legals = true;

    /**
     * СТС - Серия и номер документа (RegistrationDoc.RegDocSeriesNumber в версии витрины 1.4)
     */
    @Schema(description = "СТС - Серия и номер документа (RegistrationDoc.RegDocSeriesNumber в версии витрины 1.4)")
    private String stsSeriesNumber;

    /**
     * Паспорт транспортного средства - Тип документа
     */
    @Schema(description = "Паспорт транспортного средства - Тип документа")
    private String ptsType;

    /**
     * Паспорт транспортного средства - Серия документа
     */
    @Schema(description = "Паспорт транспортного средства - Серия документа")
    private String ptsNum;

    /**
     * Признак наложенных ограничений
     */
    @Schema(description = "Признак наложенных ограничений")
    private Boolean restrictionsFlag;

    /**
     * Категория
     */
    @Schema(description = "Категория")
    private String category;

    /**
     * Признак розыска транспортного средства
     */
    @Schema(description = "Признак розыска транспортного средства")
    private Boolean searchingTransportFlag;

    /**
     * Полное наименование цвета кузова (кабины)
     */
    @Schema(description = "Полное наименование цвета кузова (кабины)")
    private String carcaseColor;

    /**
     * Марка
     */
    @Schema(description = "Марка")
    private String markName;

    /**
     * Модель
     */
    @Schema(description = "Модель")
    private String modelName;

    /**
     * Марка, модель (модификация)
     */
    @Schema(description = "Марка, модель (модификация)")
    private String modelMarkName;

    /**
     * Год выпуска
     */
    @Schema(description = "Год выпуска")
    private String manufacturedYear;

    /**
     * Государственный регистрационный номер
     */
    @Schema(description = "Государственный регистрационный номер")
    private String govRegNumber;

    /**
     * Статус записи
     */
    @Schema(description = "Статус записи")
    private String recordStatus;

    /**
     * Идентификатор витрины
     */
    @Schema(description = "Идентификатор витрины")
    private String vin;

    /**
     * Номер шасси (рамы)
     */
    @Schema(description = "Номер шасси (рамы)")
    private String chassisNumber;

    /**
     * Номер кузова (кабины)
     */
    @Schema(description = "Номер кузова (кабины)")
    private String carcaseNumber;

    /**
     * Тип транспортного средства
     */
    @Schema(description = "Тип транспортного средства")
    private String vehicleType;

    /**
     * Модель двигателя
     */
    @Schema(description = "Модель двигателя")
    private String engineModel;

    /**
     * Номер двигателя
     */
    @Schema(description = "Номер двигателя")
    private String engineNum;

    /**
     * Объем двигателя (куб.см)
     */
    @Schema(description = "Объем двигателя (куб.см)")
    private String engineVolume;

    /**
     * Мощность л.с.
     */
    @Schema(description = "Мощность л.с.")
    private String enginePowerHorse;

    /**
     * Мощность (кВт)
     */
    @Schema(description = "Мощность (кВт)")
    private String enginePowerVt;

    /**
     * Экологический класс
     */
    @Schema(description = "Экологический класс")
    private String ecologyClass;

    /**
     * Экологический класс, описание значения
     */
    @Schema(description = "Экологический класс, описание значения")
    private String ecologyClassDesc;

    /**
     * Последнее регистрационное действие
     */
    @Schema(description = "Последнее регистрационное действие")
    private String lastRegActionName;

    /**
     * Сведения об ограничениях
     */
    @Schema(description = "Сведения об ограничениях")
    private List<RestrictionsInformation> restrictions = new ArrayList<>();

    /**
     * Периоды владения
     */
    @Schema(description = "Периоды владения")
    private List<OwnerPeriod> ownerPeriods = new ArrayList<>();

    /** Расширение для версии 1.4+ */

    /** Идентификатор витрины нормализированный */
    @Schema(description = "Идентификатор витрины нормализированный")
    private String vinNorm;

    /** Идентификационный номер (VIN2) нормализированный */
    @Schema(description = "Идентификационный номер (VIN2) нормализированный")
    private String vin2Norm;

    /** Номер шасси (рамы) нормалированный */
    @Schema(description = "Номер шасси (рамы) нормалированный")
    private String chassisNumberNorm;

    /** Номер кузова (кабины) нормазированный */
    @Schema(description = "Номер кузова (кабины) нормазированный")
    private String carcaseNumberNorm;
}
