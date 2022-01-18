package ru.gosuslugi.pgu.fs.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.service.*;
import ru.gosuslugi.pgu.fs.descriptor.SubDescriptorService;


@Service
public class CycledScreenServiceImpl extends AbstractCycledScreenService {

    private final MainDescriptorService mainDescriptorService;
    private final SubDescriptorService subDescriptorService;

    public CycledScreenServiceImpl(ComponentService componentService, JsonProcessingService jsonProcessingService,
                                   @Lazy ComponentRegistry componentRegistry, HelperScreenRegistry screenRegistry,
                                   RuleConditionService ruleConditionService, MainDescriptorService mainDescriptorService,
                                   SubDescriptorService subDescriptorService, ListComponentItemUniquenessService listComponentItemUniquenessService) {
        super(componentService, jsonProcessingService, componentRegistry, screenRegistry, ruleConditionService, listComponentItemUniquenessService);
        this.mainDescriptorService = mainDescriptorService;
        this.subDescriptorService = subDescriptorService;
    }

    @Override
    protected DescriptorService getDescriptorService(ScenarioRequest request) {
        return request.getIsInternalScenario() == null || !request.getIsInternalScenario() ? mainDescriptorService : subDescriptorService;
    }
}
