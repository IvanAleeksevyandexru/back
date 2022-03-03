package ru.gosuslugi.pgu.fs.component.address;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.service.FullAddressService;

@Slf4j
@Component
public class RegistrationLegalAddrReadOnlyComponent extends RegistrationLegalAddrComponent {

    public RegistrationLegalAddrReadOnlyComponent(UserOrgData userOrgData, FullAddressService fullAddressService) {
        super(userOrgData, fullAddressService);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.RegistrationLegalAddrReadOnly;
    }
}
