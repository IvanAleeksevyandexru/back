package ru.gosuslugi.pgu.components.descriptor;

import lombok.Data;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;

import java.util.List;

@Data
public class SubServiceDescriptor extends ServiceDescriptor {
    private List<ComponentType> presetFieldTypes;
}
