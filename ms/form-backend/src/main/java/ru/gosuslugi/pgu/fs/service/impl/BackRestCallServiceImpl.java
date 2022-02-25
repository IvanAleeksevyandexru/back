package ru.gosuslugi.pgu.fs.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.exception.PguException;
import ru.gosuslugi.pgu.dto.BackRestCallResponseDto;
import ru.gosuslugi.pgu.dto.SqlResponseDto;
import ru.gosuslugi.pgu.fs.component.RestClientRegistry;
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto;
import ru.gosuslugi.pgu.fs.service.BackRestCallService;

import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@Service
public class BackRestCallServiceImpl implements BackRestCallService {

    private final RestClientRegistry restClientRegistry;
    private final ObjectMapper objectMapper;
    public static final String SQL_RESULT_OPTION = "sqlResult";

    @Override
    public BackRestCallResponseDto sendRequest(RestCallDto requestDto) {
        var headers = new HttpHeaders();
        requestDto.getHeaders().forEach(headers::add);
        if (headers.isEmpty()) {
            headers.add(ACCEPT, APPLICATION_JSON_VALUE);
            headers.setContentType(APPLICATION_JSON);
        }
        requestDto.getCookies().forEach((key, value) -> headers.add(HttpHeaders.COOKIE, key + "=\"" + value + "\""));

        var entity = requestDto.getFormData().isEmpty() ? requestDto.getBody() : requestDto.getFormData();
        try {
            ResponseEntity<Object> responseEntity = restClientRegistry.getRestTemplate(Optional.ofNullable(requestDto.getTimeout()).orElse(-1L).intValue())
                    .exchange(requestDto.getUrl(),
                            Objects.requireNonNull(HttpMethod.resolve(requestDto.getMethod())),
                            new HttpEntity<>(entity, headers),
                            Object.class
                    );

            var body = OPTIONS.containsKey(SQL_RESULT_OPTION) && OPTIONS.get(SQL_RESULT_OPTION) == Boolean.TRUE
                    ? objectMapper.convertValue(responseEntity.getBody(), SqlResponseDto.class)
                    : responseEntity.getBody();

            return new BackRestCallResponseDto(responseEntity.getStatusCodeValue(), body);
        } catch (ExternalServiceException e) {
            log.error("Ошибка внешних данных: {} Entity: {}", e.getMessage(), entity);
            return new BackRestCallResponseDto(e.getStatus().value(), e.getMessage());
        }  catch (IllegalArgumentException e) {
            log.error("Преобразование данных: {}", entity);
            throw new PguException("Ошибка в преобразовании данных");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String message;
            if (REQUEST_TIMEOUT.equals(e.getStatusCode())) {
                log.error("Превышение времени ожидания: " + e.getMessage() + " => " + requestDto);
                message = "К сожалению, превышено время ожидания запроса и мы не получили необходимые сведения.";
            } else {
                log.error("Ошибка при обращении к внешнему сервису: " + e.getMessage() + " => " + requestDto);
                message = "К сожалению, произошла ошибка и мы не получили необходимые сведения из внешнего сервиса.";
            }

            return new BackRestCallResponseDto(e.getRawStatusCode(), message + " Пожалуйста, повторите попытку позже.", null);
        }
    }
}
