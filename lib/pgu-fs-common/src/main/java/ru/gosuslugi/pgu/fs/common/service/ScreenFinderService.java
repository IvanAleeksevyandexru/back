package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ScreenRule;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

import java.util.List;
import java.util.Optional;

/**
 * Сервис, инкапсулирующий логику поиска экранов в соответствии со сценарием
 */
public interface ScreenFinderService {

    /**
     * Находит экран по бизнес-правилам
     * @param scenarioDto ДТО
     * @param serviceDescriptor описание услуги
     * @param screenRuleListSupplied бизнес-правила, если не заданы, то берутся из {@link ScenarioDto#getDisplay()}
     * @return экран по бизнес-правилам
     */
    ScreenDescriptor findScreenDescriptorByRules(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor, List<ScreenRule> screenRuleListSupplied);


    /**
     * Находит экран по бизнес-правилам
     * @param scenarioDto ДТО
     * @param serviceDescriptor описание услуги
     * @param screenRuleListSupplied бизнес-правила, если не заданы, то берутся из {@link ScenarioDto#getDisplay()}
     * @return экран по бизнес-правилам
     */
    Optional<ScreenDescriptor> findScreenDescriptorByRulesOrEmpty(
        ScenarioDto scenarioDto,
        ServiceDescriptor serviceDescriptor,
        List<ScreenRule> screenRuleListSupplied
    );

}
