package ru.gosuslugi.pgu.fs.pgu.client;

import java.util.Set;

/**
 * Клиент для получения полномочий в рамках юр.лиц
 */
public interface PguEmpowermentClientV2 {
    Set<String> getUserEmpowerment(String token, Long oid);
}