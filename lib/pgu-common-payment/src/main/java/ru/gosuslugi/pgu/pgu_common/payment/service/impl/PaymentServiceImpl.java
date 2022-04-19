package ru.gosuslugi.pgu.pgu_common.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentInfo;
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentStatusInfo;
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentStatusInfoWrapper;
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentsInfo;
import ru.gosuslugi.pgu.pgu_common.payment.service.PaymentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String LIST_PAYMENT_INFO_V1_URL = "api/lk/v1/orders/listpaymentsinfo";
    private static final String LIST_PAYMENT_INFO_V3_URL = "api/lk/v3/orders/listpaymentsinfo";
    private static final String PAYMENT_STATUS_URL = "api/lk/v1/paygate/uin/status/{paycode}?orderId={orderId}";

    private final RestTemplate restTemplate;

    @Value("${pgu.payment-url}")
    private String pguUrl;

    @Value("${pgu.payment-ipsh-url}")
    private String pguIpshUrl;

    @Value("${mock.billing.enabled}")
    private Boolean mockEnabled;

    @Value("${mock.billing.url:#{null}}")
    private String mockUrl;

    @Override
    public List<PaymentInfo> getUnusedPaymentsV1(Long orderId, String orgCode, String token, String serviceId, String passportTC, String registrationType, String grzYes) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> paramMapOptions = new HashMap<>();
        paramMapOptions.put("passportTC", passportTC);
        paramMapOptions.put("registrationType", registrationType);
        paramMapOptions.put("grzYes", grzYes);
        paramMap.put("orderId", orderId);
        paramMap.put("orgCode", orgCode);
        paramMap.put("serviceCodes", List.of(serviceId));
        paramMap.put("options", paramMapOptions);
        return getPaymentInfos(token, paramMap, LIST_PAYMENT_INFO_V1_URL);
    }

    @Override
    public List<PaymentInfo> getUnusedPaymentsV3(Long orderId, String orgCode, String token, String serviceId, String applicantType, Long amount) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        map.put("orgCode", orgCode);
        map.put("auditory", applicantType);
        map.put("serviceCodes", List.of(serviceId));
        map.put("totalCoast", amount);
        return getPaymentInfos(token, map, LIST_PAYMENT_INFO_V3_URL);
    }

    private List<PaymentInfo> getPaymentInfos(String token, Map<String, Object> paramMap, String listPaymentInfoUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "acc_t=" + token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paramMap, headers);

        String url = String.format("%s%s", mockEnabled ? mockUrl : pguUrl, listPaymentInfoUrl);
        try {
            List<PaymentInfo> result = new ArrayList<>();
            ResponseEntity<PaymentsInfo> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
            if (nonNull(response.getBody()) && nonNull(response.getBody().getPayment())) {
                result.addAll(response.getBody().getPayment());
            }
            return result;
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    @Override
    public PaymentStatusInfo getPaymentStatus(Long orderId, String token, String payCode, String billId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "acc_t=" + token);

        Map<String, Object> queryParams = Map.of(
                "orderId", orderId,
                "paycode", payCode
        );
        String url = pguUrl + PAYMENT_STATUS_URL;
        try {
            ResponseEntity<PaymentStatusInfoWrapper> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {
                    },
                    queryParams
            );

            if (nonNull(response.getBody()) && nonNull(response.getBody().getPaymentStatus())) {
                return response.getBody().getPaymentStatus()
                        .stream()
                        .findFirst().orElse(new PaymentStatusInfo().withPaid(false));
            }
            if (StringUtils.hasText(billId)) {
                //FIXME Удалить. после правки аналитики по сервису: api/lk/v1/paygate/uin/status/
                return getReCheckedPaymentStatus(orderId, token, billId);
            }
            return new PaymentStatusInfo().withPaid(false);
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    //FIXME Удалить. после правки аналитики по сервису: api/lk/v1/paygate/uin/status/
    /**
     * Повторная проверка на наличие платежа через "api/pay/v1/bills"
     * если сервис api/lk/v1/paygate/uin/status/ ничего не нашел
     * Описание сервиса:
     *      https://confluence.egovdev.ru/pages/viewpage.action?pageId=173608023
     *      Получение статуса платежа
     */
    private PaymentStatusInfo getReCheckedPaymentStatus(Long orderId, String token, String billId) {
        String reCheckStatusUrl = "api/pay/v1/bills";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "acc_t=" + token);
        Map<String, Object> map = new HashMap<>();
        String reCheckUrl = String.format("%s%s", pguIpshUrl, reCheckStatusUrl);
        UriComponents reCheckUri = UriComponentsBuilder.fromUriString(reCheckUrl)
                .queryParam("billIds", billId)
                .queryParam("ci","false")
                .build(false);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        try {
            ResponseEntity<Map<String, Object>> reCheckedResponse = restTemplate.exchange(reCheckUri.toString(), HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
            });

            if (isNull(reCheckedResponse.getBody()) || isNull(reCheckedResponse.getBody().get("response"))) {
                return new PaymentStatusInfo().withPaid(false);
            }
            Object responseSection = reCheckedResponse.getBody().get("response");
            if (!(responseSection instanceof Map)) {
                return new PaymentStatusInfo().withPaid(false);
            }
            Map<String, Object> responseMap = (Map<String, Object>) responseSection;
            if (isNull(responseMap.get("paiedBillIds"))) {
                return new PaymentStatusInfo().withPaid(false);
            }
            List<Objects> paiedBillIds = (List) responseMap.get("paiedBillIds");
            if (!paiedBillIds.isEmpty() && nonNull(paiedBillIds.get(0))) {
                return new PaymentStatusInfo().withPaid(true);
            }
            return new PaymentStatusInfo().withPaid(false);
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }
}
