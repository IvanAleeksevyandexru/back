package ru.gosuslugi.pgu.pgu_common.nsi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.exception.NsiExternalException;
import ru.gosuslugi.pgu.common.core.service.HealthHolder;
import ru.gosuslugi.pgu.common.core.service.OkatoHolder;
import ru.gosuslugi.pgu.common.core.service.dto.DictionayHealthDto;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.common.core.exception.dto.ExternalError;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiSuggestDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiSuggestDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterSimple;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiFilterCondition;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiSimpleDictionaryFilterContainer;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;
import ru.gosuslugi.pgu.pgu_common.nsi.util.NsiDictionaryFilterRequestUtil;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Slf4j
public class NsiDictionaryServiceImpl implements NsiDictionaryService {

    @Value("${pgu.dictionary-url}")
    private String pguUrl;

    @Value("${mock.url}")
    private String mockUrl;

    @Value("${mock.enabled}")
    private boolean mockEnabled;

    @Value("${mock.host}")
    private String mockedHost;

    private static final String DICTIONARY_RESOURCE_URL = "api/nsi/v1/dictionary";
    private static final String GEPS_DICTIONARY_NAME = "533_OSS_OPERATORS";
    private static final String DICTIONARY_NSI_SUGGEST_RESOURCE_URL = "api/nsi-suggest/v1/";

    private final RestTemplate restTemplate;
    private final HealthHolder healthHolder;
    private final OkatoHolder okatoHolder;

    @Override
    public Optional<NsiDictionaryItem> getDictionaryItemByValue(String dictionaryName, String attributeName, String attributeValue) {
        NsiSimpleDictionaryFilterContainer filter = new NsiSimpleDictionaryFilterContainer();
        NsiDictionaryFilterSimple simpleFilter =
                new NsiDictionaryFilterSimple.Builder()
                        .setAttributeName(attributeName)
                        .setStringValue(attributeValue)
                        .setCondition(NsiFilterCondition.EQUALS.toString()).build();
        filter.setSimple(simpleFilter);
        NsiDictionaryFilterRequest requestBody = new NsiDictionaryFilterRequest.Builder()
                .setFilter(filter)
                .setPageNum("1")
                .setPageSize("1")
                .build();

        return getDictionary(dictionaryName, requestBody).getItem(attributeValue);
    }

    @Override
    public NsiDictionary getDictionary(String dictionaryName, NsiDictionaryFilterRequest filterRequest) {
        NsiDictionary result;

        String okato = getOkato(filterRequest);
        String url = String.format("%s/%s/%s", pguUrl, DICTIONARY_RESOURCE_URL, dictionaryName);
        try {
            result = restTemplate.postForObject(url, filterRequest, NsiDictionary.class);
        } catch (EntityNotFoundException | ExternalServiceException e) {
            throw new NsiExternalException(dictionaryName, DICTIONARY_RESOURCE_URL + "/" + dictionaryName, HttpMethod.POST, "Исключение при вызове метода", okato, e);
        }

        // validation
        handleError(result, dictionaryName, DICTIONARY_RESOURCE_URL + "/" + dictionaryName, HttpMethod.POST, okato);
        healthHolder.addDictionaryHealth(new DictionayHealthDto(dictionaryName, url, HttpMethod.GET, HttpStatus.OK, null, null, okato));
        return result;
    }

    @Override
    public NsiDictionary getDictionaryItemForMapsByFilter(String dictionaryName, NsiDictionaryFilterRequest filterRequest) {
        return getDictionaryItemForMapsByFilter(dictionaryName, filterRequest, new HttpHeaders());
    }

