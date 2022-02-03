package ru.gosuslugi.pgu.fs.component.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.BackRestCallResponseDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.SqlResponseDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.service.BackRestCallService;

import java.util.HashMap;
import java.util.Map;

import static ru.gosuslugi.pgu.components.ComponentAttributes.COOKIES_ATTR;

/**
 * Компонент запроса внешних данных - на бэке вызывается rest-запрос по данным из описания в JSON
 * Ответ сохраняется в applicationAnswers
 * Используется для непубличных запросов
 */
@Component
@RequiredArgsConstructor
public class BackRestCallComponent extends AbstractComponent<String> {

    // Признак (boolean), нужно ли в реквест добавить acc_t
    protected static final String ESIA_AUTH_ATTR = "esia_auth";

    private final RestCallComponent restCallComponent;
    private final BackRestCallService backRestCallService;
    private final UserPersonalData userPersonalData;

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {

        var responseDto = getResponse(component);
        if (component.getBooleanAttr("sqlResult")) {
            responseDto.setResponse(objectMapper.convertValue(responseDto.getResponse(), SqlResponseDto.class));
        }

        scenarioDto.getApplicantAnswers().put(
                component.getId(),
                new ApplicantAnswer(true, jsonProcessingService.toJson(responseDto))
        );
        clearComponent(component);
        return ComponentResponse.empty();
    }

    public BackRestCallResponseDto getResponse(FieldComponent component) {
        setUserToken(component);
        var restCallDto = restCallComponent.getInitialValue(component).get();
        return backRestCallService.sendRequest(restCallDto);
    }

    private void setUserToken(FieldComponent component) {
        if (component.getAttrs().containsKey(ESIA_AUTH_ATTR)
                && Boolean.TRUE.equals(component.getAttrs().getOrDefault(ESIA_AUTH_ATTR, Boolean.FALSE))) {
            if (!component.getAttrs().containsKey(COOKIES_ATTR)) {
                component.getAttrs().put(COOKIES_ATTR, new HashMap<String, String>());
            }
            ((Map<String, String>) component.getAttrs().get(COOKIES_ATTR)).put("acc_t", userPersonalData.getToken());
        }
    }

    // "Очищает" компонент, чтобы не передавать непубличные данные
    private void clearComponent(FieldComponent component) {
        if (component.getAttrs() != null) {
            component.getAttrs().clear();
        }
        if (component.getLinkedValues() != null) {
            component.getLinkedValues().clear();
        }
        component.getArguments().clear();
    }

    @Override
    public ComponentType getType() {
        return ComponentType.BackRestCall;
    }
}
