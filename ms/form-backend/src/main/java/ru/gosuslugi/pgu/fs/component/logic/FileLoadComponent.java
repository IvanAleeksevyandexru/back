package ru.gosuslugi.pgu.fs.component.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.logic.model.FileUploadDto;
import ru.gosuslugi.pgu.fs.exception.InnerDataError;
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient;
import ru.gosuslugi.pgu.terrabyte.client.model.FileType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.*;
import static ru.gosuslugi.pgu.fs.utils.RequestComponentsUtil.*;

/**
 * Логический компонент, отвечающий за загрузку файлов и сохранение на бэке
 * Загрузка и сохранение происходит на стороне бэка, с целью минимизации нагрузки на клиента
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FileLoadComponent extends AbstractComponent<FileUploadDto> {
    private final RestTemplate restTemplate;
    private final TerrabyteClient terrabyteClient;

    @Override
    public ComponentResponse<FileUploadDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        validateComponentAttrs(List.of(METHOD_ATTR, URL_ATTR, PATH_ATTR, FILENAME_ATTR, FILE_MIME_TYPE_ATTR), component.getAttrs());
        Map<String, Object> attr = component.getAttrs();
        Map<String, String> arguments = component.getArguments();

        String url = component.getAttrs().get(URL_ATTR).toString() + component.getAttrs().get(PATH_ATTR);
        if (attr.containsKey(QUERY_PARAMETERS)) {
            Map<String, String> queryParams = getAttrAsMap(QUERY_PARAMETERS, attr);
            if (!CollectionUtils.isEmpty(queryParams)) {
                url = url + "?" + queryParams.entrySet().stream()
                        .map(it -> it.getKey() + "=" + it.getValue())
                        .collect(Collectors.joining("&"));
            }
        }

        url = resolveArguments(url, arguments);

        HttpHeaders headers = new HttpHeaders();
        String body = null;


        if (attr.containsKey(BODY_ATTR)) {
            body = resolveArguments(component.getAttrs().get(BODY_ATTR).toString(), arguments);
        }

        if (attr.containsKey(HEADERS_ATTR)) {
            Map<String, String> componentHeaders = resolveMapValueArguments(getAttrAsMap(HEADERS_ATTR, attr), arguments);
            headers.addAll(new LinkedMultiValueMap(componentHeaders));
        }

        if (attr.containsKey(COOKIES_ATTR)) {
            headers.add(HttpHeaders.COOKIE, resolveArguments(component.getAttrs().get(COOKIES_ATTR).toString(), arguments));
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        HttpMethod requestMethod = null;

        try {
            requestMethod = HttpMethod.valueOf(component.getAttrs().get(METHOD_ATTR).toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InnerDataError("Некорректный метод при загрузке файлов");
        }

        ResponseEntity<byte[]> response = null;
        try {
            response = restTemplate.exchange(url, requestMethod, request, byte[].class);
        } catch (RestClientException e) {
            log.error("Error while downloading file for order id {} using url {} from component {}", scenarioDto.getOrderId(), url, component.getId(), e);
            throw new ExternalServiceException("Не удалось получить ответ от смежной системы при загрузке файла");
        }


        String fileName = component.getAttrs().get(FILENAME_ATTR).toString();
        String mimeType = component.getAttrs().get(FILE_MIME_TYPE_ATTR).toString();
        if (response.getStatusCode() == HttpStatus.OK) {
            terrabyteClient.internalSaveFile(response.getBody(), fileName, fileName, mimeType, scenarioDto.getOrderId(), FileType.ATTACHMENT.getType());
        } else {
            throw new ExternalServiceException("Не удалось получить ответ от смежной системы при загрузке файла");
        }

        return ComponentResponse.of(new FileUploadDto(fileName));
    }

    @Override
    public ComponentType getType() {
        return ComponentType.FileLoad;
    }

}
