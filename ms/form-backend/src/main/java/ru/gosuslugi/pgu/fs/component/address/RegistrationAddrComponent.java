package ru.gosuslugi.pgu.fs.component.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationAddrComponent extends AbstractFullAddressComponent<FullAddress> {

    @Override
    public String getFullAddressJsonPath() {
        // regAddr after root
        return "$['regAddr']";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.RegistrationAddr;
    }

}
