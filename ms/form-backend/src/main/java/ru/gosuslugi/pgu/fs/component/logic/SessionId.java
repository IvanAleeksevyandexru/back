package ru.gosuslugi.pgu.fs.component.logic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.logic.model.SessionIdDto;
import java.util.UUID;

/* Logic Компонент который записывает в свое value значение sessionId и кладет его в ApplicantAnswers.
Работает без фронтовой части
* */
@Component
@Slf4j
public class SessionId extends AbstractComponent<SessionIdDto> {

    @Override
    public ComponentType getType() {
        return ComponentType.SessionId;
    }

    @Override
    public ComponentResponse<SessionIdDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        var sessionId = UUID.randomUUID().toString();
        scenarioDto.getApplicantAnswers().put(component.getId(), new ApplicantAnswer(true,sessionId));

        SessionIdDto sessionIdDto = new SessionIdDto();
        sessionIdDto.setVisited(true);
        sessionIdDto.setValue(sessionId);
        return ComponentResponse.of(sessionIdDto);
    }
}
