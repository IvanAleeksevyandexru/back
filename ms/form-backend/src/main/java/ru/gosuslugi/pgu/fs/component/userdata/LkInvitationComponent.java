package ru.gosuslugi.pgu.fs.component.userdata;

import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;

@Component
public class LkInvitationComponent extends AbstractCycledComponent<String> {

    @Override
    public ComponentType getType() {
        return ComponentType.LkInvitationInput;
    }
}
