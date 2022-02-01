package ru.gosuslugi.pgu.fs.component.esep;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.atc.carcass.security.rest.model.orgs.Org;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.SignInfo;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.dto.esep.CertificateInfoDto;
import ru.gosuslugi.pgu.dto.esep.CertificateUserInfoDto;
import ru.gosuslugi.pgu.dto.esep.FileCertificateUserInfoRequest;
import ru.gosuslugi.pgu.dto.esep.FileCertificatesUserInfoResponse;
import ru.gosuslugi.pgu.dto.esep.PrepareSignRequest;
import ru.gosuslugi.pgu.dto.esep.PrepareSignResponse;
import ru.gosuslugi.pgu.dto.esep.SignedFileInfo;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.esep.model.EsepSignCheckResult;
import ru.gosuslugi.pgu.fs.component.esep.model.EsepSignComponentDto;
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Компонет для подписания заявления ЭЦП
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Setter
public class EsepSignComponent extends AbstractComponent<EsepSignComponentDto> {

    public static final String ADDITIONAL_DATA_ARGUMENT = "additionalData";
    public static final String ADDITIONAL_DATA_ON = "on";
    @Value("${voshod.integration:#{null}}")
    protected String voshodUrl;

    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;
    private final UserOrgData userOrgData;
    private final FormScenarioDtoServiceImpl scenarioDtoService;

    public static final String NOT_SIGNED_ERROR = "Заявление не было подписано";
    public static final String NOT_SIGNED_BY_USER_SIGNATURE_ERROR = "Заявление было подписано чужой подписью";
    public static final String RETURN_URL_ATTR_NOT_FOUND_ERROR = "Не задана ссылка на возврат с формы подписания";

    public static final String PREPARE_SIGN_METHOD_PATH = "/prepareSign";
    public static final String GET_CERTIFICATES_USER_INFOS_METHOD_PATH = "/getFileCertificatesUserInfo";
    public static final String ADDITIONAL_PATH_PARAM = "getLastScreen";
    public static final String ADDITIONAL_PATH_PARAM_VALUE = "signatureSuccess";
    public static final String SP_REQUEST_GUID_ATTR_NAME = "sp_request_guid";

    /**
     * Дополнительный флаг в additionalParameters,
     * который сообщает, что файлы завяления уже были сгенерированны
     * другим поользователем в рамках подписания
     */
    public static final String ALL_FILES_GENERATED_ATTR_NAME = "allFilesGenerated";

    private static final Locale LOCAL_RU = new Locale("ru");


    @Override
    public ComponentType getType() {
        return ComponentType.EsepSign;
    }

    @Override
    public ComponentResponse<EsepSignComponentDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        Map<Long, SignInfo> signInfoMap = scenarioDto.getSignInfoMap();
        boolean filesExists = false;
        scenarioDtoService.updateAdditionalAttributesWithDraftSaving(scenarioDto, serviceDescriptor.getSmevEnv(), serviceDescriptor);
        //Проверка была ли генерация файлов в рамках друго пользователя, который уже подписывал заявления
        String allFilesGenerated = scenarioDto.getAdditionalParameters().get(ALL_FILES_GENERATED_ATTR_NAME);
        if (Objects.nonNull(allFilesGenerated) && Boolean.parseBoolean(allFilesGenerated)) {
            filesExists = true;
        }
        // если ссыдка на форму подписания уже получена
        if (signInfoMap != null && signInfoMap.containsKey(scenarioDto.getOrderId())) {
            SignInfo signInfo = signInfoMap.get(scenarioDto.getOrderId());
            boolean alreadySigned = false;
            // проверяем, возможно, пользователь уже подписал заявление
            if (signInfo.getAlreadySigned()) {
                alreadySigned = true;
            } else {
                // проверяем - может пользователь уже подписал, но мы об этом еще не знаем
                EsepSignCheckResult signCheckResult = checkUserSigned(signInfo.getSignedFilesInfo());
                if (signCheckResult == EsepSignCheckResult.SIGNED) {
                    alreadySigned = true;
                    signInfo.setAlreadySigned(true);
                }
            }
            return ComponentResponse.of(new EsepSignComponentDto(signInfo.getUrl(), alreadySigned));
        }

        // GUID должен быть одинаковым во всех заявлениях комплексной услуги
        if (signInfoMap == null || signInfoMap.size()==0) {
            signInfoMap = new HashMap<>();
            scenarioDto.setSignInfoMap(signInfoMap);
            if(Objects.isNull(scenarioDto.getAdditionalParameters().get(SP_REQUEST_GUID_ATTR_NAME))) {
                scenarioDto.getAdditionalParameters().put(SP_REQUEST_GUID_ATTR_NAME, UUID.randomUUID().toString());
            }
        }
        String requestGuid = scenarioDto.getAdditionalParameters().get(SP_REQUEST_GUID_ATTR_NAME);

