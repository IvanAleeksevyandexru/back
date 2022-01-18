package ru.gosuslugi.pgu.fs.pgu.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.atc.carcass.common.exception.FaultException;
import ru.gosuslugi.pgu.fs.common.exception.CreateOrderException;
import ru.gosuslugi.pgu.fs.common.exception.DuplicateValueForOrderFoundException;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.exception.dto.error.ErrorMessage;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.PersonIdentifier;
import ru.gosuslugi.pgu.core.lk.model.feed.Feed;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.core.lk.model.order.dto.request.SetOrderAttributeDTO;
import ru.gosuslugi.pgu.core.service.client.DeserializeCallback;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;
import ru.gosuslugi.pgu.fs.exception.OrderCreationException;
import ru.gosuslugi.pgu.fs.exception.OrderNotCreatedException;
import ru.gosuslugi.pgu.fs.pgu.client.PguClient;
import ru.gosuslugi.pgu.fs.pgu.dto.*;
import ru.gosuslugi.pgu.fs.pgu.dto.feed.FeedDto;
import ru.gosuslugi.pgu.fs.pgu.service.UserTokenService;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Slf4j
@Component
@RequiredArgsConstructor
public class PguClientImpl implements PguClient {

    private static final String SEND_INVITATION_URL = "api/lk/v1/orders/{orderId}}/invitations/inviteToSign/send";

    @Value("${pgu.order-url}")
    private String pguUrl;

    @Value("${pgu.lkapi-url}")
    private String lkApiUrl;

    @Value("${pgu.catalog-url}")
    private String catalogUrl;

    private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final String LK_API_CREATE_ORDER_PATH = "/lk-api/internal/api/lk/v1/orders";
    private static final String LK_API_FIND_ORDER_PATH = "/api/lk/v1/orders/drafts?_={preventCacheValue}&embed=LAST_INVITATION&pageIndex=1&pageSize={pageSize}&sourceSystems=*&eServiceId={serviceId}&serviceTargetId={targetId}";
    private static final String LK_API_FIND_ORDER_LIGHT_PATH = "/api/lk/v1/orders/draftsLight?_={preventCacheValue}&embed=LAST_INVITATION&pageIndex=1&pageSize={pageSize}&sourceSystems=*&eServiceId={serviceId}&serviceTargetId={targetId}";
    private static final String LK_API_ORDERS_PATH = "api/lk/v1/orders/{orderId}";
    private static final String LK_API_CREATE_ORDER_PATH_HIGHLOAD = "/create-order-service-api/internal/api/lk/v1/orders/create-hp";
    private static final String LK_API_CHECK_ORDER_EXISTS = "/lk-api/internal/api/orders/v1/create-hp/{orderId}/check";
    private static final String LK_API_GET_ORDERS_BY_SERVICE_ID = "api/lk/v1/orders?eserviceCode={serviceId}&serviceTargetExtId={targetId}&pageIndex=1&pageSize=1000&sourceSystems=*";
    private static final String LK_API_FIND_ORDER_WITH_PAYMENT_PATH = "api/lk/v1/orders/{orderId}?embed=PAYMENT";
    private static final String LK_API_CHECK_ORDER = "/api/lk/v1/orders/check/{serviceCode}/{targetCode}";
    private static final String LK_API_SAVE_VALUES = "/api/lk/v1/orders/check/add/";
    private static final String LK_API_CHECK_ORDER_MNEMONIC = "/api/sf/v1/player/forms/{mnemonic}/init";
    private static final String LK_API_GET_PASSPORT_AND_TARGET_CODES = "/api/catalog/v3/services/{targetCode}_{serviceCode}/convert/new?platform=EPGU_V4";
    private static final String LK_API_SET_TECH_STATUS = "lk-api/internal/api/lk/v1/orders/{orderId}/status/tech";
    private static final String LK_API_FEEDS_PATH = "lk-api/api/lk/v1/feeds/{type}/{id}";
    private static final String LK_API_USER_AUTHORITY_CHECK = "/api/lk/v1/users/data/authority/{authorityId}?extId={targetId}";
    private static final String LK_API_ORDER_SET_ATTRIBUTES = "lk-api/internal/api/lk/v1/orders/{orderId}/set/attributes";


    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;
    private final ErrorModalDescriptorService errorModalDescriptorService;
    private final UserTokenService userTokenService;

