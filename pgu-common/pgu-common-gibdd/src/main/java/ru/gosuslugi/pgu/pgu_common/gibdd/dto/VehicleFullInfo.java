package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Все данные о транспортном средстве из Витрины ГИБДД
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleFullInfo extends VehicleInfo {

    /** Уникальный номер записи */
    @Schema(description = "Уникальный номер записи")
    private String uniqueRowNumber;

    /** Идентификационный номер (VIN2) */
    @Schema(description = "Идентификационный номер (VIN2)")
    private String vin2;

    /** СТС - Серия и номер документа в отформатированном виде */
    @Schema(description = "СТС - Серия и номер документа в отформатированном виде")
    private String stsSeriesNumberFormatted;

    /** СТС - Тип документа (RegistrationDoc.RegDocumentType в версии витрины 1.4) */
    @Schema(description = "СТС - Тип документа (RegistrationDoc.RegDocumentType в версии витрины 1.4)")
    private String stsDocumentType;

    /** СТС - Дата регистрации (RegistrationDoc.RegDocDocumentDate в версии витрины 1.4) */
    @Schema(description = "СТС - Дата регистрации (RegistrationDoc.RegDocDocumentDate в версии витрины 1.4)")
    private String stsDocumentDate;

    /** СТС - Кем выдан регистрационный документ (RegistrationDoc.RegDocIssueAgency в версии витрины 1.4) */
    @Schema(description = "СТС - Кем выдан регистрационный документ (RegistrationDoc.RegDocIssueAgency в версии витрины 1.4)")
    private String stsIssueAgency;

    /** СТС - Особые отметки в регистрационном документе (RegistrationDoc.RegDocSpecialTags в версии витрины 1.4) */
    @Schema(description = "СТС - Кем выдан регистрационный документ (RegistrationDoc.RegDocIssueAgency в версии витрины 1.4)")
    private String stsSpecialTags;

    /** ПТС - Серия документа в отформатированном виде */
    @Schema(description = "ПТС - Серия документа в отформатированном виде")
    private String ptsNumFormatted;

    /** ПТС - Дата регистрации */
    @Schema(description = "ПТС - Дата регистрации")
    private String ptsRegDate;

    /** ПТС - Кем выдан регистрационный документ */
    @Schema(description = "ПТС - Кем выдан регистрационный документ")
    private String ptsIssueAgency;

    /** ПТС - Особые отметки в регистрационном документе */
    @Schema(description = "ПТС - Особые отметки в регистрационном документе")
    private String ptsSpecialTags;

    /** Изготовитель */
    @Schema(description = "Изготовитель")
    private String manufacturer;

    /** Стоимость */
    @Schema(description = "Стоимость")
    private String price;

    /** Масса в снаряженном состоянии */
    @Schema(description = "Масса в снаряженном состоянии")
    private String maxWeight;

    /** Масса без нагрузки */
    @Schema(description = "Масса без нагрузки")
    private String weightWithoutLoading;

    /** Положение руля */
    @Schema(description = "Положение руля")
    private String wheelLocation;

    /** Положение руля, описание (1-левый, 2-правый) */
    @Schema(description = "Положение руля, описание (1-левый, 2-правый)")
    private String wheelLocationDesc;

    /** Тип коробки передач */
    @Schema(description = "Тип коробки передач")
    private String transmissionType;

    /** Тип коробки передач, описание (1-МКПП, 2-АКПП) */
    @Schema(description = "Тип коробки передач, описание (1-МКПП, 2-АКПП)")
    private String transmissionTypeDesc;

    /** Тип привода */
    @Schema(description = "Тип привода")
    private String driveUnitType;

    /** Тип привода, описание (1-передний, 2- задний) */
    @Schema(description = "Тип привода, описание (1-передний, 2- задний)")
    private String driveUnitTypeDesc;

    /** Категория (там. союз) */
    @Schema(description = "Категория (там. союз)")
    private String vehicleTypeTAM;

    /** Наименование спец. назначения */
    @Schema(description = "Наименование спец. назначения")
    private String specTargetName;

    /** Тип топлива */
    @Schema(description = "Тип топлива")
    private String engineType;

    /** Срок окончания регистрации */
    @Schema(description = "Срок окончания регистрации")
    private String endRegDate;

    /** Дата выдачи одобрения типа ТС */
    @Schema(description = "Дата выдачи одобрения типа ТС")
    private String approveDate;

    /** Серия и номер одобрения типа */
    @Schema(description = "Серия и номер одобрения типа")
    private String approveSerNum;

    /** Кем выдано одобрение типа */
    @Schema(description = "Кем выдано одобрение типа")
    private String approveIssueBy;

    /** Статус утилизационного сбора */
    @Schema(description = "Статус утилизационного сбора")
    private String utilizStatus;

    /** Таможенная декларация, таможенный приходный ордер */
    @Schema(description = "Таможенная декларация, таможенный приходный ордер")
    private String tdtpo;

    /** Дата выдачи */
    @Schema(description = "Дата выдачи")
    private String tdtpoIssueDate;

    /** Кем выдано */
    @Schema(description = "Кем выдано")
    private String tdtpoIssueBy;

    /** Таможенные ограничения */
    @Schema(description = "Таможенные ограничения")
    private String customsRestrictions;

    /** Код страна вывоза */
    @Schema(description = "Код страна вывоза")
    private String issueCountryCode;

    /** Наименование страны вывоза */
    @Schema(description = "Наименование страны вывоза")
    private String countryName;

    /** Регистрационные действия */
    @Schema(description = "Регистрационные действия")
    private List<RegAction> regActions = new ArrayList<>();

    /** Розыск спец продукции */
    @Schema(description = "Розыск спец продукции")
    private SearchingSpec searchingSpec;

    /** Сведение о владельце */
    @Schema(description = "Сведение о владельце")
    private Owner owner;

}
