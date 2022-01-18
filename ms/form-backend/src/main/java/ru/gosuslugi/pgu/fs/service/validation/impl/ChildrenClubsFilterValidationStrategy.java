package ru.gosuslugi.pgu.fs.service.validation.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.service.impl.NsiDictionaryFilterHelper;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;

import java.util.Map;
import java.util.function.Supplier;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChildrenClubsFilterValidationStrategy extends AbstractDictionaryFilterValidationStrategy {


    @Value("${pgu.eaisdo-dict-url}")
    private String eaisdoDictUlr;

    public static final String EXTERNAL_DICTIONARY_ERROR = "Ошибка обращения к внешнему справочнику";
    public static final String CHILDREN_CLUBS_API_URL = "api/eaisdo-service/v1";

    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;
    private final NsiDictionaryFilterHelper nsiDictionaryFilterHelper;

    @Override
    public DictType getDictUrlType() {
        return DictType.childrenClubsApi;
    }

    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers,
                                    Map.Entry<String, ApplicantAnswer> entry,
                                    ScenarioDto scenarioDto,
                                    FieldComponent fieldComponent,
                                    Supplier<ComponentResponse<String>> supplier) {

        try {
            NsiDictionary dictionary = getDictionaryForFilter(fieldComponent, scenarioDto, supplier);
            validateDictionaryItem(incorrectAnswers, entry, fieldComponent, dictionary);
        } catch (JSONException e) {
            throw new JsonParsingException(NOT_CORRECT_JSON_FORMAT, e);
        }
    }

    private NsiDictionary getDictionaryForFilter(FieldComponent fieldComponent,
                                                 ScenarioDto scenarioDto,
                                                 Supplier<ComponentResponse<String>> supplier) throws JSONException {
        // Заполнить предустановленные значения
        Map<String, String> presetProperties = nsiDictionaryFilterHelper.getPresetValue(fieldComponent, supplier);

        String dictionaryName = fieldComponent.getAttrs().get(DICTIONARY_NAME_ATTR).toString();
        NsiDictionaryFilterRequest filterRequest = nsiDictionaryFilterHelper.buildNsiDictionaryFilterRequest(scenarioDto, fieldComponent, presetProperties);
        return getDictionaryItemForMapsByFilter(dictionaryName, filterRequest);
    }

    private NsiDictionary getDictionaryItemForMapsByFilter(String dictionaryName, NsiDictionaryFilterRequest filterRequest) {
        NsiDictionary result;
        String url = String.format("%s%s/%s", eaisdoDictUlr, CHILDREN_CLUBS_API_URL, dictionaryName);
        HttpHeaders headers = PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken());
        HttpEntity<NsiDictionaryFilterRequest> httpEntity = new HttpEntity<>(filterRequest, headers);
        try {
            result = restTemplate.postForObject(url, httpEntity, NsiDictionary.class);
        } catch (RestClientException e) {
            throw new ExternalServiceException(EXTERNAL_DICTIONARY_ERROR, e);
        }
        return result;
    }
}