    public FeedDto findFeed(Feed.FeedType type, Long id) {
        if (id == null) return null;

        Map<String, String> requestParams = Map.of(
                "type", type.name(),
                "id", String.valueOf(id)
        );

        try {
            ResponseEntity<FeedDto> response = restTemplate.exchange(
                    lkApiUrl + LK_API_FEEDS_PATH,
                    HttpMethod.GET,
                    new HttpEntity<>(this.prepareHeaders()),
                    FeedDto.class,
                    requestParams
            );
            return response.getBody();
        } catch (EntityNotFoundException e) {
            log.warn("Unable to find feed for type {} with id {}", type, id);
            return null;
        }
    }

    public Order findLastOrder(String serviceCode, String targetCode) {
        Map<String, String> requestParametrs = Map.of(
                "serviceId", serviceCode,
                "targetId", targetCode,
                "preventCacheValue", this.generatePreventCacheValue(),
                "pageSize", "1");
        try {
            ResponseEntity<ListOrderResponse> response = restTemplate
                    .exchange(pguUrl + LK_API_FIND_ORDER_PATH,
                            HttpMethod.GET,
                            new HttpEntity<>(this.prepareHeaders()),
                            ListOrderResponse.class,
                            requestParametrs);
            if (Objects.nonNull(response.getBody())) {
                if (Objects.nonNull(response.getBody().getOrder())) {
                    return response.getBody().getOrder().size() > 0 ? response.getBody().getOrder().get(0) : null;
                }
            }
        } catch (ExternalServiceException | RestClientException e) {
            return null;
        }
        return null;
    }

    public List<Order> findOrders(String serviceCode, String targetCode) {
        return findOrders(serviceCode, targetCode, new HttpEntity<>(this.prepareHeaders()));
    }

    public List<Order> findOrders(String serviceCode, String targetCode, Long userId) {
        return findOrders(serviceCode, targetCode, new HttpEntity<>(this.prepareHeaders(userId)));
    }

    private List<Order> findOrders(String serviceCode, String targetCode, HttpEntity<ListOrderResponse> httpEntity) {
        Map<String, String> requestParametrs = Map.of(
                "serviceId", serviceCode,
                "targetId", targetCode,
                "preventCacheValue", this.generatePreventCacheValue(),
                "pageSize", "1000");
        ResponseEntity<ListOrderResponse> response = restTemplate
                .exchange(pguUrl + LK_API_FIND_ORDER_PATH,
                        HttpMethod.GET,
                        httpEntity,
                        ListOrderResponse.class,
                        requestParametrs);
        if (Objects.nonNull(response.getBody())) {
            if (Objects.nonNull(response.getBody().getOrder())) {
                return response.getBody().getOrder().size() > 0 ? response.getBody().getOrder() : emptyList();
            }
        }
        return emptyList();
    }

    @Override
    public List<Order> findOrdersByStatus(String serviceCode, String targetCode, OrderStatuses orderStatus) {
        Map<String, String> requestParams = Map.of("serviceId", serviceCode,"targetId", targetCode);
        ResponseEntity<ListOrderResponse> response = restTemplate
                .exchange(pguUrl + LK_API_GET_ORDERS_BY_SERVICE_ID,
                        HttpMethod.GET,
                        new HttpEntity<>(this.prepareHeaders()),
                        ListOrderResponse.class,
                        requestParams);
        if (Objects.nonNull(response.getBody()) && Objects.nonNull(response.getBody().getOrder()))
            return response.getBody().getOrder().stream()
                    .filter(order -> orderStatus.getStatusId() == order.getOrderStatusId())
                    .collect(Collectors.toList());
        return emptyList();
    }

    @Override
    public List<Order> findOrdersWithoutStatuses(String serviceCode, String targetCode, List<Long> ignoreOrderStatuses) {
        Map<String, String> requestParams = Map.of("serviceId", serviceCode,"targetId", targetCode);
        ResponseEntity<ListOrderResponse> response = restTemplate
                .exchange(pguUrl + LK_API_GET_ORDERS_BY_SERVICE_ID,
                        HttpMethod.GET,
                        new HttpEntity<>(this.prepareHeaders()),
                        ListOrderResponse.class,
                        requestParams);
        if (Objects.nonNull(response.getBody()) && Objects.nonNull(response.getBody().getOrder()))
            return response.getBody().getOrder().stream()
                    .filter(order -> !ignoreOrderStatuses.contains(order.getOrderStatusId()))
                    .collect(Collectors.toList());
        return emptyList();
    }

