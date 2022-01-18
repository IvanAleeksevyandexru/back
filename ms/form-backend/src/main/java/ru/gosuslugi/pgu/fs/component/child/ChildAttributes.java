package ru.gosuslugi.pgu.fs.component.child;

/**
 * Класс полей компонента подтверждения данных ребенка
 */
enum ChildAttributes {
    /** Имя */
    CHILDREN_FIRST_NAME_ATTR("firstName"),
    /** Фамилия */
    CHILDREN_LAST_NAME_ATTR("lastName"),
    /** Отчество */
    CHILDREN_MIDDLE_NAME_ATTR("middleName"),
    /** Дата рождения */
    CHILDREN_BIRTH_DATE_ATTR("birthDate"),
    /** Пол */
    CHILDREN_GENDER_ATTR("gender"),
    /** СНИЛС */
    CHILDREN_SNILS_ATTR("snils"),
    /** Серия свидетельства о рождении */
    CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR("rfBirthCertificateSeries"),
    /** Номер свидетельства о рождении */
    CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR("rfBirthCertificateNumber"),
    /** Номер актовой записи */
    CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR("rfBirthCertificateActNumber"),
    /** Дата выдачи свидетельства */
    CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR("rfBirthCertificateIssueDate"),
    /** Название органа выдавшего свидетельство */
    CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR("rfBirthCertificateIssuedBy"),
    /** Дата актовой записи */
    CHILDREN_ACT_DATE_ATTR("actDate"),

    /** номер полиса ОМС ребенка */
    CHILDREN_OMS_NUMBER_ATTR("omsNumber"),

    /** Серия полиса ОМС ребенка */
    CHILDREN_OMS_SERIES_ATTR("omsSeries");

    /** Ключ, название поля */
    public final String name;

    /**
     * Поле с необходимостью валидации
     * @param name - название поля
     */
    ChildAttributes(String name) {
        this.name = name;
    }
}
