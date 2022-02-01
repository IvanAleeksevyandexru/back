package ru.gosuslugi.pgu.fs.esia.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.atc.carcass.security.rest.model.EsiaContact;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.esia.EsiaRestContactDataClient;
import ru.gosuslugi.pgu.fs.esia.config.EsiaRestProperties;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaConfirmContactDto;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactDto;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactState;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaOmsDto;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsiaRestContactDataClientImpl implements EsiaRestContactDataClient {

    private static final String CHECK_PHONE_CONFIRMED_URL = "/ctt/check-cfm-mobile?mobile={mobile}";
    private static final String CHECK_CONTACT_USED_URL = "/ctt/used?value={value}&type={type}";
    private static final String CREATE_CONTACT_URL = "/prns/{prn_oid}/ctts";
    private static final String ADD_UPDATE_ADDRESS_URL = "/prns/{prn_oid}/addrs";
    private static final String CHANGE_CONTACT_URL = "/prns/{prn_oid}/ctts/{id}";
    private static final String CHANGE_OMS_URL = "/prns/{prn_oid}/docs";
    private static final String RESEND_CODE_URL = "/prns/{prn_oid}/ctts/{id}/rfrCode";
    private static final String CONFIRM_CONTACT = "/prns/{prn_oid}/ctts/confirm";
    private static final String CHECK_CONTACT_CONFIRMED = "/ctt/checkUsedCfmd?value={value}&type={type}";

    /** Урлы для изменения контакта организации */
    private static final String CREATE_LEGAL_CONTACT_URL = "/orgs/{org_oid}/ctts";
    private static final String CHANGE_LEGAL_CONTACT_URL = "/orgs/{org_oid}/ctts/{id}";
    private static final String RESEND_LEGAL_CODE_URL = "/orgs/{org_oid}/ctts/{id}/rfrCode";

    private final EsiaRestProperties esiaRestProperties;
    private final UserPersonalData userPersonalData;
    private final UserOrgData userOrgData;
    private final RestTemplate esiaClientRestTemplate;

    @Override
    public EsiaContactState checkPhone(String phone) {
        return getPhoneUsedResponse(phone);
    }

    @Override
    public Boolean checkEmail(String email) {
        ResponseEntity<EsiaContactState> response = esiaClientRestTemplate
                .exchange(buildEsiaUrl() + CHECK_CONTACT_USED_URL,
                        HttpMethod.GET,
                        new HttpEntity<String>(this.prepareSecurityHeader()),
                        EsiaContactState.class,
                        Map.of(
                                "value", URLEncoder.encode(email),
                                "type", EsiaContact.Type.EMAIL.getCode()
                        )
                );
        validateResponse(response);
        return response.getBody().isState();
    }

    @Override
    public Boolean isEmailConfirmed(String esiaContactTypeCode, String email) {
        ResponseEntity<EsiaContactState> response = esiaClientRestTemplate
                .exchange(buildEsiaUrl() + CHECK_CONTACT_CONFIRMED,
                        HttpMethod.GET,
                        new HttpEntity<String>(this.prepareSecurityHeader()),
                        EsiaContactState.class,
                        Map.of(
                                "value", URLEncoder.encode(email),
                                "type", esiaContactTypeCode
                        )
                );
        validateResponse(response);
        return response.getBody().isResult();
    }

    private Boolean checkPhoneConfirmed(String phone) {
        ResponseEntity<EsiaContactState> response = esiaClientRestTemplate
                .exchange(buildEsiaUrl() + CHECK_PHONE_CONFIRMED_URL,
                        HttpMethod.GET,
                        new HttpEntity<String>(this.prepareSecurityHeader()),
                        EsiaContactState.class,
                        Map.of(
                                "mobile", URLEncoder.encode(phone)
                        )
                );
        validateResponse(response);
        return response.getBody().isState();
    }

    private EsiaContactState getPhoneUsedResponse(String phone) {
        ResponseEntity<EsiaContactState> response = esiaClientRestTemplate
                .exchange(buildEsiaUrl() + CHECK_CONTACT_USED_URL,
                        HttpMethod.GET,
                        new HttpEntity<String>(this.prepareSecurityHeader()),
                        EsiaContactState.class,
                        Map.of(
                                "value", URLEncoder.encode(phone),
                                "type", EsiaContact.Type.MOBILE_PHONE.getCode()
                        )
                );
        validateResponse(response);
        return response.getBody();
    }


    @Override
    public EsiaContactDto addContact(EsiaContactDto esiaContactDto) {
        HttpEntity<EsiaContactDto> httpEntity = new HttpEntity<>(esiaContactDto, this.prepareSecurityHeader());
        try {
            ResponseEntity<EsiaContactDto> esiaContactDtoResponseEntity = esiaClientRestTemplate.postForEntity(
                    buildEsiaUrl() + CREATE_CONTACT_URL,
                    httpEntity,
                    EsiaContactDto.class,
                    Map.of("prn_oid", userPersonalData.getUserId())
            );
            validateResponse(esiaContactDtoResponseEntity);
            return esiaContactDtoResponseEntity.getBody();
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    @Override
    public EsiaContactDto changeContact(EsiaContactDto esiaContactDto) {
        HttpEntity<EsiaContactDto> httpEntity = new HttpEntity<>(esiaContactDto, this.prepareSecurityHeader());
        try {
            esiaClientRestTemplate.put(
                    buildEsiaUrl() + CHANGE_CONTACT_URL,
                    httpEntity,
                    Map.of(
                            "prn_oid", userPersonalData.getUserId(),
                            "id", esiaContactDto.getId()
                    )
            );
            return esiaContactDto;
        } catch(RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }


    @Override
    public Boolean resendCode(String contactId) {
        try {
            ResponseEntity<Object> response = esiaClientRestTemplate
                    .postForEntity(buildEsiaUrl() + RESEND_CODE_URL,
                            new HttpEntity<String>(this.prepareSecurityHeader()),
                            Object.class,
                            Map.of(
                                    "prn_oid",userPersonalData.getUserId(),
                                    "id", contactId
                            )
                    );
            validateResponse(response);
            return response.getStatusCode().is2xxSuccessful();
        } catch (ExternalServiceException e) {
            return false;
        }
    }

    @Override
    public Boolean confirmContact(String code) {
        try {
            EsiaConfirmContactDto esiaConfirmContactDto = new EsiaConfirmContactDto();
            esiaConfirmContactDto.setCnfCode(code);
            esiaConfirmContactDto.setType(EsiaContact.Type.MOBILE_PHONE.getCode());
            HttpEntity<EsiaConfirmContactDto> httpEntity = new HttpEntity<>(esiaConfirmContactDto, this.prepareSecurityHeader());
            ResponseEntity<Object> esiaContactDtoResponseEntity = esiaClientRestTemplate.postForEntity(
                    buildEsiaUrl()+CONFIRM_CONTACT,
                    httpEntity,
                    Object.class,
                    Map.of("prn_oid",userPersonalData.getUserId())
            );
            validateResponse(esiaContactDtoResponseEntity);
            return esiaContactDtoResponseEntity.getStatusCode().is2xxSuccessful();
        } catch (RestClientException | ExternalServiceException e) {
            return false;
        }
    }

    @Override
    public EsiaAddress updateAddress(EsiaAddress esiaAddress) {
        HttpEntity<EsiaAddress> httpEntity = new HttpEntity<>(esiaAddress, this.prepareSecurityHeader());
        try {
            ResponseEntity<EsiaAddress> responseEntity = esiaClientRestTemplate.postForEntity(
                    buildEsiaUrl() + ADD_UPDATE_ADDRESS_URL,
                    httpEntity,
                    EsiaAddress.class,
                    Map.of("prn_oid", userPersonalData.getUserId())
            );
            validateResponse(responseEntity);
            return responseEntity.getBody();
        } catch (RestClientException e) {
            String message = "Ошибка добавление/обновления адреса для пользователя Id = " + userPersonalData.getUserId() + " тип адреса = " + esiaAddress.getType();
            if (log.isErrorEnabled()) {
                log.error(message, e);
            }
            throw new ExternalServiceException(message, e);
        }
    }

    @Override
    public EsiaContactDto addLegalContact(EsiaContactDto esiaContactDto) {
        HttpEntity<EsiaContactDto> httpEntity = new HttpEntity<>(esiaContactDto, this.prepareSecurityHeader());
        try {
            ResponseEntity<EsiaContactDto> esiaContactDtoResponseEntity = esiaClientRestTemplate.postForEntity(
                    buildEsiaUrl() + CREATE_LEGAL_CONTACT_URL,
                    httpEntity,
                    EsiaContactDto.class,
                    Map.of("org_oid", userOrgData.getOrg().getOid())
            );
            validateResponse(esiaContactDtoResponseEntity);
            return esiaContactDtoResponseEntity.getBody();
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    @Override
    public EsiaContactDto changeLegalContact(EsiaContactDto esiaContactDto) {
        HttpEntity<EsiaContactDto> httpEntity = new HttpEntity<>(esiaContactDto, this.prepareSecurityHeader());
        try {
            esiaClientRestTemplate.postForEntity(
                    buildEsiaUrl() + CHANGE_LEGAL_CONTACT_URL,
                    httpEntity,
                    EsiaContactDto.class,
                    Map.of(
                            "org_oid", userOrgData.getOrg().getOid(),
                            "id", esiaContactDto.getId()
                    )
            );
            return esiaContactDto;
        } catch(RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    @Override
    public EsiaOmsDto changeOms(EsiaOmsDto esiaOmsDto) {
        HttpEntity<EsiaOmsDto> httpEntity = new HttpEntity<>(esiaOmsDto, this.prepareSecurityHeader());
        try {
            ResponseEntity<EsiaOmsDto> responseEntity = esiaClientRestTemplate.postForEntity(
                    buildEsiaUrl() + CHANGE_OMS_URL,
                    httpEntity,
                    EsiaOmsDto.class,
                    Map.of("prn_oid", userPersonalData.getUserId())
            );
            validateResponse(responseEntity);
            return responseEntity.getBody();
        } catch (RestClientException e) {
            String message = "Ошибка создания данных омс для пользователя Id " + userPersonalData.getUserId();
            if (log.isErrorEnabled()) {
                log.error(message, e);
            }
            throw new ExternalServiceException(message, e);
        }
    }

    @Override
    public Boolean resendLegalCode(String contactId) {
        try {
            ResponseEntity<Object> response = esiaClientRestTemplate
                    .postForEntity(buildEsiaUrl() + RESEND_LEGAL_CODE_URL,
                            new HttpEntity<String>(this.prepareSecurityHeader()),
                            Object.class,
                            Map.of(
                                    "org_oid", userOrgData.getOrg().getOid(),
                                    "id", contactId
                            )
                    );
            validateResponse(response);
            return response.getStatusCode().is2xxSuccessful();
        } catch (ExternalServiceException e) {
            return false;
        }
    }

    private void validateResponse(ResponseEntity responseEntity) {
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return;
        }
        if (log.isWarnEnabled()) log.warn("Получена ошибка от Api {}; Код ответа = {}; Тело ответа = {}", esiaRestProperties.getUrl(),
                responseEntity.getStatusCodeValue(), responseEntity.getBody());
    }

    private String buildEsiaUrl() {
        return String.join("/", Arrays.asList(esiaRestProperties.getUrl(), esiaRestProperties.getVersion()));
    }

    private HttpHeaders prepareSecurityHeader() {
        return PguAuthHeadersUtil.prepareAuthBearerHeaders(userPersonalData.getToken());
    }
}
