package ru.gosuslugi.pgu.fs.component.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.BackRestCallResponseDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.service.BackRestCallService;

import java.util.HashMap;
import java.util.Map;

import static ru.gosuslugi.pgu.components.ComponentAttributes.COOKIES_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.HEADERS_ATTR;
import static ru.gosuslugi.pgu.fs.service.impl.BackRestCallServiceImpl.SQL_RESULT_OPTION;

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
    // Признак (boolean), нужно ли в реквест добавить header Authorization с значением "Bearer {acc_t}"
    public static final String BEARER_AUTH_ATTR = "bearer_auth";

    private final RestCallComponent restCallComponent;
    private final BackRestCallService backRestCallService;
    private final UserPersonalData userPersonalData;

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        var responseDto = getResponse(component);
        scenarioDto.getApplicantAnswers().put(
                component.getId(),
                new ApplicantAnswer(true, jsonProcessingService.toJson(responseDto))
        );
        clearComponent(component);
    }

    public BackRestCallResponseDto getResponse(FieldComponent component) {
        setUserToken(component);
        var restCallDto = restCallComponent.getInitialValue(component).get();

        if (component.getBooleanAttr(SQL_RESULT_OPTION)) {
            backRestCallService.setOption(SQL_RESULT_OPTION);
        }
        return backRestCallService.sendRequest(restCallDto);
    }

    private void setUserToken(FieldComponent component) {
        if (Boolean.TRUE.equals(component.getAttrs().getOrDefault(ESIA_AUTH_ATTR, Boolean.FALSE))) {
            if (!component.getAttrs().containsKey(COOKIES_ATTR)) {
                component.getAttrs().put(COOKIES_ATTR, new HashMap<String, String>());
            }
            ((Map<String, String>) component.getAttrs().get(COOKIES_ATTR)).put("acc_t", userPersonalData.getToken());
        }
        if (Boolean.TRUE.equals(component.getAttrs().getOrDefault(BEARER_AUTH_ATTR, Boolean.FALSE))) {
            ((Map<String, String>) component.getAttrs().get(HEADERS_ATTR)).put("Authorization", "Bearer " + userPersonalData.getToken());
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
