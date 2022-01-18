package ru.gosuslugi.pgu.fs.component.logic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.BODY_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.COOKIES_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.FORMDATA_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.HEADERS_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.METHOD_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.PATH_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.QUERY_PARAMETERS;
import static ru.gosuslugi.pgu.components.ComponentAttributes.TIMEOUT_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.URL_ATTR;
import static ru.gosuslugi.pgu.fs.utils.RequestComponentsUtil.getAttrAsMap;
import static ru.gosuslugi.pgu.fs.utils.RequestComponentsUtil.resolveArguments;
import static ru.gosuslugi.pgu.fs.utils.RequestComponentsUtil.resolveMapValueArguments;
import static ru.gosuslugi.pgu.fs.utils.RequestComponentsUtil.validateComponentAttrs;

/**
 * Компонент запроса внешних данных - на фронте вызвается rest запрос по данным сформированным из описания в JSON
 * Ответ сохраняется в ApplicationAnswers
 */
@Slf4j
@Component
public class RestCallComponent extends AbstractComponent<RestCallDto> {

    private final String restCallUrl;

    public RestCallComponent(@Value("${pgu.rest-call-url}") String restCallUrl) {
        this.restCallUrl = restCallUrl;
    }

    @Override
    public ComponentResponse<RestCallDto> getInitialValue(FieldComponent component) {
        validateComponentAttrs(List.of(METHOD_ATTR, PATH_ATTR), component.getAttrs());
        Map<String, Object> attr = component.getAttrs();
        Map<String, String> arguments = component.getArguments();

        RestCallDto result = new RestCallDto();
        result.setMethod(component.getAttrs().get(METHOD_ATTR).toString().toUpperCase());

        String link = Objects.toString(component.getAttrs().get(URL_ATTR), null);
        if (StringUtils.isEmpty(link)) {
            link = restCallUrl;
        }
        String url = link  + component.getAttrs().get(PATH_ATTR);
        if (attr.containsKey(QUERY_PARAMETERS)) {
            Map<String, String> queryParams = getAttrAsMap(QUERY_PARAMETERS, attr);
            if (!CollectionUtils.isEmpty(queryParams)) {
                url = url + "?" + queryParams.entrySet().stream()
                    .map(it -> it.getKey() + "=" + it.getValue())
                    .collect(Collectors.joining("&"));
            }
        }
        result.setUrl(resolveArguments(url, arguments));

        if (attr.containsKey(BODY_ATTR)) {
            result.setBody(resolveArguments(component.getAttrs().get(BODY_ATTR).toString(), arguments));
        }

        if (attr.containsKey(HEADERS_ATTR)) {
            result.setHeaders(resolveMapValueArguments(getAttrAsMap(HEADERS_ATTR, attr), arguments));
        }

        if (attr.containsKey(COOKIES_ATTR)) {
            result.setCookies(resolveMapValueArguments(getAttrAsMap(COOKIES_ATTR, attr), arguments));
        }

        if (attr.containsKey(FORMDATA_ATTR)) {
            Map<String, String> map = resolveMapValueArguments(getAttrAsMap(FORMDATA_ATTR, attr), arguments);
            result.getFormData().setAll(map);
        }

        if (attr.containsKey(TIMEOUT_ATTR)) {
            try {
                result.setTimeout(Long.valueOf(component.getAttrs().get(TIMEOUT_ATTR).toString()));
            } catch (NumberFormatException e) {
                log.error("Неверное значение таймаута: {}", component.getAttrs().get(TIMEOUT_ATTR).toString());
            }
        }

        return ComponentResponse.of(result);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.RestCall;
    }

}