    @Override
    public NsiDictionary getDictionaryItemForMapsByFilter(String dictionaryName, NsiDictionaryFilterRequest filterRequest, HttpHeaders headers) {
        NsiDictionary result;

        String okato = getOkato(filterRequest);
        String url = String.format("%s/%s/%s", pguUrl, DICTIONARY_RESOURCE_URL, dictionaryName);

        HttpEntity<NsiDictionaryFilterRequest> entity = new HttpEntity<>(filterRequest, headers);
        if (mockEnabled) {
            url =  String.format("%s/%s/%s", mockUrl, DICTIONARY_RESOURCE_URL, dictionaryName);
            headers.setHost(InetSocketAddress.createUnresolved(mockedHost, 80));
            entity = new HttpEntity<>(filterRequest, headers);
        }

        try {
            result = restTemplate.postForObject(url, entity, NsiDictionary.class, new HashMap<>());
        } catch (EntityNotFoundException | ExternalServiceException e) {
            throw new NsiExternalException(dictionaryName, url, HttpMethod.POST, "Исключение при вызове метода", okato, e);
        }

        // validation
        handleError(result, dictionaryName, url, HttpMethod.POST, okato);
        healthHolder.addDictionaryHealth(new DictionayHealthDto(dictionaryName, url, HttpMethod.GET, HttpStatus.OK, null, null, okato));
        return result;
    }

