package ru.gosuslugi.pgu.fs.component.address;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;

@Slf4j
@Component
public class RegistrationLegalAddrReadOnlyComponent extends RegistrationLegalAddrComponent {

    public RegistrationLegalAddrReadOnlyComponent(UserOrgData userOrgData) {
        super(userOrgData);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.RegistrationLegalAddrReadOnly;
    }
}
