package ru.gosuslugi.pgu.player.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.service.*;


@Service
public class LightweightCycledScreenServiceImpl extends AbstractCycledScreenService {

    private final MainDescriptorService mainDescriptorService;

    public LightweightCycledScreenServiceImpl(ComponentService componentService, JsonProcessingService jsonProcessingService,
                                              @Lazy ComponentRegistry componentRegistry, HelperScreenRegistry screenRegistry,
                                              RuleConditionService ruleConditionService, MainDescriptorService mainDescriptorService,
                                              ListComponentItemUniquenessService listComponentItemUniquenessService) {
        super(componentService, jsonProcessingService, componentRegistry, screenRegistry, ruleConditionService, listComponentItemUniquenessService);
        this.mainDescriptorService = mainDescriptorService;
    }

    @Override
    protected DescriptorService getDescriptorService(ScenarioRequest request) {
        return mainDescriptorService;
    }
}
