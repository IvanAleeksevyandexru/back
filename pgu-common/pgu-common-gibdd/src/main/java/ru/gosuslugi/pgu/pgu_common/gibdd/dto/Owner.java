package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import lombok.Data;

/**
 * Сведение о владельце
 */
@Data
public class Owner {
    /** Фамилия */
    private String lastName;
    /** Имя */
    private String firstName;
    /** Отчество */
    private String middleName;
    /** Дата рождения */
    private String birthDay;
    /** Место рождения */
    private String birthPlace;
    /** ИНН (для ИП) */
    private String inn;
    /** ОГРНИП */
    private String ogrnip;
    /** СНИЛС */
    private String snils;
    /** Адрес регистрации */
    private String regAddress;
    /** Наименование документа удостоверяющего личность */
    private String idDocumentType;
    /** Серия и номер документа */
    private String documentNumSer;
    /** Серия и номер документа в отформатированном виде */
    private String documentNumSerFormatted;
    /** Дата выдачи документа */
    private String documentDate;
    /** Кем выдан документ */
    private String issueAgency;
}
