package ru.gosuslugi.pgu.fs.component.logic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.SmevConverterGetRequestDto;
import ru.gosuslugi.pgu.dto.SmevConverterPullRequestDto;
import ru.gosuslugi.pgu.dto.SmevConverterPushRequestDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.service.BackRestCallService;
import ru.gosuslugi.pgu.fs.service.RestCallService;

import java.util.Map;

import static ru.gosuslugi.pgu.components.ComponentAttributes.BODY_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.METHOD_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.PATH_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SERVICE_ID_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.URL_ATTR;

/**
 * Компонент запроса внешних данных из барбарбока с конвертированием в драфт-конвертере
 * Ответ сохраняется в applicantAnswers.component_id.response
 * Используется для запросов в барбарбок с конвертировнием через vm-шаблон (smev-converter)
 */
@Component
public class BarbarbokRestCallComponent extends BackRestCallComponent {

    private final String smevConverterUrl;

    public BarbarbokRestCallComponent(
            RestCallService restCallService,
            BackRestCallService backRestCallService,
            UserPersonalData userPersonalData,
            @Value("${pgu.smev-converter-url}") String smevConverterUrl
    ) {
        super(restCallService, backRestCallService, userPersonalData);
        this.smevConverterUrl = smevConverterUrl;
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        prepareAttrs(component, scenarioDto);
        super.preProcess(component, scenarioDto);
    }

    private void prepareAttrs(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> attrs = component.getAttrs();
        attrs.putAll(
                Map.of(
                        SERVICE_ID_ATTR, scenarioDto.getServiceCode(),
                        METHOD_ATTR, RequestMethod.POST,
                        URL_ATTR, smevConverterUrl,
                        ESIA_AUTH_ATTR, true
                )
        );

        if (attrs.containsKey("requestId")) {
            setBodyDto(attrs, SmevConverterPullRequestDto.class);
            attrs.put(PATH_ATTR, "/services/pull");
            return;
        }

        if (attrs.containsKey("templateName")) {
            setBodyDto(attrs, SmevConverterGetRequestDto.class);
            attrs.put(PATH_ATTR, "/services/get");
            return;
        }

        setBodyDto(attrs, SmevConverterPushRequestDto.class);
        attrs.put(PATH_ATTR, "/services/push");
    }

    private <T> void setBodyDto(Map<String, Object> attrs, Class<T> converterRequestDto) {
        attrs.put(BODY_ATTR, JsonProcessingUtil.toJson(objectMapper.convertValue(attrs, converterRequestDto)));
    }

    @Override
    public ComponentType getType() {
        return ComponentType.BarbarbokRestCall;
    }
}