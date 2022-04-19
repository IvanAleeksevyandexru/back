package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

import java.util.Map;

public interface ScenarioDtoService {

    /**
     * Восстанавливает DTO из черновика и устанавливает начальный экран в зависимости от роли, стейджа и стадии
     * заполнения пользователем
     * @param scenarioDto сценарий сервиса форм
     * @param serviceId идентификатор услуги
     * @return true если сченарий был изменен и требует сохранения в сервисе черновиков
     */
    boolean prepareScenarioDto(ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor, String serviceId);

    /**
     * Восстанавливает DTO из черновика и устанавливает начальный экран в зависимости от роли, стейджа и стадии
     * заполнения пользователем
     * @param scenarioDto сценарий сервиса форм
     * @param serviceId идентификатор услуги
     * @param stage стейдж
     * @param role роль пользователя
     * @return true если сченарий был изменен и требует сохранения в сервисе черновиков
     */
    boolean prepareScenarioDto(ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor, String serviceId, String stage, ApplicantRole role);

    /**
     * Готовит scenarioDto для указанного экрана
     * @param serviceDescriptor {@link ServiceDescriptor} услуги
     * @param screenId id экрана для подготовки
     * @param scenarioDto {@link ScenarioDto} для подготовки
     * @return {@link ScenarioDto} с нужным экраном
     */
    ScenarioDto prepareScreenById(ServiceDescriptor serviceDescriptor, String screenId, ScenarioDto scenarioDto);
}