        // делаем запрос на создание формы подписания
        PrepareSignResponse response = prepareSign(scenarioDto.getOrderId(), userPersonalData.getUserId(),userPersonalData.getOrgId(), prepareReturnUrl(scenarioDto.getCurrentUrl()), requestGuid, filesExists);
        signInfoMap.put(scenarioDto.getOrderId(), new SignInfo(response.getOperationID(), response.getUrl(), response.getSignedFileInfos(), false));
        return ComponentResponse.of(new EsepSignComponentDto(response.getUrl(), false));
    }

    public void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent component) {
        // нужно проверить что пользователь подписал заявление своей ЭЦП
        Map<Long, SignInfo> signInfoMap = scenarioDto.getSignInfoMap();
        SignInfo signInfo = signInfoMap.get(scenarioDto.getOrderId());
        if (!signInfo.getAlreadySigned()) {
            EsepSignCheckResult signCheckResult = getEsepSignCheckResult(signInfo, component);
            if (signCheckResult == EsepSignCheckResult.SIGNED) {
                signInfo.setAlreadySigned(true);
            } else if(signCheckResult == EsepSignCheckResult.NOT_SIGNED) {
                incorrectAnswers.put(entry.getKey(), NOT_SIGNED_ERROR);
            } else {
                // если заявление подписано чужой подписью - пересоздаем форму подписания
                PrepareSignResponse response = prepareSign(
                        scenarioDto.getOrderId(),
                        userPersonalData.getUserId(),
                        userPersonalData.getOrgId(),
                        prepareReturnUrl(scenarioDto.getCurrentUrl()),
                        scenarioDto.getAdditionalParameters().get(SP_REQUEST_GUID_ATTR_NAME),
                        true);
                signInfoMap.put(scenarioDto.getOrderId(), new SignInfo(response.getOperationID(), response.getUrl(), response.getSignedFileInfos(), false));
                entry.getValue().setValue(jsonProcessingService.toJson(new EsepSignComponentDto(response.getUrl(), false)));
                incorrectAnswers.put(entry.getKey(), NOT_SIGNED_BY_USER_SIGNATURE_ERROR);
            }
        }
    }

    private String prepareReturnUrl(String currentUrl) {
        URIBuilder returnUrlBuilder;
        if (Objects.isNull(currentUrl)) {
            throw new FormBaseException(RETURN_URL_ATTR_NOT_FOUND_ERROR);
        }
        try {
            returnUrlBuilder = new URIBuilder(currentUrl);
            returnUrlBuilder.addParameter(ADDITIONAL_PATH_PARAM, ADDITIONAL_PATH_PARAM_VALUE);
            return returnUrlBuilder.build().toURL().toString();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new FormBaseException(RETURN_URL_ATTR_NOT_FOUND_ERROR);
        }
    }

    private EsepSignCheckResult getEsepSignCheckResult(SignInfo signInfo, FieldComponent component) {
        if ("UL".equals(component.getAttrs().get("signUserType")))
            return checkOrganizationUserSigned(signInfo.getSignedFilesInfo());
        return checkUserSigned(signInfo.getSignedFilesInfo());
    }

    /**
     * Проверяем что рользователь подписал все файлы
     * @param signedFileInfos   Информация о подписанных файлах, из которой берем коды доступов для проверки
     * @return                  Результат проверки подписания
     */
    private EsepSignCheckResult checkUserSigned(List<SignedFileInfo> signedFileInfos) {
        List<String> fileAccessCodes = Objects.nonNull(signedFileInfos) ?
                signedFileInfos.stream().map(SignedFileInfo::getFileAccessCodes).collect(Collectors.toList())
                : new ArrayList<>();
        FileCertificatesUserInfoResponse response = getFileCertificatesUserInfo(fileAccessCodes);
        List<CertificateInfoDto> certificateInfoList = response.getCertificateInfoDtoList();
        if (CollectionUtils.isEmpty(certificateInfoList)) {
            return EsepSignCheckResult.NOT_SIGNED;
        }

        Person person = userPersonalData.getPerson();
        String esiaSnils = person.getSnils();
        // по идее такого не должно быть, считаем что не подписано
        if (isEmpty(esiaSnils)) {
            log.error("У пользователя (oid={}) нет СНИЛСа, не возможно проверить достоверность подписания заявления ЭЦП", userPersonalData.getUserId());
            return EsepSignCheckResult.NOT_SIGNED;
        }

        esiaSnils = esiaSnils.replaceAll("[^0-9]", "");
        String esiaCommonName = Stream.of(person.getLastName(), person.getFirstName(), person.getMiddleName())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "))
                .trim()
                .toLowerCase(LOCAL_RU);

        for (CertificateInfoDto certificateInfo : certificateInfoList) {
            List<CertificateUserInfoDto> userInfoList = certificateInfo.getCertificateUserInfoList();
            if (CollectionUtils.isEmpty(userInfoList)) {
                return EsepSignCheckResult.NOT_SIGNED;
            }

            boolean found = false;
            for (CertificateUserInfoDto userInfo: userInfoList) {
                if (userInfo.getSnils() != null && userInfo.getCommonName() != null) {
                    String certSnils = userInfo.getSnils().replaceAll("[^0-9]", "");
                    String certCommonName = userInfo.getCommonName().trim().toLowerCase(LOCAL_RU);
                    if (certSnils.equals(esiaSnils) && certCommonName.startsWith(esiaCommonName)) {
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                return EsepSignCheckResult.NOT_SIGNED_BY_USER_SIGNATURE;
            }
        }

        return EsepSignCheckResult.SIGNED;
    }

    /**
     * Проверяем что рользователь подписал все файлы через свою ЭЦП организации
     * @param signedFileInfos   Информация о подписанных файлах, из которой берем коды доступов для проверки
     * @return                  Результат проверки подписания
     */
    private EsepSignCheckResult checkOrganizationUserSigned(List<SignedFileInfo> signedFileInfos) {
        if(isNull(userOrgData) || isNull(userOrgData.getOrg()))
            return EsepSignCheckResult.NOT_SIGNED_EMPTY_ORG_DATA;

        List<String> fileAccessCodes = Objects.nonNull(signedFileInfos) ?
                signedFileInfos.stream().map(SignedFileInfo::getFileAccessCodes).collect(Collectors.toList())
                : new ArrayList<>();
        FileCertificatesUserInfoResponse response = getFileCertificatesUserInfo(fileAccessCodes);
        List<CertificateInfoDto> certificateInfoList = response.getCertificateInfoDtoList();
        if (CollectionUtils.isEmpty(certificateInfoList))
            return EsepSignCheckResult.NOT_SIGNED;

        Org userOrg = userOrgData.getOrg();
        String snils = userPersonalData.getPerson().getSnils().replaceAll("-", "").replaceAll(" ", "");
        String userPersonalInn = userPersonalData.getPerson().getInn();
        String inn = userOrg.getInn();
        String ogrn = userOrg.getOgrn();

        for (CertificateInfoDto certificateInfo : certificateInfoList) {
            List<CertificateUserInfoDto> userInfoList = certificateInfo.getCertificateUserInfoList();
            if (CollectionUtils.isEmpty(userInfoList))
                return EsepSignCheckResult.NOT_SIGNED;

            for (CertificateUserInfoDto certUserInfo: userInfoList) {
                String certUserInn = certUserInfo.getInn();
                String certUserOgrn = certUserInfo.getOgrn();
                String certUserOgrnip = certUserInfo.getOgrnip();
                String certUserSnils = certUserInfo.getSnils().replaceAll("-", "").replaceAll(" ", "");

                //UL INN + OGRN
                boolean isInnCheck = StringUtils.hasText(certUserInn) ?
                        certUserInn.equals(inn) || certUserInn.equals(userPersonalInn)
                        : true;
                if (isInnCheck && ogrn.equals(certUserOgrn) && snils.equals(certUserSnils))
                    continue;

                //IP INN + OGRNIP
                if (isInnCheck && ogrn.equals(certUserOgrnip) && snils.equals(certUserSnils))
                    continue;

                return EsepSignCheckResult.NOT_SIGNED_BY_USER_SIGNATURE;
            }
        }
        return EsepSignCheckResult.SIGNED;
    }

    /**
     * Метод для создания формы подписания заявления ЭЦП
     * @param orderId               Идентификатор заявления
     * @param userId                Идентификатор пользователя
     * @param orgId                 Идентификатор организации пользователя
     * @param currentUrl            URL форм сервиса (нужен для возврата с формы подписания)
     * @param requestGuid           GUID файлов заявления
     * @param filesAlreadyExists    Признак существования файлов заявления
     * @return                      Данные для подписания заявления
     */

    private PrepareSignResponse prepareSign(Long orderId, Long userId, Long orgId, String currentUrl, String requestGuid, boolean filesAlreadyExists) {
        PrepareSignRequest request = new PrepareSignRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setOrgId(orgId);
        request.setReturnUrl(currentUrl);
        request.setRequestGuid(requestGuid);
        request.setFilesAlreadyExists(filesAlreadyExists);
        try {
            ResponseEntity<PrepareSignResponse> result = restTemplate.exchange(
                    voshodUrl + PREPARE_SIGN_METHOD_PATH, HttpMethod.POST,
                    new HttpEntity<>(request),
                    PrepareSignResponse.class
            );
            return result.getBody();
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    private FileCertificatesUserInfoResponse getFileCertificatesUserInfo(List<String> fileAccessCodes) {
        FileCertificateUserInfoRequest request = new FileCertificateUserInfoRequest();
        request.setFileAccessCodes(fileAccessCodes);
        try {
            ResponseEntity<FileCertificatesUserInfoResponse> result = restTemplate.exchange(
                    voshodUrl + GET_CERTIFICATES_USER_INFOS_METHOD_PATH, HttpMethod.POST,
                    new HttpEntity<>(request),
                    FileCertificatesUserInfoResponse.class
            );
            return result.getBody();
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }
}
