package ru.gosuslugi.pgu.fs.component.logic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.SmevConverterRequestDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.service.BackRestCallService;

import java.util.Map;

import static ru.gosuslugi.pgu.components.ComponentAttributes.BODY_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.METHOD_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.PATH_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SERVICE_ID_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.URL_ATTR;

/**
 * Компонент запроса внешних данных из барбарбока с конвертированием в драфт-конвертере
 * Ответ сохраняется в applicationAnswers
 * Используется для запросов в барбарбок с конвертировнием через mv-шаблон (smev-converter)
 */
@Component
public class BarbarbokRestCallComponent extends BackRestCallComponent {

    private final String smevConverterUrl;

    public BarbarbokRestCallComponent(RestCallComponent restCallComponent,
                                      BackRestCallService backRestCallService,
                                      UserPersonalData userPersonalData,
                                      @Value("${pgu.smev-converter-url}") String smevConverterUrl) {
        super(restCallComponent, backRestCallService, userPersonalData);
        this.smevConverterUrl = smevConverterUrl;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {

        prepareAttrs(component, scenarioDto);
        return super.getInitialValue(component, scenarioDto);
    }

    private void prepareAttrs(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> attrs = component.getAttrs();
        attrs.put(SERVICE_ID_ATTR, scenarioDto.getServiceCode());
        attrs.putAll(Map.of(
                METHOD_ATTR, RequestMethod.POST,
                URL_ATTR, smevConverterUrl,
                PATH_ATTR, "/services/get",
                BODY_ATTR, JsonProcessingUtil.toJson(objectMapper.convertValue(attrs, SmevConverterRequestDto.class)),
                ESIA_AUTH_ATTR, true
        ));
    }

    @Override
    public ComponentType getType() {
        return ComponentType.BarbarbokRestCall;
    }
}
