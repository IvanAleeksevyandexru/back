package ru.gosuslugi.pgu.fs.esia;

import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactDto;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactState;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaOmsDto;

public interface EsiaRestContactDataClient {

    EsiaContactState checkPhone(String phone);

    EsiaContactDto changeContact(EsiaContactDto esiaContactDto);

    EsiaContactDto addContact(EsiaContactDto esiaContactDto);

    Boolean resendCode(String contactId);

    Boolean confirmContact(String code);

    Boolean checkEmail(String email);

    /**
     * Проверка подтверждения емайла
     * @param esiaContactTypeCode ESIA код типа контакта
     * @param email емайл
     * @return true если емайл подтверждет (VERIFIED)
     */
    Boolean isEmailConfirmed(String esiaContactTypeCode, String email);

    /**
     * Добавление/изменение адреса пользователя в ЛК
     * @see <a href="https://jira.egovdev.ru/browse/EPGUCORE-51164">EPGUCORE-51164</a>
     * @see <a href="https://confluence.egovdev.ru/pages/viewpage.action?pageId=185945393">Добавление/изменение адреса пользователя в ЛК - confluence</a>
     *
     * @param esiaAddress esia адресс
     * @return вернувшийся адресс из ESIA
     */
    EsiaAddress updateAddress(EsiaAddress esiaAddress);

    /**
     * Добавить контакт для организации
     * @param esiaContactDto контакт DTO
     * @return ответ контакта DTO
     */
    EsiaContactDto addLegalContact(EsiaContactDto esiaContactDto);

    /**
     * Изменить контакт для организации
     * @param esiaContactDto контакт DTO
     * @return ответ контакта DTO
     */
    EsiaContactDto changeLegalContact(EsiaContactDto esiaContactDto);

    /**
     * Изменить омс в ЕСИА
     * @param esiaOmsDto ОМС DTO
     * @return ответ ОМС DTO
     */
    EsiaOmsDto changeOms(EsiaOmsDto esiaOmsDto);


    /**
     * Повторная отправка контакта для организации
     * @param contactId идентификатор контакт
     * @return true если процесс отправки прошкл успешно
     */
    Boolean resendLegalCode(String contactId);
}