    @Override
    @Async
    public Future<NsiDictionary> asyncGetDictionaryItemForMapsByFilter(String dictionaryName, NsiDictionaryFilterRequest filterRequest) {
        NsiDictionary result;

        String okato = getOkato(filterRequest);
        String url = String.format("%s/%s/%s", pguUrl, DICTIONARY_RESOURCE_URL, dictionaryName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-PRELOAD", "1");
        HttpEntity<NsiDictionaryFilterRequest> entity = new HttpEntity<>(filterRequest, headers);
        if(mockEnabled){
            url =  String.format("%s/%s/%s", mockUrl, DICTIONARY_RESOURCE_URL, dictionaryName);
            headers.setHost(InetSocketAddress.createUnresolved(mockedHost, 80));
            entity = new HttpEntity<>(filterRequest, headers);
        }
        try {
            result = restTemplate.postForObject(url, entity, NsiDictionary.class, new HashMap<>());
        } catch (EntityNotFoundException | ExternalServiceException e) {
            throw new NsiExternalException(dictionaryName, url, HttpMethod.POST, "Исключение при вызове метода", okato, e);
        }

        // validation
        handleError(result, dictionaryName, url, HttpMethod.POST, okato);
        healthHolder.addDictionaryHealth(new DictionayHealthDto(dictionaryName, url, HttpMethod.GET, HttpStatus.OK, null, null, okato));
        return new AsyncResult(result);
    }

    private void handleError(NsiDictionary result, String id, String url, HttpMethod method, String okato) {
        if (isNull(result)) {
            String message = "При получении справочника сервис не вернул результат. dictionaryName = " + id;
            if (log.isInfoEnabled()) {
                log.info(message);
            }
            throw new NsiExternalException(id, url, method, "При получении справочника сервис не вернул результат", okato);
        }

        Optional<ExternalError> error = Optional.ofNullable(result).map(NsiDictionary::getError);
        Integer errorCode = error.map(ExternalError::getCode).orElse(null);
        if (isNull(errorCode) || errorCode != 0) {
            if (log.isInfoEnabled()) {
                String errorMessage = error.map(ExternalError::getMessage).orElse(null);
                log.info("Не удалось получить справочник. dictionaryName = {}, код = {}, сообшение = \"{}\"", id, errorCode, errorMessage);
            }
            throw new NsiExternalException(id, url, method, "Не удалось получить справочник. код = " + errorCode, okato);
        }

        String total = Optional.ofNullable(result).map(NsiDictionary::getTotal).orElse(null);
        Integer totalInteger;
        try {
            totalInteger = Integer.valueOf(total);
        } catch (NumberFormatException ex) {
            throw new NsiExternalException(id, url, method, "Проверка total. Формат total не соответствует ожидаемому. total = \"" + total + "\"", okato);
        }
        if (totalInteger < 0) {
            throw new NsiExternalException(id, url, method, "Проверка total. Значение total не соответствует ожидаемому. total = " + totalInteger + "", okato);
        }
    }

    /**
     * Возвращает не пустое ОКАТО
     * @param filterRequest филтровый реквест
     * @return не пустое ОКАТО
     */
    private String getOkato(NsiDictionaryFilterRequest filterRequest) {
        return Optional.ofNullable(NsiDictionaryFilterRequestUtil.getOkato(filterRequest)).orElseGet(() -> okatoHolder.getOkato());
    }


    @Override
    public NsiDictionary getGepsDictionary() {
        return this.getDictionary(GEPS_DICTIONARY_NAME,prepareGepsFilterRequest());
    }

    private NsiDictionaryFilterRequest prepareGepsFilterRequest(){
        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        filterRequest.setTreeFiltering("ONELEVEL");
        filterRequest.setPageNum("1");
        filterRequest.setPageSize("10000");
        filterRequest.setParentRefItemValue("");
        filterRequest.setSelectAttributes(List.of("*"));
        return filterRequest;
    }

    @Override
    public Map<String, Object> getNsiDictionaryItemByValue(String dictionaryName, String attributeName, String attributeValue) {
        NsiSimpleDictionaryFilterContainer filter = new NsiSimpleDictionaryFilterContainer();
        NsiDictionaryFilterSimple simpleFilter =
                new NsiDictionaryFilterSimple.Builder()
                        .setAttributeName(attributeName)
                        .setStringValue(attributeValue)
                        .setCondition(NsiFilterCondition.EQUALS.toString()).build();
        filter.setSimple(simpleFilter);
        NsiDictionaryFilterRequest requestBody = new NsiDictionaryFilterRequest.Builder()
                .setFilter(filter)
                .setPageNum("1")
                .setPageSize("1")
                .build();
        NsiSuggestDictionary nsiSuggestDictionary = getNsiSuggestDictionary(dictionaryName, requestBody);
        return nsiSuggestDictionary.getItem();
    }


    @Override
    public NsiSuggestDictionary getNsiSuggestDictionary(String dictionaryName, NsiDictionaryFilterRequest filterRequest) {
        NsiSuggestDictionary result;
        String okato = getOkato(filterRequest);
        String url = pguUrl + DICTIONARY_NSI_SUGGEST_RESOURCE_URL + dictionaryName;
        try {
            result = restTemplate.postForObject(url, filterRequest, NsiSuggestDictionary.class);
        } catch (EntityNotFoundException | ExternalServiceException e) {
            throw new NsiExternalException(dictionaryName, url, HttpMethod.POST, "Исключение при вызове метода", okato, e);
        }
        // validation
        handleNsiSuggestError(result, dictionaryName, DICTIONARY_NSI_SUGGEST_RESOURCE_URL + dictionaryName, HttpMethod.POST, okato);
        return result;
    }

    private void handleNsiSuggestError(NsiSuggestDictionary result, String id, String url, HttpMethod method, String okato) {
        if (isNull(result)) {
            String message = "При получении справочника сервис не вернул результат. dictionaryName = " + id;
            if (log.isInfoEnabled()) {
                log.info(message);
            }
            throw new NsiExternalException(id, url, method, "При получении справочника сервис не вернул результат", okato);
        }
        Optional<ExternalError> error = Optional.ofNullable(result).map(NsiSuggestDictionary::getError);
        Integer errorCode = error.map(ExternalError::getCode).orElse(null);
        if (error.isPresent()) {
            if (log.isInfoEnabled()) {
                String errorMessage = error.map(ExternalError::getMessage).orElse(null);
                log.info("Не удалось получить справочник. dictionaryName = {}, код = {}, сообшение = \"{}\"", id, errorCode, errorMessage);
            }
            throw new NsiExternalException(id, url, method, "Не удалось получить справочник. код = " + errorCode, okato);
        }
        String total = Optional.ofNullable(result).map(NsiSuggestDictionary::getTotal).orElse(null);
        Integer totalInteger;
        try {
            totalInteger = Integer.valueOf(total);
        } catch (NumberFormatException ex) {
            throw new NsiExternalException(id, url, method, "Проверка total. Формат total не соответствует ожидаемому. total = \"" + total + "\"", okato);
        }
        if (totalInteger < 0) {
            throw new NsiExternalException(id, url, method, "Проверка total. Значение total не соответствует ожидаемому. total = " + totalInteger + "", okato);
        }
    }
}
