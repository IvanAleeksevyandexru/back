package ru.gosuslugi.pgu.fs.component.snippets;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.service.SnippetsClient;

@Component
@RequiredArgsConstructor
public class SnippetsComponent extends AbstractComponent<String> {
    private final SnippetsClient client;

    @Override
    public ComponentType getType() {
        return ComponentType.Snippet;
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        String body = String.valueOf(component.getAttrs().get("body"));
        Long orderId = Long.parseLong(component.getAttrs().get("orderId").toString());
        String response = client.setCustomSnippet(orderId, body);
        scenarioDto.getApplicantAnswers().put(component.getId(), new ApplicantAnswer(true, response));
    }
}