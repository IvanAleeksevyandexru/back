package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;

/**
 * Изменение и проверка подтверждения контактной информации для организации
 *
 * boolean isEmailIsAlreadyUsed(String email); Не имплементирован, так как несколько организаций вполне могут быть на одной электронной почте
 */
public interface OrgContactService {

    /**
     * Запрос нового подтверждения для email-а организации
     * @param scenarioDto внутренний сценарий
     */
    void resendEmailConfirmation(ScenarioDto scenarioDto);

    /**
     * Проверка подтверждения емайла
     * @param email емайл
     * @return true если емайл подтверждет (VERIFIED)
     */
    boolean isEmailConfirmedAndLinked(String email);

    /**
     * Добавление/обновление емайла организации
     * @param scenarioDto внутренний сценарий
     * @param email емайл
     */
    void updateEmail(ScenarioDto scenarioDto, String email);
}
