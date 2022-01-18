package ru.gosuslugi.pgu.fs.component.esep.model;

public enum EsepSignCheckResult {
    SIGNED,
    NOT_SIGNED,
    NOT_SIGNED_BY_USER_SIGNATURE,    // подписано чужой подписью
    NOT_SIGNED_EMPTY_ORG_DATA    // нет данных об организации
}
