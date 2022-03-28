package ru.gosuslugi.pgu.fs.pgu.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.atc.carcass.common.exception.FaultException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.exception.dto.error.ErrorMessage;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.core.lk.model.order.dto.request.SetOrderAttributeDTO;
import ru.gosuslugi.pgu.fs.common.exception.CreateOrderException;
import ru.gosuslugi.pgu.fs.common.exception.DuplicateValueForOrderFoundException;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;
import ru.gosuslugi.pgu.fs.exception.OrderCreationException;
import ru.gosuslugi.pgu.fs.exception.OrderNotCreatedException;
import ru.gosuslugi.pgu.fs.pgu.client.PguOrderClient;
import ru.gosuslugi.pgu.fs.pgu.dto.HighLoadOrderRequestDto;
import ru.gosuslugi.pgu.fs.pgu.dto.HighLoadOrderResponseDto;
import ru.gosuslugi.pgu.fs.pgu.dto.ListOrderResponse;
import ru.gosuslugi.pgu.fs.pgu.dto.ListOrderResponseLight;
import ru.gosuslugi.pgu.fs.pgu.dto.OrderLight;
import ru.gosuslugi.pgu.fs.pgu.dto.PguServiceCodes;
import ru.gosuslugi.pgu.fs.pgu.dto.PoweredOrderWithAuthDTO;
import ru.gosuslugi.pgu.fs.pgu.service.UserTokenService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Slf4j
@Component
@RequiredArgsConstructor
public class PguOrderClientImpl implements PguOrderClient {

    @Value("${pgu.lkapi-url}")
    private String lkApiUrl;

    private static final String ORDER_ID_KEY = "orderId";
    
    private static final String LK_API_CREATE_ORDER_PATH = "/lk-api/internal/api/lk/v1/orders";
    private static final String LK_API_FIND_ORDER_PATH = "/lk-api/internal/api/lk/v1/drafts?_={preventCacheValue}&embed=LAST_INVITATION&pageIndex=1&pageSize={pageSize}&sourceSystems=*&eServiceId={serviceId}&serviceTargetId={targetId}";
    private static final String LK_API_ORDERS_PATH = "/lk-api/internal/api/lk/v1/orders/{orderId}/esia-token";
    private static final String LK_API_TOKEN_ORDERS_PATH = "/lk-api/internal/api/lk/v1/orders/{orderId}?token={userId}@@AL20@@1";
    private static final String LK_API_CREATE_ORDER_PATH_HIGHLOAD = "/lk-api/internal/api/lk/v1/orders/create-hp";
    private static final String LK_API_CHECK_ORDER_EXISTS = "/lk-api/internal/api/orders/v1/create-hp/{orderId}/check";
    private static final String LK_API_GET_ORDERS_BY_SERVICE_ID = "/lk-api/internal/api/lk/v1/orders/?eserviceCode={serviceId}&serviceTargetExtId={targetId}&pageIndex=1&pageSize=1000&sourceSystems=*";
    private static final String LK_API_FIND_ORDER_WITH_PAYMENT_PATH = "/lk-api/internal/api/lk/v1/orders/{orderId}/esia-token?embed=PAYMENT";
    private static final String LK_API_FIND_ORDER_LIGHT_PATH = "/lk-api/internal/api/lk/v1/drafts/light?_={preventCacheValue}&embed=LAST_INVITATION&pageIndex=1&pageSize={pageSize}&sourceSystems=*&eServiceId={serviceId}&serviceTargetId={targetId}";
    private static final String LK_API_ORDER_SET_ATTRIBUTES = "/lk-api/internal/api/lk/v1/orders/{orderId}/set/attributes";
    private static final String LK_API_CHECK_ORDER = "/api/lk/v1/orders/check/{serviceCode}/{targetCode}";
    private static final String LK_API_SAVE_VALUES = "/api/lk/v1/orders/check/add/";
    private static final String LK_API_SET_TECH_STATUS = "/lk-api/internal/api/lk/v1/orders/{orderId}/status/tech";

    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;
    private final ErrorModalDescriptorService errorModalDescriptorService;
    private final UserTokenService userTokenService;

