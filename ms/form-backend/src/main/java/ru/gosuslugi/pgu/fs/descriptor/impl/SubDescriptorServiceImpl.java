package ru.gosuslugi.pgu.fs.descriptor.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.components.descriptor.SubServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;
import ru.gosuslugi.pgu.fs.descriptor.SubDescriptorService;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubDescriptorServiceImpl implements SubDescriptorService {

    Map<String, ServiceDescriptor> serviceSubDescriptorMap = new HashMap<>();
    private final List<ServiceDescriptor> internalDescriptors;
    private final ComponentService componentService;

    @PostConstruct
    public void init() {
        for (ServiceDescriptor serviceDescriptor: internalDescriptors) {
            applyNewServiceDescriptor(serviceDescriptor.getServiceName(), serviceDescriptor);
        }
    }

    @Override
    public void presetFieldsForCurrentStep(String serviceId, ScenarioDto scenarioDto) {
        ServiceDescriptor serviceDescriptor = getServiceDescriptor(serviceId);
        if (serviceDescriptor == null) {
            return;
        }
        List<ComponentType> fieldTypesTpPreset = ((SubServiceDescriptor) serviceDescriptor).getPresetFieldTypes();
        if (fieldTypesTpPreset == null) {
            return;
        }
        for (ComponentType fieldType: fieldTypesTpPreset) {
            setFieldValueForDisplay(fieldType ,scenarioDto, serviceDescriptor);
        }

    }

    private void setFieldValueForDisplay(ComponentType fieldType, ScenarioDto scenarioDto, ServiceDescriptor descriptor){
        List<FieldComponent> displayComponents = scenarioDto.getDisplay().getComponents();
        Optional<FieldComponent> fieldComponentBox = displayComponents.stream().filter(c -> c.getType().equals(fieldType)).findFirst();
        fieldComponentBox.ifPresent(c -> {
            FieldComponent filledComponent = componentService.processField(c, scenarioDto, descriptor);
            int replaceIndex = displayComponents.indexOf(c);
            displayComponents.set(replaceIndex, filledComponent);
        });
    }

    @Override
    public void applyNewServiceDescriptor(String serviceId, ServiceDescriptor serviceDescriptor) {
        serviceSubDescriptorMap.put(serviceId, serviceDescriptor);
    }

    @Override
    public ServiceDescriptor getServiceDescriptor(String serviceId) {
        return serviceSubDescriptorMap.get(serviceId);
    }
}
