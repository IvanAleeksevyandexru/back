package ru.gosuslugi.pgu.pgu_common.gibdd.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.ExternalServiceCallResult;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.GibddServiceResponse;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.OwnerPeriod;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.OwnerVehiclesRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.RegAction;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleFullInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleInfoRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.mapper.VehicleFullInfoMapper;
import ru.gosuslugi.pgu.pgu_common.gibdd.mapper.VehicleInfoMapper;
import ru.gosuslugi.pgu.pgu_common.gibdd.service.GibddDataService;
import ru.gosuslugi.pgu.pgu_common.gibdd.util.DateUtils;
import ru.gosuslugi.pgu.pgu_common.gibdd.util.NsiDictionaryUtil;
import ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Сервис интеграция с витриной ГИБДД
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Setter
public class GibddDataServiceImpl implements GibddDataService {

    @Value("${mock.gibdd.enabled}")
    private boolean mockEnabled;

    @Value("${mock.gibdd.url:#{null}}")
    private String mockPath;

    private final NsiDictionaryService nsiDictionaryService;
    private final VehicleInfoMapper vehicleInfoMapper;
    private final VehicleFullInfoMapper vehicleFullInfoMapper;

    private static final String NSI_V2_RESOURCE_URL = "nsiv2/internal/api/nsi/v1/dictionary";
    private static final String SHOWCASE_VIN_SERVICE_NAME = "ShowcaseVIN";
    private static final String SHOWCASE_OWNER_SERVICE_NAME = "ShowcaseOwner";
    private static final String FEDERAL_NOTARY_SERVICE_NAME = "CheckTSinPledge";

    private static final String NSI_V1_RESOURCE_URL = "api/nsi/v1/dictionary";
    private static final String GIBDD_RECORD_STATUS_DICTIONARY_NAME = "GIBDD_RECORD_STATUS";
    private static final String GIBDD_OWNER_TYPE_DICTIONARY_NAME = "GIBDD_OWNER_TYPE";
    private static final String EKOKLASS_MVD_DICTIONARY_NAME = "ekoklass_MVD";

    private static final String FEDERAL_NOTARY_SERVICE_PARAMS_STUB = "1";
    private static final Set<String> FEDERAL_NOTARY_SERVICE_EXPECTED_VALUES = Set.of("OK", "NO_DATA");

    private static final String NO_DATA_VALUE = "NO_DATA";
    private static final String MAIN_ATTR = "main";
    private static final String REGISTRATION_DOC_ATTR = "RegistrationDoc";
    private static final String PTS_ATTR = "PTS";
    private static final String REG_ACTION_ATTR = "RegAction";
    private static final String OWNERS_ATTR = "Owners";
    private static final String OWNER_ATTR = "Owner";
    private static final String RESTRICTION_ATTR = "RestrInfoElement";
    private static final String SEARCHING_SPEC_ATTR = "SearchingSpec";

    private static final String SERVICE_SUCCESS_MESSAGE = "operation completed";

    /**
     * Получение данных о ТС из Витрины ГИБДД
     * @param request   Запрос на получение данных
     * @return          Результат вызова сервиса витрины ГИБДД
     */
    @Async("gibddExecutor")
    @Override
    public CompletableFuture<GibddServiceResponse<VehicleInfo>> getAsyncVehicleInfo(VehicleInfoRequest request) {
        GibddServiceResponse<VehicleInfo> result = new GibddServiceResponse<>();
        try {
            NsiDictionary dictionary = getVehicleNsiDictionary(request);
            handleError(dictionary);
            if (!SERVICE_SUCCESS_MESSAGE.equals(dictionary.getError().getMessage())) {
                result.setExternalServiceCallResult(ExternalServiceCallResult.NOT_FOUND_ERROR);
            } else {
                result.setData(convertToVehicleInfo(dictionary));
            }
        } catch (RestClientException | ExternalServiceException e) {
            result.setExternalServiceCallResult(ExternalServiceCallResult.EXTERNAL_SERVER_ERROR);
            result.setErrorMessage(e.getMessage());
        }
        return CompletableFuture.completedFuture(result);
    }