    @Override
    public Order findLastOrder(String serviceCode, String targetCode) {
        Map<String, String> requestParametrs = Map.of(
                "serviceId", serviceCode,
                "targetId", targetCode,
                "preventCacheValue", this.generatePreventCacheValue(),
                "pageSize", "1");
        try {
            ResponseEntity<ListOrderResponse> response = restTemplate
                    .exchange(lkApiUrl + LK_API_FIND_ORDER_PATH,
                            HttpMethod.GET,
                            new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
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

    @Override
    public List<Order> findOrders(String serviceCode, String targetCode) {
        return findOrders(serviceCode, targetCode,
                new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())));
    }

    @Override
    public List<Order> findOrders(String serviceCode, String targetCode, Long userId) {
        return findOrders(serviceCode, targetCode,
                new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userTokenService.getUserToken(userId))));
    }

    @Override
    public List<Order> findOrders(String serviceCode, String targetCode, HttpEntity<ListOrderResponse> httpEntity) {
        Map<String, String> requestParametrs = Map.of(
                "serviceId", serviceCode,
                "targetId", targetCode,
                "preventCacheValue", this.generatePreventCacheValue(),
                "pageSize", "1000");
        ResponseEntity<ListOrderResponse> response = restTemplate
                .exchange(lkApiUrl + LK_API_FIND_ORDER_PATH,
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
                .exchange(lkApiUrl + LK_API_GET_ORDERS_BY_SERVICE_ID,
                        HttpMethod.GET,
                        new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
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
                .exchange(lkApiUrl + LK_API_GET_ORDERS_BY_SERVICE_ID,
                        HttpMethod.GET,
                        new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                        ListOrderResponse.class,
                        requestParams);
        if (Objects.nonNull(response.getBody()) && Objects.nonNull(response.getBody().getOrder()))
            return response.getBody().getOrder().stream()
                    .filter(order -> !ignoreOrderStatuses.contains(order.getOrderStatusId()))
                    .collect(Collectors.toList());
        return emptyList();
    }

    @Override
    public Order createOrder(PoweredOrderWithAuthDTO orderDto) {
        try {
            var resp = restTemplate.exchange(lkApiUrl + LK_API_CREATE_ORDER_PATH,
                    HttpMethod.POST,
                    new HttpEntity<>(orderDto,PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
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
        return deleteOrder(orderId,
                new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())));
    }

    @Override
    public Boolean deleteOrder(Long orderId, Long userId) {
        return deleteOrder(orderId,
                new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userTokenService.getUserToken(userId))));
    }

    @Override
    public Boolean deleteOrder(Long orderId, HttpEntity<Object> httpEntity) {
        try {
            restTemplate.exchange(lkApiUrl + LK_API_TOKEN_ORDERS_PATH,
                            HttpMethod.DELETE,
                            httpEntity,
                            Object.class,
                            Map.of(ORDER_ID_KEY, orderId, "userId", userPersonalData.getUserId()));
        } catch (ExternalServiceException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error from external service", e);
            }
            return false;
        }
        return true;
    }

    @Override
    public Order findOrderWithPayment(Long orderId) {
        try {
            ResponseEntity<Order> response = restTemplate
                    .exchange(lkApiUrl + LK_API_FIND_ORDER_WITH_PAYMENT_PATH,
                            HttpMethod.GET,
                            new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                            Order.class,
                            Map.of(ORDER_ID_KEY, orderId));
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
                    .exchange(lkApiUrl + LK_API_ORDERS_PATH,
                            HttpMethod.GET,
                            new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                            Order.class,
                            Map.of(ORDER_ID_KEY, orderId));
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
                    .exchange(lkApiUrl + LK_API_ORDERS_PATH,
                            HttpMethod.GET,
                            new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userTokenService.getUserToken(userId))),
                            Order.class,
                            Map.of(ORDER_ID_KEY, orderId));
            return response.getBody();
        } catch (ExternalServiceException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatus())) {
                throw new ErrorModalException(errorModalDescriptorService.getErrorModal(ErrorModalView.REQUESTED_ORDER_DOES_NOT_EXIST), "Запрашиваемого черновика не существует");
            }
            log.error("Error from external service", e);
            throw new FaultException("Error while receiving order by id" , e);
        }
    }

    private String generatePreventCacheValue() {
        return String.valueOf(Math.random());
    }

    @Override
    public HighLoadOrderResponseDto createHighloadOrder(HighLoadOrderRequestDto highLoadOrderPguDto) {
        try {
            var resp = restTemplate.exchange(lkApiUrl + LK_API_CREATE_ORDER_PATH_HIGHLOAD,
                    HttpMethod.POST,
                    new HttpEntity<>(highLoadOrderPguDto, PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
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

    public void checkOrderExists(Long orderId) {
        try {
            var resp = restTemplate.exchange(lkApiUrl + LK_API_CHECK_ORDER_EXISTS,
                    HttpMethod.GET,
                    new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                    String.class,
                    Map.of(ORDER_ID_KEY, orderId)
            );
            if(resp.getStatusCode() == HttpStatus.ACCEPTED){
                throw new OrderNotCreatedException("Заявление не создано");
            }
            if(resp.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR){
                log.error("Error creating order = {}", orderId);
                throw new OrderCreationException("Ошибка при создании заявления");
            }
        } catch (Exception e){
            log.error("Error while checking order status", e);
            throw new OrderNotCreatedException("Заявление не создано");
        }
    }

    @Override
    public List<OrderLight> findOrdersLight(String serviceCode, String targetCode, Long userId) {
        var httpEntity = new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userTokenService.getUserToken(userId)));
        Map<String, String> requestParametrs = Map.of(
                "serviceId", serviceCode,
                "targetId", targetCode,
                "preventCacheValue", this.generatePreventCacheValue(),
                "pageSize", "1000");
        ResponseEntity<ListOrderResponseLight> response = restTemplate
                .exchange(lkApiUrl + LK_API_FIND_ORDER_LIGHT_PATH,
                        HttpMethod.GET,
                        httpEntity,
                        ListOrderResponseLight.class,
                        requestParametrs);
        if (Objects.nonNull(response.getBody())) {
            if (Objects.nonNull(response.getBody().getOrder())) {
                return response.getBody().getOrder().size() > 0 ? response.getBody().getOrder() : emptyList();
            }
        } return emptyList();
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
                            ORDER_ID_KEY, orderId
                    )
            );
            return response.getBody();
        } catch (ExternalServiceException e) {
            log.error("Error from external service", e);
            throw new FaultException("Error while updating order attributes" + " by id" , e);
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
                        ORDER_ID_KEY, orderId
                )
        );
    }

    @Override
    public Boolean saveChoosenValuesForOrder(String serviceCode, String targetCode, Long orderId, Map<String, Object> userAnswers) {
        try {
            HashMap<String, Object> requestBody = new HashMap<>(userAnswers);
            requestBody.put(ORDER_ID_KEY, orderId);
            restTemplate.exchange(
                    lkApiUrl + LK_API_SAVE_VALUES,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                    Map.class
            );
        } catch (ExternalServiceException e) {
            throw new FaultException("Ошибка при сохранении выбранных значений для заявления." + e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Boolean hasDuplicatesForOrder(String serviceCode, String targetCode, Map<String, Object> userAnswers) {
        try {
            restTemplate.exchange(
                    lkApiUrl + LK_API_CHECK_ORDER,
                    HttpMethod.POST,
                    new HttpEntity<>(userAnswers, PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
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
}
