package ru.gosuslugi.pgu.fs.component.payment.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class CommonDataBox<T, R> {
    private FieldComponent component;
    private ScenarioDto scenario;
    private Map<String, String> incorrectAnswers;
    private Map.Entry<String, ApplicantAnswer> entry;
    private String serviceId;
    private final T elementT;
    private final R elementR;

    public CommonDataBox<T, R> withComponent(FieldComponent component) {
        this.component = component;
        return this;
    }

    public CommonDataBox<T, R> withScenario(ScenarioDto scenario) {
        this.scenario = scenario;
        return this;
    }

    public CommonDataBox<T, R> withIncorrectAnswers(Map<String, String> incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
        return this;
    }

    public CommonDataBox<T, R> withEntry(Map.Entry<String, ApplicantAnswer> entry) {
        this.entry = entry;
        return this;
    }

    public CommonDataBox<T, R> withServiceId(String stringValue) {
        this.serviceId = stringValue;
        return this;
    }
}