    public Order createOrder(PoweredOrderWithAuthDTO orderDto) {
        try {
            var resp = restTemplate.exchange(lkApiUrl + LK_API_CREATE_ORDER_PATH,
                    HttpMethod.POST,
                    new HttpEntity<>(orderDto,this.prepareHeaders()),
                    Order.class
            );
            return resp.getBody();
        } catch (Exception e) {
            log.error("Error creating order", e);
            Throwable cause = e.getCause();
            if (cause instanceof ExternalServiceException) {
                ExternalServiceException externalServiceException = (ExternalServiceException) e.getCause();
                ErrorMessage errorBody = externalServiceException.getErrorBody();
                throw new CreateOrderException(
                        errorModalDescriptorService.getErrorModal(
                                ErrorModalView.fromStringOrDefault(errorBody.getStatus(), ErrorModalView.DEFAULT_WARNING)
                        ),
                        externalServiceException.getMessage());
            }
            throw new FaultException("Error while create order", e);
        }
    }

    @Override
    public Boolean deleteOrder(Long orderId) {
        return deleteOrder(orderId, new HttpEntity<>(this.prepareHeaders()));
    }

    @Override
    public Boolean deleteOrder(Long orderId, Long userId) {
        return deleteOrder(orderId, new HttpEntity<>(this.prepareHeaders(userId)));
    }

