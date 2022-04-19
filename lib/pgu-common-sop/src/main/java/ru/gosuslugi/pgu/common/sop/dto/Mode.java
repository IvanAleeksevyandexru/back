package ru.gosuslugi.pgu.common.sop.dto;

/**
 * Режим возврата полей справочника.
 * <ul>
 *     <li>INCLUDE – вернуть перечисленные атрибуты</li>
 *     <li>EXCLUDE – вернуть все имеющиеся в СОП атрибуты, кроме перечисленных</li>
 * </ul>
 **/
public enum Mode {
    /** Режим возврата перечисленных в {@link SopRequestProjection#getColumnUids()} атрибутов. */
    INCLUDE,
    /** Режим возврата всех имеющихся в справочнике атрибутов, кроме перечисленных в {@link SopRequestProjection#getColumnUids()}. */
    EXCLUDE
}
