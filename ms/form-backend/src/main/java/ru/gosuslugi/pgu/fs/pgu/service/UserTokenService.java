package ru.gosuslugi.pgu.fs.pgu.service;


import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;

/**
 * Сервис для получения токена пользователя из лк
 */
public interface UserTokenService {

    /**
     * Достаёт токен пользователя из ЛК по id пользователя
     * нужен в случае если мы по какой то причине не можем достать данные из {@link UserPersonalData}
     * @param userId id пользователя
     * @return токен авторизации пользователя(может быть не активным)
     */
    String getUserToken(Long userId);
}