    private Boolean deleteOrder(Long orderId, HttpEntity<Object> httpEntity) {
        try {
            ResponseEntity<Object> response = restTemplate
                    .exchange(pguUrl + LK_API_ORDERS_PATH,
                            HttpMethod.DELETE,
                            httpEntity,
                            Object.class,
                            Map.of("orderId", orderId));
        } catch (ExternalServiceException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error from external service", e);
            }
            return false;
        }
        return true;
    }

    private DeserializeCallback<Order, String> orderDeserializeCallback() {
        return order -> {
            try {
                return ru.gosuslugi.lk.api.order.Order.toObject(new JSONObject(order));
            } catch (JSONException e) {
                throw new FaultException("Couldn't parse order response " + order, e);
            }
        };
    }

    @Override
    public Boolean sendEmailInvitationToParticipant(Long orderId, PersonIdentifier participant) {
        HttpEntity<List<PersonIdentifier>> entity = new HttpEntity<>(List.of(participant), prepareHeaders());
        try {
            restTemplate.exchange(pguUrl + SEND_INVITATION_URL,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {},
                    Map.of("orderId", orderId));
            return Boolean.TRUE;
        } catch (ExternalServiceException ex) {
            log.error("Send invitation to participant error: {}", ex);
            throw new ErrorModalException(errorModalDescriptorService.getErrorModal(ErrorModalView.REPEATABLE_INVITATION), "Пока нельзя отправить");
        }
    }

    @Override
    public Order findOrderWithPayment(Long orderId) {
        try {
            ResponseEntity<Order> response = restTemplate
                    .exchange(pguUrl + LK_API_FIND_ORDER_WITH_PAYMENT_PATH,
                            HttpMethod.GET,
                            new HttpEntity<>(this.prepareHeaders()),
                            Order.class,
                            Map.of("orderId", orderId));
            return response.getBody();
        } catch (ExternalServiceException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error from external service", e);
            }
            return null;
        }
    }

    @Override
    public Order findOrderById(Long orderId) {
        try {
            ResponseEntity<Order> response = restTemplate
                    .exchange(pguUrl + LK_API_ORDERS_PATH,
                            HttpMethod.GET,
                            new HttpEntity<>(this.prepareHeaders()),
                            Order.class,
                            Map.of("orderId", orderId));
            return response.getBody();
        } catch (ExternalServiceException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatus())) {
                throw new ErrorModalException(errorModalDescriptorService.getErrorModal(ErrorModalView.REQUESTED_ORDER_DOES_NOT_EXIST), "Запрашиваемого черновика не существует");
            }
            log.error("Error from external service", e);
            throw new FaultException("Error while receiving order by id" , e);
        }
    }

    @Override
    public Order findOrderByIdAndUserId(Long orderId, Long userId) {
        try {
            ResponseEntity<Order> response = restTemplate
                    .exchange(pguUrl + LK_API_ORDERS_PATH,
                            HttpMethod.GET,
                            new HttpEntity<>(this.prepareHeaders(userId)),
                            Order.class,
                            Map.of("orderId", orderId));
            return response.getBody();
        } catch (ExternalServiceException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatus())) {
                throw new ErrorModalException(errorModalDescriptorService.getErrorModal(ErrorModalView.REQUESTED_ORDER_DOES_NOT_EXIST), "Запрашиваемого черновика не существует");
            }
            log.error("Error from external service", e);
            throw new FaultException("Error while receiving order by id" , e);
        }
    }

    private HttpHeaders prepareHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Cookie", "acc_t=" + userPersonalData.getToken());
        return httpHeaders;
    }

    private HttpHeaders prepareHeaders(Long userId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String token = userTokenService.getUserToken(userId);
        httpHeaders.add("Cookie", "acc_t=" + token);
        return httpHeaders;
    }

    private String generatePreventCacheValue() {
        return String.valueOf(Math.random());
    }

    @Override
    public Boolean hasDuplicatesForOrder(String serviceCode, String targetCode, Map<String, Object> userAnswers) {
        try {
            restTemplate.exchange(
                    pguUrl + LK_API_CHECK_ORDER,
                    HttpMethod.POST,
                    new HttpEntity<>(userAnswers, this.prepareHeaders()),
                    Map.class,
                    Map.of(
                            "serviceCode", serviceCode,
                            "targetCode", targetCode
                    ));
        } catch (ExternalServiceException e) {
            if (e.getStatus().value() == HttpStatus.BAD_GATEWAY.value() || e.getStatus().value() == HttpStatus.GATEWAY_TIMEOUT.value()) {
                log.info("Ошибка при проверке дублирующихся значений в заявлении. Сервис не отвечает.", e);
                throw new FormBaseException("Ошибка при проверке дублирующихся значений в заявлении. Сервис не отвечает.", e);
            }
            if (e.getStatus().value() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                log.info("Ошибка при проверке дублирующихся значений в заявлении. Ошибка сервера.", e);
                throw new FormBaseException("Ошибка при проверке дублирующихся значений в заявлении. Ошибка сервера.", e);
            }
            if (e.getStatus().value() == HttpStatus.BAD_REQUEST.value()) {
                throw new DuplicateValueForOrderFoundException("Ошибка при проверке дублирующихся значений в заявлении. " + e.getMessage(), e);
            }
        }
        return false;
    }

    @Override
    public Boolean saveChoosenValuesForOrder(String serviceCode, String targetCode, Long orderId, Map<String, Object> userAnswers) {
        try {
            HashMap<String, Object> requestBody = new HashMap<>(userAnswers);
            requestBody.put("orderId", orderId);
            restTemplate.exchange(
                    pguUrl + LK_API_SAVE_VALUES,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, this.prepareHeaders()),
                    Map.class
            );
        } catch (ExternalServiceException e) {
            throw new FaultException("Ошибка при сохранении выбранных значений для заявления." + e.getMessage(), e);
        }
        return true;
    }

    @Override
    public PguServiceCodes getPguServiceCodes(String serviceCode, String targetCode) {
        try {
            var response = restTemplate.exchange(
                    catalogUrl + LK_API_GET_PASSPORT_AND_TARGET_CODES,
                    HttpMethod.GET,
                    new HttpEntity<>(this.prepareHeaders()),
                    PguServiceCodes.class,
                    Map.of(
                            "targetCode", targetCode,
                            "serviceCode", serviceCode
                    )
            );
            return response.getBody();
        } catch (ExternalServiceException | EntityNotFoundException e) {
            throw new FaultException("Ошибка при получении passport и target кодов. "  + e.getMessage(), e);
        }
    }

    @Override
    public void setTechStatusToOrder(Long orderId, Long status) {
        Map<String, Object> requestBody = Map.of(
                "token", String.format("%d@@AL20@@1", userPersonalData.getUserId()),
                "statusCode", status
        );

        restTemplate.exchange(
                lkApiUrl + LK_API_SET_TECH_STATUS,
                HttpMethod.POST,
                new HttpEntity<>(requestBody),
                PguServiceCodes.class,
                Map.of(
                        "orderId", orderId
                )
        );
    }

    @Override
    public boolean checkAuthorityForService(String authorityId, String targetId) {
        if(Objects.isNull(authorityId)){
            return false;
        }
        try {
            restTemplate.exchange(
                    pguUrl + LK_API_USER_AUTHORITY_CHECK,
                    HttpMethod.GET,
                    new HttpEntity<>(this.prepareHeaders()),
                    Object.class,
                    Map.of(
                            "authorityId", authorityId,
                            "targetId", targetId
                    )
            );
        } catch (EntityNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public ru.gosuslugi.lk.api.order.Order setOrderAttributes(SetOrderAttributeDTO setOrderAttributeDTO, Long orderId) {
        try {
            var response = restTemplate.exchange(
                    lkApiUrl + LK_API_ORDER_SET_ATTRIBUTES,
                    HttpMethod.POST,
                    new HttpEntity<>(setOrderAttributeDTO),
                    ru.gosuslugi.lk.api.order.Order.class,
                    Map.of(
                            "orderId", orderId
                    )
            );
            return response.getBody();
        }catch (ExternalServiceException e) {
            log.error("Error from external service", e);
            throw new FaultException("Error while updating order attributes" +
                    " by id" , e);
        }
    }


    @Override
    public HighLoadOrderResponseDto createHighloadOrder(HighLoadOrderRequestDto highLoadOrderPguDto) {
        try {
            var resp = restTemplate.exchange(lkApiUrl + LK_API_CREATE_ORDER_PATH_HIGHLOAD,
                    HttpMethod.POST,
                    new HttpEntity<>(highLoadOrderPguDto,this.prepareHeaders()),
                    HighLoadOrderResponseDto.class
            );
            return resp.getBody();
        } catch (Exception e) {
            log.error("Error creating order", e);
            Throwable cause = e.getCause();
            if (cause instanceof ExternalServiceException) {
                ExternalServiceException externalServiceException = (ExternalServiceException) e.getCause();
                ErrorMessage errorBody = externalServiceException.getErrorBody();
                throw new CreateOrderException(
                        errorModalDescriptorService.getErrorModal(
                                ErrorModalView.fromStringOrDefault(errorBody.getStatus(), ErrorModalView.DEFAULT_WARNING)
                        ),
                        externalServiceException.getMessage());
            }
            throw new FaultException("Error while create order", e);
        }
    }

    @Override
    public void checkOrderExists(Long orderId) {
        try {
            var resp = restTemplate.exchange(lkApiUrl + LK_API_CHECK_ORDER_EXISTS,
                    HttpMethod.GET,
                    new HttpEntity<>(this.prepareHeaders()),
                    String.class,
                    Map.of("orderId", orderId)
            );
            if(resp.getStatusCode() == HttpStatus.ACCEPTED){
                throw new OrderNotCreatedException("Заявление не создано");
            }
            if(resp.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR){
                log.error("Error creating order = {}", orderId);
                throw new OrderCreationException("Ошибка при создании заявления");
            }
        }catch (Exception e){
            log.error("Error while checking order status", e);
            throw new OrderNotCreatedException("Заявление не создано");
        }
    }

    @Override
    public List<OrderLight> findOrdersLight(String serviceCode, String targetCode, Long userId) {
        var httpEntity = new HttpEntity<>(this.prepareHeaders(userId));
        Map<String, String> requestParametrs = Map.of(
                "serviceId", serviceCode,
                "targetId", targetCode,
                "preventCacheValue", this.generatePreventCacheValue(),
                "pageSize", "1000");
        ResponseEntity<ListOrderResponseLight> response = restTemplate
                .exchange(pguUrl + LK_API_FIND_ORDER_LIGHT_PATH,
                        HttpMethod.GET,
                        httpEntity,
                        ListOrderResponseLight.class,
                        requestParametrs);
        if (Objects.nonNull(response.getBody())) {
            if (Objects.nonNull(response.getBody().getOrder())) {
                return response.getBody().getOrder().size() > 0 ? response.getBody().getOrder() : emptyList();
            }
        }
        return emptyList();
    }
}
