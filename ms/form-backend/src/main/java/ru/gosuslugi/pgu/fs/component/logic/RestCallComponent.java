package ru.gosuslugi.pgu.fs.component.logic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto;
import ru.gosuslugi.pgu.fs.service.RestCallService;

/**
 * Компонент запроса внешних данных - на фронте вызвается rest запрос по данным сформированным из описания в JSON
 * Ответ сохраняется в ApplicationAnswers
 */
@Slf4j
@Component
public class RestCallComponent extends AbstractComponent<RestCallDto> {

    private final String restCallUrl;
    private final RestCallService restCallService;

    public RestCallComponent(@Value("${pgu.rest-call-url}") String restCallUrl,
                             RestCallService restCallService) {
        this.restCallService = restCallService;
        this.restCallUrl = restCallUrl;
    }

    @Override
    public ComponentResponse<RestCallDto> getInitialValue(FieldComponent component) {
        return restCallService.fillRestCallDto(component, restCallUrl);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.RestCall;
    }

}
