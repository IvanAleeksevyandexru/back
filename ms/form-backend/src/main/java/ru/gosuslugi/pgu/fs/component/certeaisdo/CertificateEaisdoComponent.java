package ru.gosuslugi.pgu.fs.component.certeaisdo;

import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

/**
 * Компонент для запроса данных о сертификате ДО
 */
@Component
public class CertificateEaisdoComponent extends AbstractComponent<String> {
    @Override
    public ComponentType getType() {
        return ComponentType.CertificateEaisdo;
    }


    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        return ComponentResponse.empty();
    }
}
