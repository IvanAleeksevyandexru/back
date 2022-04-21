package ru.gosuslugi.pgu.fs.common.service;

/**
 * Сервис хранения cookies пользователя. Устанавливаются в фильтрах пакета {@link ru.gosuslugi.pgu.fs.common.filter}
 */
public interface UserCookiesService {

    String getUserTimezone();

    void setUserTimezone(String timezone);

    String getUserSelectedRegion();

    void setUserSelectedRegion(String region);

}
