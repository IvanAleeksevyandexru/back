package ru.gosuslugi.pgu.fs.service;

/**
 * Возвращает пользовательский ОКАТО
 * @see <a href="https://jira.egovdev.ru/browse/EPGUCORE-52512">EPGUCORE-52512</a>
 */
public interface PersonOkatoService {

    /**
     * Возвращает пользовательский ОКАТО
     * @return ОКАТО
     */
    String getOkato();
}
