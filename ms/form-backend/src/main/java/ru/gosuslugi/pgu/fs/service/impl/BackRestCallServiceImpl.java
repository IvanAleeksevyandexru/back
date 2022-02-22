package ru.gosuslugi.pgu.fs.service.impl;

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

    @Override
    public BackRestCallResponseDto sendRequest(RestCallDto requestDto) {
        var headers = new HttpHeaders();
        requestDto.getHeaders().forEach(headers::add);
        if (headers.isEmpty()) {
            headers.add(ACCEPT, APPLICATION_JSON_VALUE);
            headers.setContentType(APPLICATION_JSON);
        }
        requestDto.getCookies().forEach((key, value) -> headers.add(HttpHeaders.COOKIE, key + "=\"" + value + "\""));

        try {
            ResponseEntity<Object> responseEntity = restClientRegistry.getRestTemplate(Optional.ofNullable(requestDto.getTimeout()).orElse(-1L).intValue())
                    .exchange(requestDto.getUrl(),
                            Objects.requireNonNull(HttpMethod.resolve(requestDto.getMethod())),
                            new HttpEntity<>(requestDto.getFormData().isEmpty() ? requestDto.getBody() : requestDto.getFormData(), headers),
                            Object.class
                    );
            return new BackRestCallResponseDto(responseEntity.getStatusCodeValue(), responseEntity.getBody());
        } catch (PguException e) {
            return new BackRestCallResponseDto(HttpStatus.NOT_FOUND.value(), e.getMessage());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String message = "";
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
