package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Сведение о владельце
 */
@Data
public class Owner {

    /** Фамилия */
    @Schema(description = "Фамилия")
    private String lastName;

    /** Имя */
    @Schema(description = "Имя")
    private String firstName;

    /** Отчество */
    @Schema(description = "Отчество")
    private String middleName;

    /** Дата рождения */
    @Schema(description = "Дата рождения")
    private String birthDay;

    /** Место рождения */
    @Schema(description = "Место рождения")
    private String birthPlace;

    /** ИНН (для ИП) */
    @Schema(description = "ИНН (для ИП)")
    private String inn;

    /** ОГРНИП */
    @Schema(description = "ОГРНИП")
    private String ogrnip;

    /** СНИЛС */
    @Schema(description = "СНИЛС")
    private String snils;

    /** Адрес регистрации */
    @Schema(description = "Адрес регистрации")
    private String regAddress;

    /** Наименование документа удостоверяющего личность */
    @Schema(description = "Наименование документа удостоверяющего личность")
    private String idDocumentType;

    /** Серия и номер документа */
    @Schema(description = "Серия и номер документа")
    private String documentNumSer;

    /** Серия и номер документа в отформатированном виде */
    @Schema(description = "Серия и номер документа в отформатированном виде")
    private String documentNumSerFormatted;

    /** Дата выдачи документа */
    @Schema(description = "Дата выдачи документа")
    private String documentDate;

    /** Кем выдан документ */
    @Schema(description = "Кем выдан документ")
    private String issueAgency;

}
