package ru.gosuslugi.pgu.fs.component.circles;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.component.logic.model.SessionIdDto;

@Component
@Slf4j
public class GroupListComponent extends AbstractComponent<SessionIdDto> {

    @Override
    public ComponentType getType() {
        return ComponentType.GroupList;
    }
}
