package ru.gosuslugi.pgu.common.sop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.sop.exception.SopExternalException;
import ru.gosuslugi.pgu.common.core.service.HealthHolder;
import ru.gosuslugi.pgu.common.core.service.dto.DictionayHealthDto;
import ru.gosuslugi.pgu.common.sop.dto.SopDictionaryErrorInfo;
import ru.gosuslugi.pgu.common.sop.dto.SopDictionaryRequest;
import ru.gosuslugi.pgu.common.sop.dto.SopDictionaryResponse;
import ru.gosuslugi.pgu.common.sop.service.SopDictionaryService;
import ru.gosuslugi.pgu.common.sop.service.SopUid;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Slf4j
public class SopDictionaryServiceImpl implements SopDictionaryService {

    @Value("${pgu.dictionary-sop-url}")
    private String pguUrl;

    @Value("${pgu.dictionary-sop-api-key}")
    private String pguHeader;

    private static final String DICTIONARY_RESOURCE_URL = "cps-data-api/api/v1/data";

    private static final String API_KEY = "Api-Key";

    private final RestTemplate restTemplate;
    private final HealthHolder healthHolder;

    @Override
    public SopDictionaryResponse findByRequest(SopDictionaryRequest sopDictionaryRequest, String okato) {
        String sourceUid = sopDictionaryRequest.getSourceUid();
        SopUid sopUid = SopUid.getSopElementByUid(sourceUid);

        ResponseEntity<SopDictionaryResponse> result;
        String url = String.format("%s%s", pguUrl, DICTIONARY_RESOURCE_URL);
        String dictionaryName = sopUid.name();
        if (SopUid.UNKNOWN.equals(sopUid)) {
            throw new SopExternalException(dictionaryName, url, HttpMethod.POST, "Незарегистрированное имя справочника", okato);
        }
        try {
           result = restTemplate
                    .exchange(url,
                            HttpMethod.POST,
                            new HttpEntity<>(sopDictionaryRequest, prepareSecurityHeader()),
                            SopDictionaryResponse.class
                    );
        } catch (EntityNotFoundException | ExternalServiceException e) {
            throw new SopExternalException(dictionaryName, url, HttpMethod.POST, "Исключение при вызове метода", okato, e);
        }

        // validation
        validateResponse(result);
        handleError(result, dictionaryName, url, HttpMethod.POST, okato);
        healthHolder.addDictionaryHealth(new DictionayHealthDto(dictionaryName, url, HttpMethod.POST, HttpStatus.OK, null, null, okato));

        return result.getBody();
    }

    private void validateResponse(ResponseEntity<SopDictionaryResponse> responseEntity) {
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
            return;
        }
        if (log.isWarnEnabled()) {
            log.warn("Получена ошибка от Api {}; Код ответа = {}; Тело ответа = {}", pguUrl,
                    responseEntity.getStatusCodeValue(), responseEntity.getBody());
        }
    }

    private static void handleError(ResponseEntity<SopDictionaryResponse> responseEntity, String id, String url, HttpMethod method, String okato) {
        if (isNull(responseEntity)) {
            String message = "При получении справочника сервис не вернул результат. dictionaryName = " + id;
            if (log.isInfoEnabled()) {
                log.info(message);
            }
            throw new SopExternalException(id, url, method, "При получении справочника сервис не вернул результат", okato);
        }

        List<SopDictionaryErrorInfo> errors = Optional.ofNullable(responseEntity).map(entity -> entity.getBody().getErrors()).orElse(null);
        if(!CollectionUtils.isEmpty(errors)) {
            if (log.isInfoEnabled()) {
                String errorMessage = errors.stream()
                        .filter(error -> error.getCode() == null || error.getCode() != 0)
                        .map(SopDictionaryErrorInfo::toString).collect(Collectors.joining());
                log.info("Не удалось получить справочник. dictionaryName = {}, код = {}, сообшение = \"{}\"", id, errors.get(0).getCode(), errorMessage);
            }
            throw new SopExternalException(id, url, method, "Не удалось получить справочник. код = " + errors.get(0).getCode(), okato);
        }
    }

    /** Метод создания key-заголовка для доступа к ресурсам СОП. */
    private HttpHeaders prepareSecurityHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(API_KEY, pguHeader);
        return httpHeaders;
    }
}
