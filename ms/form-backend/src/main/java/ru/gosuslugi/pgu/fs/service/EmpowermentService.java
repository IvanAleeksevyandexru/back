package ru.gosuslugi.pgu.fs.service;

import java.util.List;
import java.util.Set;

/**
 * Сервис для проверки полномочий пользователя на прохождение услуги.
 * Используется для сотрудников ЮЛ, ИП
 */
public interface EmpowermentService {

    /**
     * Проверяет, имеет ли пользователь полномочие для прохождения услуги.
     * @param empowermentId идентификатор полномочия
     * @return true если полномочие есть
     */
    boolean hasEmpowerment(List<String> empowermentId);

    Set<String> getUserEmpowerments();


    /**
     * Проверяет доступна ли услуга для пользователя для заполнения
     */
    boolean checkServiceAvailableForUser(String targetId);

    boolean checkUserUserPermissionForSendOrder(String targetId);
}
