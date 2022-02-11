package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponse;
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponseErrorDetail;
import ru.gosuslugi.pgu.fs.component.userdata.model.*;
import ru.gosuslugi.pgu.fs.service.MedicineService;

import java.util.*;

import static ru.gosuslugi.pgu.components.ComponentAttributes.SESSION_ID;
import static ru.gosuslugi.pgu.components.regex.RegExpContext.getValueByRegex;
import static ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil.prepareAuthCookieHeaders;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private static final String API_URL_REF_ITEMS = "api/lk/v1/equeue/agg/ref/items";
    private static final Set<String> IGNORE_ARGUMENTS = Set.of("ESERVICEID_V3", "ESERVICEID_V2", "serviceCode", "checkRegionCode");
    private static final String FAILURE_ERROR_CODE = "FAILURE";
    private static final String UNKNOWN_REQUEST_DESCRIPTION_ERROR_CODE = "UNKNOWN_REQUEST_DESCRIPTION";
    private static final String NO_DATA_ERROR_CODE = "NO_DATA";
    private static final List<String> ERROR_CODES = List.of(FAILURE_ERROR_CODE, UNKNOWN_REQUEST_DESCRIPTION_ERROR_CODE, NO_DATA_ERROR_CODE);


    @Value("${pgu.lkapi-url}")
    private String lkApiUrl;

    @Value("${pgu.dictionary-url}")
    private String nsiApiUrl;

    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;

    @Override
    public MedDictionaryResponse getSmevResponse(String sessionId, Integer smevVersion, FieldComponent component) {
        if (smevVersion == 2) return sendRequest(getValidatePersonSmev2(sessionId, component));
        if (smevVersion == 3) return sendRequest(getValidatePatientSmev3(sessionId, component));
        throw new FormBaseWorkflowException("Ошибка обращения к сервису. Неверная версия сервиса: " + smevVersion);
    }

    @Override
    public MedDictionaryResponse getSpecialists(MedicalItemsRequest requestBody) {
        return sendRequest(requestBody);
    }

    private MedDictionaryResponse sendRequest(MedicalItemsRequest itemsRequest) {
        HttpEntity<MedicalItemsRequest> request = new HttpEntity<>(itemsRequest, prepareAuthCookieHeaders(userPersonalData.getToken()));
        var req = restTemplate.exchange(lkApiUrl + API_URL_REF_ITEMS, HttpMethod.POST, request, MedDictionaryResponse.class);
        if (req.getStatusCode().is2xxSuccessful()) {
            MedDictionaryResponse response = req.getBody();
            handleErrorInResponse(response);
            return response;
        }
        throw new FormBaseWorkflowException("Ошибка обращения к сервису");
    }

    private MedicalItemsRequest getValidatePatientSmev3(String sessionId, FieldComponent component) {
        List<MedicalSimpleFilter> filterParams = new ArrayList<>();
        Map<String, String> filterElements = new HashMap<>(component.getArguments());
        filterElements.put("esiaId", userPersonalData.getUserId().toString());
        filterElements.put(SESSION_ID, sessionId);
        filterElements.put("userSnils", userPersonalData.getPerson().getSnils());
        updateEmptyPatientSnils(filterElements);
        filterElements.forEach((key, value) -> {
            if (!IGNORE_ARGUMENTS.contains(key)) {
                var simpleItem = MedicalSimpleItem.builder()
                        .value(value)
                        .attributeName(key)
                        .condition("EQUALS")
                        .checkAllValues(true)
                        .build();
                var simpleFilter = MedicalSimpleFilter.builder().simple(simpleItem).build();
                filterParams.add(simpleFilter);
            }
        });
        var medicalItemsUnion = MedicalItemsUnion.builder().subs(filterParams).unionKind("AND").build();
        var medicalItemsFilter = MedicalItemsFilter.builder().union(medicalItemsUnion).build();

        return MedicalItemsRequest.builder()
                .eserviceId(component.getArgument("ESERVICEID_V3"))
                .refName("getValidatePatient")
                .treeFiltering("ONELEVEL")
                .filter(medicalItemsFilter)
                .build();
    }

    private void updateEmptyPatientSnils(Map<String, String> filterElements) {
        filterElements.computeIfPresent("patientSnils", (k, v) -> v.startsWith("answer.") ? "" : v);
    }

    private MedicalItemsRequest getValidatePersonSmev2(String sessionId, FieldComponent component) {
        String[] selectAttributes = {"Patient_Id"};
        Map<String, String> argToFilter = new HashMap<>();
        argToFilter.put("firstName", "First_Name");
        argToFilter.put("lastName", "Last_Name");
        argToFilter.put("middleName", "Middle_Name");
        argToFilter.put("sex", "Sex");
        argToFilter.put("OMS_Number", "OMS_Number");
        argToFilter.put("OMS_Series", "OMS_Series");
        argToFilter.put("birthdate", "Birthdate");
        argToFilter.put("patientSnils", "SNILS");
        argToFilter.put("regionCode", "Reg_Code");

        Map<String, String> filterElements = new HashMap<>(component.getArguments());
        String genderConverted = "1".equals(filterElements.get("sex")) ? "M" : "F";
        filterElements.put("sex", genderConverted);
        updateEmptyPatientSnils(filterElements);

        List<MedicalSimpleFilter> filterParams = new ArrayList<>();
        filterElements.forEach((key, value) -> {
            if (!IGNORE_ARGUMENTS.contains(key) && argToFilter.containsKey(key)) {
                var simpleItem = MedicalSimpleItem.builder()
                        .value(value)
                        .attributeName(argToFilter.get(key))
                        .condition("EQUALS")
                        .checkAllValues(false)
                        .build();
                var simpleFilter = MedicalSimpleFilter.builder().simple(simpleItem).build();
                filterParams.add(simpleFilter);
            }
        });
        filterParams.add(getSessionIdItem(sessionId));

        var medicalItemsUnion = MedicalItemsUnion.builder().subs(filterParams).unionKind("AND").build();
        var medicalItemsFilter = MedicalItemsFilter.builder().union(medicalItemsUnion).build();
        return MedicalItemsRequest.builder()
                .eserviceId(component.getArgument("ESERVICEID_V2"))
                .refName("ValidatePerson")
                .parentRefItemValue(null)
                .treeFiltering("ONELEVEL")
                .filter(medicalItemsFilter)
                .params(List.of(ParamsItem
                        .builder()
                        .name("region")
                        .value(filterElements.get("regionCode"))
                        .build()
                ))
                .selectAttributes(selectAttributes)
                .userSelectedRegion(component.getArgument("checkRegionCode"))
                .build();
    }

    private MedicalSimpleFilter getSessionIdItem(String sessionId) {
        var item = MedicalSimpleItem.builder()
                .value(sessionId)
                .attributeName(SESSION_ID)
                .condition(null)
                .checkAllValues(false)
                .build();

        return MedicalSimpleFilter.builder().simple(item).build();
    }

    /**
     * Проверяет есть ли ошибка в ответе, вырезает код ошибки из текста и кладёт его в отдельное поле
     * @param response ответ из справочника
     */
    private void handleErrorInResponse(MedDictionaryResponse response) {
        if (response != null && response.getError() != null && response.getError().getErrorDetail() != null) {
            MedDictionaryResponseErrorDetail errorDetail = response.getError().getErrorDetail();
            if (errorDetail.getErrorCode() != 0) {
                for (String errorCode: ERROR_CODES) {
                    String regexp = errorCode + ":";
                    if (errorDetail.getErrorMessage().startsWith(regexp)) {
                        errorDetail.setErrorCodeTxt(errorCode);
                        errorDetail.setErrorMessage(getValueByRegex(regexp, pattern -> pattern.matcher(errorDetail.getErrorMessage()).replaceFirst("")));
                        break;
                    }
                }
                if (!StringUtils.hasText(errorDetail.getErrorCodeTxt())) {
                    errorDetail.setErrorCodeTxt(NO_DATA_ERROR_CODE);
                }
            }
        }
    }
}
