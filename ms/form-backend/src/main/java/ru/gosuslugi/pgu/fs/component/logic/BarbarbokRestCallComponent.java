package ru.gosuslugi.pgu.fs.component.logic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.service.BackRestCallService;

import java.util.Map;

import static ru.gosuslugi.pgu.components.ComponentAttributes.URL_ATTR;

/**
 * Компонент запроса внешних данных из барбарбока с конвертированием в драфт-конвертере
 * Ответ сохраняется в applicationAnswers
 * Используется для запросов в барбарбок с конвертировнием через mv-шаблон (smev-converter)
 */
@Component
public class BarbarbokRestCallComponent extends BackRestCallComponent {

    private final String smevConverterUrl;

    public BarbarbokRestCallComponent(
            RestCallComponent restCallComponent,
            BackRestCallService backRestCallService,
            UserPersonalData userPersonalData,
            @Value("${pgu.smev-converter-url}") String smevConverterUrl
    ) {
        super(restCallComponent, backRestCallService, userPersonalData);
        this.smevConverterUrl = smevConverterUrl;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        component.getAttrs().putAll(
                Map.of(
                        URL_ATTR, smevConverterUrl,
                        ESIA_AUTH_ATTR, true
                )
        );

        return super.getInitialValue(component, scenarioDto);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.BarbarbokRestCall;
    }
}