    public VehicleFullInfo getVehicleFullInfo(VehicleInfoRequest request) {
        NsiDictionary dictionary = getVehicleNsiDictionary(request);
        if (!SERVICE_SUCCESS_MESSAGE.equals(dictionary.getError().getMessage())) {
            return null;
        }
        return convertToVehicleFullInfo(dictionary, request.isHasSensitiveData());
    }

    /**
     * Получение данных о ТС из Витрины ГИБДД
     * @param request   Запрос на получение данных
     * @return          Информация о ТС
     */
    @Override
    public List<VehicleFullInfo> getOwnerVehiclesInfo(OwnerVehiclesRequest request) {
        Map<String, String> params = Map.of(
                "LastName",         request.getLastName(),
                "FirstName",        request.getFirstName(),
                "BirthDay",         request.getBirthDay(),
                "TypeUser",         request.getUserType(),
                "NameDoc_Ind",      request.getDocumentType().getCode().toString(),
                "SerNumDoc_Ind",    request.getDocumentNumSer()
        );
        try {
            NsiDictionary dictionary = nsiDictionaryService.getDictionary(
                    getResourceUrl(NSI_V2_RESOURCE_URL),
                    SHOWCASE_OWNER_SERVICE_NAME,
                    NsiDictionaryUtil.getFilterRequest(params, request.getTx())
            );
            handleError(dictionary);
            return convertToVehicleFullInfoList(dictionary);
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    /**
     * Получение данных о ТС из Федеральной нотариальной палаты
     * @param request   Запрос на получение данных
     * @return          Результат вызова метода Федеральной нотариальной палаты
     */
    @Override
    public FederalNotaryInfo getFederalNotaryInfo(FederalNotaryRequest request) {
        Map<String, String> params = Map.of(
                "orderId",          request.getOrderId(),
                "Department",       FEDERAL_NOTARY_SERVICE_PARAMS_STUB,
                "ServiceCode",      FEDERAL_NOTARY_SERVICE_PARAMS_STUB,
                "TargetCode",       FEDERAL_NOTARY_SERVICE_PARAMS_STUB,
                "StatementDate",    LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                "vin",              VehicleInfoMapperUtil.convertToLatin(request.getVin())
        );

        try {
            NsiDictionary dictionary = nsiDictionaryService.getDictionary(
                    getResourceUrl(NSI_V2_RESOURCE_URL),
                    FEDERAL_NOTARY_SERVICE_NAME,
                    NsiDictionaryUtil.getFilterRequest(params, request.getTx())
            );
            handleError(dictionary);
            if (dictionary.getItems() == null
                    || dictionary.getItems().isEmpty()
                    || !FEDERAL_NOTARY_SERVICE_EXPECTED_VALUES.contains(dictionary.getItems().get(0).getValue())) {
                throw new ExternalServiceException("Витрина нотариальной палаты вернула некорректный результат");
            }
            return new FederalNotaryInfo(dictionary.getItems().get(0).getValue().equals("OK"));
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    @Async("gibddExecutor")
    @Override
    public CompletableFuture<GibddServiceResponse<FederalNotaryInfo>> getAsyncFederalNotaryInfo(FederalNotaryRequest request) {
        GibddServiceResponse<FederalNotaryInfo> result = new GibddServiceResponse<>();
        try {
            FederalNotaryInfo notaryInfo = getFederalNotaryInfo(request);
            result.setData(notaryInfo);
        } catch(RestClientException | ExternalServiceException e) {
            result.setExternalServiceCallResult(ExternalServiceCallResult.EXTERNAL_SERVER_ERROR);
            result.setErrorMessage(e.getMessage());
        }
        return CompletableFuture.completedFuture(result);
    }

    private NsiDictionary getVehicleNsiDictionary(VehicleInfoRequest request) {
        // todo - нужно добавить обработку запроса с СТС + гос. номер - пока делаем так
        if (StringUtils.isEmpty(request.getVin())) {
            request.setVin(request.getSts() + request.getGovRegNumber());
        }

        Map<String, String> params = new HashMap<>() {{
            put("LastName",     request.getLastName());
            put("FirstName",    request.getFirstName());
            put("VIN",          VehicleInfoMapperUtil.convertToLatin(request.getVin()));
            if (Objects.nonNull(request.getGovRegNumber())) {
                put("GovRegNumber", VehicleInfoMapperUtil.convertToLatin(request.getGovRegNumber()));
            }
            if (!StringUtils.isEmpty(request.getMiddleName())) {
                put("MiddleName", request.getMiddleName());
            }
        }};

        NsiDictionaryFilterRequest filterRequest = NsiDictionaryUtil.getFilterRequest(params, request.getTx());
        NsiDictionary dictionary = nsiDictionaryService.getDictionary(
                getResourceUrl(NSI_V2_RESOURCE_URL),
                SHOWCASE_VIN_SERVICE_NAME,
                filterRequest
        );
        handleError(dictionary);
        return dictionary;
    }

    private List<VehicleFullInfo> convertToVehicleFullInfoList(NsiDictionary dictionary) {
        List<VehicleFullInfo> result = new ArrayList<>();
        List<NsiDictionaryItem> dictionaryItems = dictionary.getItems();
        if (CollectionUtils.isEmpty(dictionaryItems)
                || (dictionaryItems.size() == 1
                && NO_DATA_VALUE.equals(dictionaryItems.get(0).getValue()))) {
            return result;
        }

        Collection<List<NsiDictionaryItem>> vehiclesItems = dictionary.getItems().stream()
                .collect(Collectors.groupingBy(NsiDictionaryItem::getValue))
                .values();

        Map<String, String> ownerTypeMap = getNsiDictionaryItemsValueMap(GIBDD_OWNER_TYPE_DICTIONARY_NAME);
        Map<String, String> statusMap = getNsiDictionaryItemsValueMap(GIBDD_RECORD_STATUS_DICTIONARY_NAME);
        Map<String, String> ecologyClassMap = getNsiDictionaryItemsValueMap(EKOKLASS_MVD_DICTIONARY_NAME);
        result = vehiclesItems.stream()
                .map(it -> convertToVehicleFullInfo(it, ownerTypeMap, statusMap, ecologyClassMap, true))
                .sorted(Comparator.comparing(it -> it.getModelMarkName() != null ? it.getModelMarkName() : it.getMarkName()))
                .collect(Collectors.toList());

        return result;
    }

    private VehicleInfo convertToVehicleInfo(NsiDictionary dictionary) {
        List<NsiDictionaryItem> items = dictionary.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return new VehicleInfo();
        }

        VehicleInfo vehicleInfo = vehicleInfoMapper.toVehicleInfo(joinVehicleNsiAttributeValues(items));

        Map<String, String> ownerTypeMap = getNsiDictionaryItemsValueMap(GIBDD_OWNER_TYPE_DICTIONARY_NAME);
        List<RegAction> regActions = new ArrayList<>();
        List<OwnerPeriod> ownerPeriods = new ArrayList<>();
        for (NsiDictionaryItem item : dictionary.getItems()) {
            String value = item.getValue();
            if (StringUtils.isEmpty(value)) {
                continue;
            }

            if (value.startsWith(REG_ACTION_ATTR)) {
                regActions.add(vehicleInfoMapper.toRegAction(item));
            } else if (value.startsWith(OWNER_ATTR)) {
                OwnerPeriod ownerPeriod = vehicleInfoMapper.toOwnerPeriod(item);
                ownerPeriod.setOwnerType(ownerTypeMap.get(ownerPeriod.getOwnerType()));
                ownerPeriods.add(ownerPeriod);
            } else if (value.startsWith(RESTRICTION_ATTR)) {
                vehicleInfo.getRestrictions().add(vehicleInfoMapper.toRestrictionsInformation(item));
            }
        }

        setVehicleStatus(vehicleInfo, getNsiDictionaryItemsValueMap(GIBDD_RECORD_STATUS_DICTIONARY_NAME));
        setVehicleEcologyClassDesc(vehicleInfo, getNsiDictionaryItemsValueMap(EKOKLASS_MVD_DICTIONARY_NAME));
        setVehicleLastRegActionName(vehicleInfo, regActions);
        setVehicleLegals(vehicleInfo);
        setOwnerPeriods(vehicleInfo, ownerPeriods);

        return vehicleInfo;
    }

    private VehicleFullInfo convertToVehicleFullInfo(NsiDictionary dictionary, boolean hasSensitiveData) {
        return convertToVehicleFullInfo(
                dictionary.getItems(),
                getNsiDictionaryItemsValueMap(GIBDD_OWNER_TYPE_DICTIONARY_NAME),
                getNsiDictionaryItemsValueMap(GIBDD_RECORD_STATUS_DICTIONARY_NAME),
                getNsiDictionaryItemsValueMap(EKOKLASS_MVD_DICTIONARY_NAME),
                hasSensitiveData
        );
    }

    private VehicleFullInfo convertToVehicleFullInfo(
            List<NsiDictionaryItem> dictionaryItems,
            Map<String, String> ownerTypeMap,
            Map<String, String> statusMap, Map<String, String> ecologyClassMap,
            boolean hasSensitiveData
    ) {
        VehicleFullInfo vehicleInfo;

        if (hasSensitiveData) {
            vehicleInfo = vehicleFullInfoMapper.toVehicleInfo(joinVehicleNsiAttributeValues(dictionaryItems));
        } else {
            vehicleInfo = vehicleFullInfoMapper.toProtectedVehicleInfo(joinVehicleNsiAttributeValues(dictionaryItems));
        }

        List<OwnerPeriod> ownerPeriods = new ArrayList<>();
        for (NsiDictionaryItem item : dictionaryItems) {
            String itemTitle = item.getTitle();
            if (StringUtils.isEmpty(itemTitle) || CollectionUtils.isEmpty(item.getAttributeValues())) {
                continue;
            }

            if (itemTitle.startsWith(REG_ACTION_ATTR)) {
                vehicleInfo.getRegActions().add(vehicleFullInfoMapper.toRegAction(item));
            } else if (itemTitle.startsWith(OWNERS_ATTR)) {
                vehicleInfo.setOwner(vehicleFullInfoMapper.toOwner(item));
            } else if (itemTitle.startsWith(OWNER_ATTR)) {
                OwnerPeriod ownerPeriod = vehicleInfoMapper.toOwnerPeriod(item);
                ownerPeriod.setOwnerType(ownerTypeMap.get(ownerPeriod.getOwnerType()));
                ownerPeriods.add(ownerPeriod);
            } else if (itemTitle.startsWith(RESTRICTION_ATTR)) {
                if (hasSensitiveData) {
                    vehicleInfo.getRestrictions().add(vehicleFullInfoMapper.toRestrictionsInformation(item));
                } else {
                    vehicleInfo.getRestrictions().add(vehicleFullInfoMapper.toProtectedRestrictionsInformation(item));
                }
            } else if (itemTitle.startsWith(SEARCHING_SPEC_ATTR)) {
                vehicleInfo.setSearchingSpec(vehicleFullInfoMapper.toSearchingSpec(item));
            }
        }

        setVehicleStatus(vehicleInfo, statusMap);
        setVehicleEcologyClassDesc(vehicleInfo, ecologyClassMap);
        setVehicleLastRegActionName(vehicleInfo, vehicleInfo.getRegActions());
        setVehicleLegals(vehicleInfo);
        setOwnerPeriods(vehicleInfo, ownerPeriods);

        return vehicleInfo;
    }

    private NsiDictionaryItem joinVehicleNsiAttributeValues(List<NsiDictionaryItem> items) {
        List<String> baseInfoAttrs = List.of(MAIN_ATTR, REGISTRATION_DOC_ATTR, PTS_ATTR);
        Map<String, String> baseInfoMap = items.stream()
                .filter(item -> item.getValue() == null || (item.getTitle() != null && baseInfoAttrs.contains(item.getTitle())))
                .flatMap(m -> m.getAttributeValues().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        NsiDictionaryItem nsiDictionaryItem = new NsiDictionaryItem();
        nsiDictionaryItem.setAttributeValues(baseInfoMap);
        return nsiDictionaryItem;
    }

    private void setVehicleStatus(VehicleInfo vehicleInfo, Map<String, String> statusMap) {
        if (vehicleInfo.getRecordStatus() != null) {
            vehicleInfo.setStatus(statusMap.getOrDefault(vehicleInfo.getRecordStatus(), ""));
        }
    }

    private void setVehicleEcologyClassDesc(VehicleInfo vehicleInfo, Map<String, String> map) {
        if (vehicleInfo.getEcologyClass() != null) {
            vehicleInfo.setEcologyClassDesc(map.get(vehicleInfo.getEcologyClass()));
        }
    }

    private void setVehicleLastRegActionName(VehicleInfo vehicleInfo, List<RegAction> regActions) {
        vehicleInfo.setLastRegActionName(regActions
                .stream()
                .filter(it -> it.getRegDate() != null)
                .max(Comparator.comparing(RegAction::getRegDate))
                .map(RegAction::getRegActionName)
                .orElse(null));
    }

    private void setVehicleLegals(VehicleInfo vehicleInfo) {
        if ((vehicleInfo.getSearchingTransportFlag() != null && vehicleInfo.getSearchingTransportFlag())
                || (vehicleInfo.getRestrictionsFlag() != null && vehicleInfo.getRestrictionsFlag())) {
            vehicleInfo.setLegals(false);
        }
    }

    private void setOwnerPeriods(VehicleInfo vehicleInfo, List<OwnerPeriod> ownerPeriods) {
        if (!ownerPeriods.isEmpty()) {
            ownerPeriods.sort(Comparator.comparing(OwnerPeriod::getDateStart,
                    (s1, s2) -> s1 == null ? Integer.MIN_VALUE : s2 == null ? Integer.MAX_VALUE : s1.compareTo(s2)));
            ownerPeriods = ownerPeriods.stream()
                    .peek(it -> {
                        it.setDateStart(DateUtils.formatDate(it.getDateStart()));
                        it.setDateEnd(DateUtils.formatDate(it.getDateEnd())); })
                    .collect(Collectors.toList());
            vehicleInfo.getOwnerPeriods().addAll(ownerPeriods);
        }
    }

    private void handleError(NsiDictionary dictionary) {
        if (dictionary == null) {
            throw new ExternalServiceException("Внешний сервис не вернул результат");
        }

        if (dictionary.getError() != null && dictionary.getError().getCode() != 0) {
            throw new ExternalServiceException(String.format("Код: %s, сообщение: %s", dictionary.getError().getCode(), dictionary.getError().getMessage()));
        }
    }

    private Map<String, String> getNsiDictionaryItemsValueMap(String dictionaryName) {
        try {
            NsiDictionary dictionary = nsiDictionaryService.getDictionary(
                    getResourceUrl(NSI_V1_RESOURCE_URL),
                    dictionaryName,
                    NsiDictionaryUtil.getSimpleRequest()
            );
            if (dictionary != null && dictionary.getItems() != null) {
                return dictionary.getItems().stream().collect(Collectors.toMap(NsiDictionaryItem::getValue, NsiDictionaryItem::getTitle));
            }
            return new HashMap<>();
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    private String getResourceUrl(String resourceUrl) {
        return mockEnabled
                ? String.format("%s/%s", mockPath, resourceUrl)
                : resourceUrl;
    }
}
