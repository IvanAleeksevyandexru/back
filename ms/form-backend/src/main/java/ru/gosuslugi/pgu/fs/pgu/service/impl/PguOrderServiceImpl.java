package ru.gosuslugi.pgu.fs.pgu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import ru.atc.carcass.security.rest.model.orgs.OrgType;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.PersonIdentifier;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.core.lk.model.order.OrderStatus;
import ru.gosuslugi.pgu.dto.descriptor.HighloadParameters;
import ru.gosuslugi.pgu.dto.descriptor.types.OrderType;
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguOrderClientImpl;
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguUtilsClientImpl;
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguEmailClientImpl;
import ru.gosuslugi.pgu.fs.pgu.dto.HighLoadOrderRequestDto;
import ru.gosuslugi.pgu.fs.pgu.dto.OrderLight;
import ru.gosuslugi.pgu.fs.pgu.dto.PguServiceCodes;
import ru.gosuslugi.pgu.fs.pgu.dto.PoweredOrderWithAuthDTO;
import ru.gosuslugi.pgu.fs.pgu.mapper.HighLoadOrderPguMapper;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.EmpowermentService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.*;

@Slf4j
@RequiredArgsConstructor
public class PguOrderServiceImpl implements PguOrderService {

    private final UserPersonalData userPersonalData;
    private final UserOrgData userOrgData;
    private final PguUtilsClientImpl orderUtilsClient;
    private final UserCookiesService userCookiesService;
    private final EmpowermentService empowermentService;
    private final HighLoadOrderPguMapper highLoadOrderPguMapper;
    private final PguEmailClientImpl sendEmailInvitationClient;
    private final PguOrderClientImpl orderClient;

    @Override
    public Long createOrderId(String serviceCode, String targetCode, OrderType orderType, HighloadParameters highloadParameters) {
        if(Objects.nonNull(highloadParameters) && highloadParameters.isEnabled()){
            var dto = highLoadOrderPguMapper.toDto(highloadParameters);
            dto.setUserSelectedRegion(userCookiesService.getUserSelectedRegion());
            dto.setEServiceCode(serviceCode);
            dto.setUserId(String.valueOf(userPersonalData.getUserId()));
            return this.createHighLoadOrder(dto);
        }
        info(log, () -> String.format("Creating new %s order for service %s:%s", orderType, serviceCode, targetCode));
        PoweredOrderWithAuthDTO draftDto = createDraftDto(serviceCode, targetCode, orderType);
        Order order = orderClient.createOrder(draftDto);
        return order.getId();
    }

    private Long createHighLoadOrder(HighLoadOrderRequestDto highLoadOrderPguDto){
        info(log, () -> String.format("Creating new %s highload order for service %s:%s",
                highLoadOrderPguDto.getOrderType(),
                highLoadOrderPguDto.getEServiceCode(),
                highLoadOrderPguDto.getServiceTargetCode())
        );

        return orderClient.createHighloadOrder(highLoadOrderPguDto).getId();
    }

    @Override
    public Order findLastOrder(String serviceCode, String targetCode) {
        info(log, () -> String.format("Checking if orderId exists for user with oid %s service %s:%s", userPersonalData.getUserId(), serviceCode, targetCode));
        return orderClient.findLastOrder(serviceCode, targetCode);
    }

    @Override
    public List<Order> findDrafts(String serviceCode, String targetCode) {
        info(log, () -> String.format("Checking if orderId exists for user with oid %s service %s:%s", userPersonalData.getUserId(), serviceCode, targetCode));

        return orderClient.findOrders(serviceCode, targetCode).stream()
                .filter(o -> o.getOrderStatusId() == OrderStatuses.DRAFT.getStatusId())
                .collect(toList());
    }

    @Override
    public List<Order> findDrafts(String serviceCode, String targetCode, Long userId) {
        info(log, () -> String.format("Checking if orderId exists for user with oid %s service %s:%s", userId, serviceCode, targetCode));

        return orderClient.findOrders(serviceCode, targetCode, userId).stream()
                .filter(o -> o.getOrderStatusId() == OrderStatuses.DRAFT.getStatusId())
                .collect(toList());
    }

    @Override
    public List<OrderLight> findDraftsLight(String serviceCode, String targetCode, Long userId) {
        info(log, () -> String.format("Checking if orderId exists for user with oid %s service %s:%s", userId, serviceCode, targetCode));

        return orderClient.findOrdersLight(serviceCode, targetCode, userId).stream()
                .filter(o -> o.getStatusId() == OrderStatuses.DRAFT.getStatusId())
                .collect(toList());
    }

    /**
     * Найти все ордера услуг пользователя
     * @param serviceCode код услуги
     * @param targetCode код //TODO чего??? как он правильно назвается???
     * @return лист ордеров на слуги
     */
    @Override
    public List<Order> findOrders(String serviceCode, String targetCode) {
        return orderClient.findOrders(serviceCode, targetCode);
    }

