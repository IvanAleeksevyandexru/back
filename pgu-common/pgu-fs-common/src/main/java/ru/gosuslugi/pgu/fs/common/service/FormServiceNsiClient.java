package ru.gosuslugi.pgu.fs.common.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import ru.atc.idecs.refregistry.ws.Condition;
import ru.atc.idecs.refregistry.ws.ListRefItemsRequest;
import ru.atc.idecs.refregistry.ws.ListRefItemsResponse;
import ru.atc.idecs.refregistry.ws.RefItem;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.core.service.client.ServiceClient;
import ru.gosuslugi.pgu.core.service.client.rest.RestRequest;
import ru.gosuslugi.pgu.fs.common.utils.NsiUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FormServiceNsiClient {
    private static final String NSI_PATH = "api/nsi/v1/dictionary/";

    private final ServiceClient<RestRequest> restClient;
    private final ObjectMapper objectMapper;
    private final String pguNsiUrl;

    public FormServiceNsiClient(ServiceClient<RestRequest> restClient, String pguNsiUrl) {
        this.restClient = restClient;
        this.pguNsiUrl = pguNsiUrl + NSI_PATH;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    public String getFirstItem(ListRefItemsRequest request, final String dictionaryName) throws Exception {
        if (Objects.nonNull(request) && !StringUtils.isBlank(dictionaryName)) {
            String requestBody = objectMapper.writeValueAsString(request);
            RestRequest restRequest = prepareRequest(dictionaryName, HttpMethod.POST, requestBody);
            String response = restClient.invoke(restRequest, String.class);
            Map<String, Object> mapResponse = JsonProcessingUtil.fromJson(response, LinkedHashMap.class);
            List nsiItem = (List) mapResponse.get("items");
            if (Objects.nonNull(nsiItem) && !nsiItem.isEmpty()) return objectMapper.writeValueAsString(nsiItem.get(0));
            throw new FormBaseException("Nsi error empty items in dictionary:" + dictionaryName);
        }
        throw new FormBaseException("Request or dictionary name cannot be empty");
    }

    /**
     * Получение элемента по запросу к nsi-справочникам.
     * @param attrName имя атрибута для фильтра
     * @param attrValue значение атрибута для фильтра
     * @param condition условие поиска
     * @param dictionaryName имя nsi-справочника
     * @return ответ с найденным элементом
     * @throws Exception исключение при вызове rest-сервиса.
     */
    public Optional<RefItem> getItem(String attrName, String attrValue, Condition condition, String dictionaryName) throws Exception {
        if (StringUtils.isNotEmpty(attrName) && StringUtils.isNotEmpty(attrValue) && !StringUtils.isBlank(dictionaryName)) {
            ListRefItemsRequest requestBody = NsiUtils.getSimpleRequestByAttr(
                    Map.of("pageSize", 1), Map.of(attrName, Map.entry(attrValue, condition))
            );
            String requestBodyStr = objectMapper.writeValueAsString(requestBody);
            RestRequest restRequest = prepareRequest(dictionaryName, HttpMethod.POST, requestBodyStr);
            Instant start = Instant.now();
            String response = restClient.invoke(restRequest, String.class);
            Instant finish = Instant.now();
            ListRefItemsResponse restResponse = JsonProcessingUtil.fromJson(response, ListRefItemsResponse.class);
            String message = String.format("Nsi error empty items in dictionary %s: method=[%s], path=[%s], responseBody=[%s], time=[%s]",
                    dictionaryName, restRequest.getMethod().name(), restRequest.getPath(), response, Duration.between(start, finish).toMillis());
            if (Objects.nonNull(restResponse) && restResponse.getTotal() > 0 && !CollectionUtils.isEmpty(restResponse.getItems())) {
                Optional<List<RefItem>> itemsOptional = Optional.of(restResponse).map(ListRefItemsResponse::getItems);
                if (itemsOptional.isPresent() && !itemsOptional.get().isEmpty()) {
                    return itemsOptional.get().stream().filter(i -> i.getValue().equals(attrValue)).findFirst();
                }
                throw new FormBaseException(message);
            }
            throw new FormBaseException(message);
        }
        throw new FormBaseException("Request attribute or dictionary name cannot be empty");
    }

    /**
     * Получение списка элементов по запросу к nsi-справочникам.
     * @param requestBody тело запроса
     * @param dictionaryName имя nsi-справочника
     * @return ответ со списком элементов
     * @throws Exception исключение при вызове rest-сервиса.
     */
    public ListRefItemsResponse getItems(ListRefItemsRequest requestBody, final String dictionaryName) throws Exception {
        if (Objects.nonNull(requestBody) && !StringUtils.isBlank(dictionaryName)) {
            String requestBodyStr = objectMapper.writeValueAsString(requestBody);
            RestRequest restRequest = prepareRequest(dictionaryName, HttpMethod.POST, requestBodyStr);
            Instant start = Instant.now();
            String response = restClient.invoke(restRequest, String.class);
            Instant finish = Instant.now();
            ListRefItemsResponse restResponse = JsonProcessingUtil.fromJson(response, ListRefItemsResponse.class);
            if (Objects.nonNull(restResponse) && restResponse.getTotal() > 0 && !CollectionUtils.isEmpty(restResponse.getItems())) {
                return restResponse;
            }
            String message = String.format("Nsi error empty items in dictionary %s: method=[%s], path=[%s], responseBody=[%s], time=[%s]",
                    dictionaryName, restRequest.getMethod().name(), restRequest.getPath(), response, Duration.between(start, finish).toMillis());
            throw new FormBaseException(message);
        }
        throw new FormBaseException("Request or dictionary name cannot be empty");
    }

    private RestRequest prepareRequest(String dictionaryName, HttpMethod method, Object body) {
        RestRequest restRequest = new RestRequest("", false, pguNsiUrl + dictionaryName, method, body);
        restRequest.getHeaders().put("Content-Type", "application/json;charset=UTF-8");
        return restRequest;
    }

}
