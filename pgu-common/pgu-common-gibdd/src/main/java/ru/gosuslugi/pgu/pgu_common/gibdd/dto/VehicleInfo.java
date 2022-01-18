package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Данные о транспортном средстве из Витрины ГИБДД
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleInfo {
    /**
     * Статус ТС
     */
    private String status;
    /**
     * Статус ТС в виде числа
     */
    private String statusIntValue;
    /**
     * Юридическая чистота
     */
    private Boolean legals = true;
    /**
     * СТС - Серия и номер  документа (RegistrationDoc.RegDocSeriesNumber в версии витрины 1.4)
     */
    private String stsSeriesNumber;
    /**
     * Паспорт транспортного средства - Тип документа
     */
    private String ptsType;
    /**
     * Паспорт транспортного средства - Серия документа
     */
    private String ptsNum;
    /**
     * Признак наложенных ограничений
     */
    private Boolean restrictionsFlag;
    /**
     * Категория
     */
    private String category;
    /**
     * Признак розыска транспортного средства
     */
    private Boolean searchingTransportFlag;
    /**
     * Полное наименование цвета кузова (кабины)
     */
    private String carcaseColor;
    /**
     * Марка
     */
    private String markName;
    /**
     * Модель
     */
    private String modelName;
    /**
     * Марка, модель (модификация)
     */
    private String modelMarkName;
    /**
     * Год выпуска
     */
    private String manufacturedYear;
    /**
     * Государственный регистрационный номер
     */
    private String govRegNumber;
    /**
     * Статус записи
     */
    private String recordStatus;
    /**
     * Идентификатор витрины
     */
    private String vin;
    /**
     * Номер шасси (рамы)
     */
    private String chassisNumber;
    /**
     * Номер кузова (кабины)
     */
    private String carcaseNumber;
    /**
     * Тип транспортного средства
     */
    private String vehicleType;
    /**
     * Модель двигателя
     */
    private String engineModel;
    /**
     * Номер двигателя
     */
    private String engineNum;
    /**
     * Объем двигателя (куб.см)
     */
    private String engineVolume;
    /**
     * Мощность л.с.
     */
    private String enginePowerHorse;
    /**
     * Мощность (кВт)
     */
    private String enginePowerVt;
    /**
     * Экологический класс
     */
    private String ecologyClass;
    /**
     * Экологический класс, описание значения
     */
    private String ecologyClassDesc;
    /**
     * Последнее регистрационное действие
     */
    private String lastRegActionName;
    /**
     * Сведения об ограничениях
     */
    private List<RestrictionsInformation> restrictions = new ArrayList<>();
    /**
     * Периоды владения
     */
    private List<OwnerPeriod> ownerPeriods = new ArrayList<>();
}
