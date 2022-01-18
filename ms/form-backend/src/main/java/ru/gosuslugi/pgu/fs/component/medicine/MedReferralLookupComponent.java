package ru.gosuslugi.pgu.fs.component.medicine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.component.medicine.mapper.ReferralMapper;
import ru.gosuslugi.pgu.fs.component.medicine.model.*;
import ru.gosuslugi.pgu.fs.component.userdata.model.*;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REFERRAL_NUMBER;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SESSION_ID;

/** получение данных о направлении по номеру */
@Slf4j
@RequiredArgsConstructor
@Component
public class MedReferralLookupComponent extends AbstractComponent<String> {
    private static final String E_QUEUE_SERVICE_URL = "api/lk/v1/equeue/agg/ref/items";
    private static final String NO_DATA_PREFIX = "NO_DATA";
    private static final String PREFIX_DELIMITER = ":";

    @Value("${pgu.lkapi-url}")
    private String lkApiUrl;

    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;
    private final ReferralMapper referralMapper;
    private final ErrorModalDescriptorService errorModalDescriptorService;

    @Override
    public ComponentType getType() {
        return ComponentType.ReferralNumber;
    }

    @Override
    protected void postProcess(FieldComponent component, ScenarioDto scenarioDto, String value) {
        val eServiceId = component.getArgument("eserviceId");
        val sessionId = component.getArgument("sessionId");
        val result = new ReferralNumberComponentDto();

        try {
            val dictionary = callMedDictionary(value, eServiceId, sessionId);
            if (nonNull(dictionary)) {
                if (nonNull(dictionary.getError()) && nonNull(dictionary.getError().getErrorDetail())) {
                    result.setErrorCode(dictionary.getError().getErrorDetail().getErrorCode());
                    result.setErrorMessage(dictionary.getError().getErrorDetail().getErrorMessage());
                }
                if (result.getErrorCode() == 0 && !CollectionUtils.isEmpty(dictionary.getItems())) {
                    dictionary.getItems().stream()
                            .map(this::toReferralAttrs)
                            .map(referralMapper::toReferral)
                            .max(Comparator.comparing(ReferralDto::getReferralStartDate))
                            .ifPresent(result::setReferral);
                }
            }
        } catch (RestClientException | ExternalServiceException | EntityNotFoundException e) {
            log.warn("Referral receiving error from an external service", e);
        }
        result.setStatusCode(determineStatusCode(result));
        scenarioDto.getCurrentValue().put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(result)));
    }

    private MedDictionaryResponse callMedDictionary(String referralNumber, String eServiceId, String sessionId) {
        val request = MedicalItemsRequest.builder()
                .eserviceId(eServiceId)
                .filter(createItemsFilter(referralNumber, sessionId))
                .refName("Referral")
                .treeFiltering("ONELEVEL")
                .build();

        val entity = restTemplate.exchange(
                lkApiUrl + E_QUEUE_SERVICE_URL,
                HttpMethod.POST,
                new HttpEntity<>(request, PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                MedDictionaryResponse.class
        );
        return entity.getBody();
    }

    private MedicalItemsFilter createItemsFilter(String referralNumber, String sessionId) {
        val subs = List.of(
                createSimpleFilter(REFERRAL_NUMBER, referralNumber),
                createSimpleFilter(SESSION_ID, sessionId)
        );
        val union = MedicalItemsUnion.builder()
                .unionKind("AND")
                .subs(subs)
                .build();
        return MedicalItemsFilter.builder()
                .union(union)
                .build();
    }

    private MedicalSimpleFilter createSimpleFilter(String attributeName, String attributeValue) {
        val item = MedicalSimpleItem.builder()
                .attributeName(attributeName)
                .value(attributeValue)
                .condition("EQUALS")
                .checkAllValues(true)
                .build();
        return MedicalSimpleFilter.builder()
                .simple(item)
                .build();
    }

    private Map<String, String> toReferralAttrs(MedDictionaryResponseItem item) {
        return item.getAttributes().stream()
                .collect(toMap(MedDictionaryResponseAttribute::getName, MedDictionaryResponseAttribute::getValue));
    }

    private MedDictionaryResponseCode determineStatusCode(ReferralNumberComponentDto referralNumberComponent) {
        val referral = referralNumberComponent.getReferral();
        val errorMessage = referralNumberComponent.getErrorMessage();
        val errorModalWindow = errorModalDescriptorService.getErrorModal(ErrorModalView.LOADING_ERROR);

        if (nonNull(referral)) {
            return referral.isExpired()
                    ? MedDictionaryResponseCode.REFERRAL_EXPIRED
                    : MedDictionaryResponseCode.SUCCESS;
        }
        if (nonNull(errorMessage) && errorMessage.startsWith(NO_DATA_PREFIX)) {
            return MedDictionaryResponseCode.REFERRAL_NOT_FOUND;
        }
        if (nonNull(errorMessage)) {
            val index = errorMessage.indexOf(PREFIX_DELIMITER);
            errorModalWindow.getContent().setHelperText(errorMessage.substring(index + 1));
        }
        throw new ErrorModalException(errorModalWindow, "Произошла ошибка загрузки мед. направления из ЛК");
    }
}