    @Override
    public List<Order> findOrdersByStatus(String serviceCode, String targetCode, OrderStatuses orderStatus) {
        return orderClient.findOrdersByStatus(serviceCode, targetCode, orderStatus);
    }

    @Override
    public List<Order> findOrdersWithoutStatuses(String serviceCode, String targetCode, List<Long> ignoreOrderStatuses) {
        return orderClient.findOrdersWithoutStatuses(serviceCode, targetCode, ignoreOrderStatuses);
    }

    @Override
    public Boolean hasDuplicatesForOrder(String serviceCode, String targetCode, Map<String, Object> userAnswers) {
        return orderClient.hasDuplicatesForOrder(serviceCode, targetCode, userAnswers);
    }

    @Override
    public Boolean saveChoosenValuesForOrder(String serviceCode, String targetCode, Long orderId, Map<String, Object> userAnswers) {
        return orderClient.saveChoosenValuesForOrder(serviceCode, targetCode, orderId, userAnswers);
    }

    @Override
    public PguServiceCodes getPguServiceCodes(String serviceCode, String targetCode) {
        return orderUtilsClient.getPguServiceCodes(serviceCode, targetCode);
    }

    @Override
    public boolean allTerminated(String serviceCode, String targetCode) {
        return findOrders(serviceCode, targetCode)
                .stream()
                .map(Order::getOrderStatusId)
                .distinct()
                .allMatch(o -> OrderStatuses.terminated().contains(o));
    }

    @Override
    public Boolean sendEmailInvitationToParticipant(Long orderId, PersonIdentifier participant) {
        return sendEmailInvitationClient.sendEmailInvitationToParticipant(orderId,participant);
    }

    @Override
    public Boolean deleteOrderById(Long orderId) {
        return orderClient.deleteOrder(orderId);
    }

    @Override
    public Boolean deleteOrderByIdAndUserId(Long orderId, Long userId) {
        return orderClient.deleteOrder(orderId, userId);
    }

    @Override
    public Order findOrderByIdAndUserId(Long orderId, Long userId) {
        return orderClient.findOrderByIdAndUserId(orderId, userId);
    }

    @Override
    public Order getOrderWithPaymentInfo(Long orderId) {
        info(log, () -> String.format("Getting order with payment information for id %s", orderId));
        return orderClient.findOrderWithPayment(orderId);
    }

    @Override
    @Cacheable(cacheResolver = "requestCacheResolver", cacheNames = "findOrderById")
    public Order findOrderByIdCached(Long orderId) {
        info(log, () -> String.format("Getting order by id %s", orderId));
        return orderClient.findOrderById(orderId);
    }

    @Override
    public void setTechStatusToOrder(Long orderId, Long status) {
        info(log, () -> String.format("Setting tech status %s to order %s", status, orderId));
        orderClient.setTechStatusToOrder(orderId, status);
    }

    private PoweredOrderWithAuthDTO createDraftDto(String serviceCode, String targetCode, OrderType orderType) {
        PoweredOrderWithAuthDTO orderDto = new PoweredOrderWithAuthDTO();
        orderDto.setUserId(userPersonalData.getUserId());
        if (Objects.nonNull(userOrgData.getOrg())) {
            orderDto.setOrgId(userPersonalData.getOrgId());
            // Тип юр. лица: B - BUSINESS, L - LEGAL и AGENCY
            OrgType orgType = userOrgData.getOrg().getType();
            if (orgType == OrgType.AGENCY) {
                orgType = OrgType.LEGAL;
            }
            orderDto.setOrgType(orgType.toString().substring(0, 1));
            if (Objects.nonNull(userOrgData.getOrgRole())
                    && !Boolean.parseBoolean(userOrgData.getOrgRole().getChief())
            ) {
                String orgPowers = String.join(",", empowermentService.getUserEmpowerments());
                if (orgPowers.length() > 0) {
                    orderDto.setOrgPowers(orgPowers);
                }
            }
        }
        orderDto.seteServiceCode(serviceCode);
        orderDto.setServiceTargetCode(targetCode);
        orderDto.setAssuranceLevel("AL20");
        orderDto.setLocation("00");
        orderDto.setOrderType(ofNullable(orderType).orElse(OrderType.ORDER).toString());
        orderDto.setSourceSystem("EPGU2020");
        orderDto.setOrderStatusId(OrderStatus.STATUS_DRAFT);
        orderDto.setUserSelectedRegion(userCookiesService.getUserSelectedRegion());
        return orderDto;
    }

    @Override
    public void checkOrderExists(Long orderId) {
        orderClient.checkOrderExists(orderId);
    }
}
