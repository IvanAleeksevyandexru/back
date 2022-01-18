package ru.gosuslugi.pgu.pgu_common.nsi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.exception.NsiExternalException;
import ru.gosuslugi.pgu.common.core.service.HealthHolder;
import ru.gosuslugi.pgu.common.core.service.OkatoHolder;
import ru.gosuslugi.pgu.common.core.service.dto.DictionayHealthDto;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataError;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class NsiDadataServiceImpl implements NsiDadataService {

    @Value("${pgu.dadata-url}")
    private String pguUrl;

    private static final String NORMALIZE_URL = "api/nsi/v1/dadata/normalize?q={q}";
    private static final String BASE_DADATA_URL = "api/nsi/v1/dadata/{fias}";

    private final RestTemplate restTemplate;
    private final HealthHolder healthHolder;
    private final OkatoHolder okatoHolder;

    @Override
    public DadataAddressResponse getAddress(String address) {
        return getAddress(address, okatoHolder.getOkato());
    }

    @Override
    public DadataAddressResponse getAddress(String address, String okato) {
        DadataAddressResponse result;

        String url = pguUrl + NORMALIZE_URL;
        try {
            result = restTemplate.getForObject(url, DadataAddressResponse.class, Map.of("q", address));
        } catch (EntityNotFoundException | ExternalServiceException e) {
            throw new NsiExternalException("dadataAddress", url, HttpMethod.GET, "Исключение при вызове метода", okato, e);
        }

        // validation
        handleErrorForAddress(address, result, "dadataAddress", url, HttpMethod.GET, okato);
        healthHolder.addDictionaryHealth(new DictionayHealthDto("dadataAddress", url, HttpMethod.GET, HttpStatus.OK, null, null, okato));
        return result;
    }

    private void handleErrorForAddress(String address, DadataAddressResponse result, String id, String url, HttpMethod method, String okato) {
        if (isNull(result)) {
            String message = "Сервис не вернул результат";
            if (log.isInfoEnabled()) {
                log.info(message + ". Адрес = {}", address);
            }
            throw new NsiExternalException(id, url, method, message, okatoHolder.getOkato());
        }

        Optional<DadataError> error = Optional.ofNullable(result).map(DadataAddressResponse::getError);
        Integer errorCode = error.map(DadataError::getCode).orElse(null);
        if (isNull(errorCode) || errorCode != 0) {
            if (log.isInfoEnabled()) {
                String errorMessage = error.map(DadataError::getMessage).orElse(null);
                log.info("Не удалось получить проверить адрес в Dadata сервисе. Адрес = {}, код = {}, сообшение = \"{}\"", address, errorCode, errorMessage);
            }
            if(Objects.nonNull(errorCode) && errorCode > 400){
                throw new NsiExternalException(id, url, method, "Dadata service not available " + errorCode,"");
            }
            throw new NsiExternalException(id, url, method, "Не удалось получить проверить адрес в Dadata сервисе. Код = " + errorCode, okato);
        }
    }


    @Override
    public DadataAddressResponse getAddressByFiasCode(String fiasCode) {
        DadataAddressResponse result;

        String url = pguUrl + BASE_DADATA_URL;
        try {
            result = restTemplate.getForObject(url, DadataAddressResponse.class, Map.of("fias", fiasCode));
        } catch (EntityNotFoundException | ExternalServiceException e) {
            throw new NsiExternalException("dadataAddressByFiasCode", url, HttpMethod.GET, "Исключение при вызове метода", okatoHolder.getOkato(), e);
        }

        // validation
        handleErrorForAddressByFiasCode(fiasCode, result, "dadataAddressByFiasCode", url, HttpMethod.GET);
        healthHolder.addDictionaryHealth(new DictionayHealthDto("dadataAddressByFiasCode", url, HttpMethod.GET, HttpStatus.OK, null, null, okatoHolder.getOkato()));
        return result;
    }

    @Override
    public String getAddressOkato(String address) {
        String url = pguUrl + NORMALIZE_URL;
        try {
            DadataAddressResponse addressResponse = restTemplate.getForObject(url, DadataAddressResponse.class, Map.of("q", address));
            if (addressResponse != null) {
                return addressResponse.getOkato();
            }
        } catch (EntityNotFoundException | ExternalServiceException e) {
            throw new NsiExternalException("dadataAddress", url, HttpMethod.GET, "Исключение при вызове метода", "", e);
        }
        return null;
    }

    private void handleErrorForAddressByFiasCode(String fiasCode, DadataAddressResponse result, String id, String url, HttpMethod method) {
        if (isNull(result)) {
            String message = "Сервис не вернул результат";
            if (log.isInfoEnabled()) {
                log.info(message + ". Фиас код = {}", fiasCode);
            }
            throw new NsiExternalException(id, url, method, message, okatoHolder.getOkato());
        }

        Optional<DadataError> error = Optional.ofNullable(result).map(DadataAddressResponse::getError);
        Integer errorCode = error.map(DadataError::getCode).orElse(null);
        if (isNull(errorCode) || errorCode != 0) {
            if (log.isInfoEnabled()) {
                String errorMessage = error.map(DadataError::getMessage).orElse(null);
                log.info("Не удалось получить проверить фиас код в Dadata сервисе. Фиас код = {}, код = {}, сообшение = \"{}\"", fiasCode, errorCode, errorMessage);
            }
            throw new NsiExternalException(id, url, method, "Не удалось получить проверить фиас код в Dadata сервисе. Код = " + errorCode, okatoHolder.getOkato());
        }
    }
}
