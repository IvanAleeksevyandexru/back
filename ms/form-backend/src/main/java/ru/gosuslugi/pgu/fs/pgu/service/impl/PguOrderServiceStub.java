package ru.gosuslugi.pgu.fs.pgu.service.impl;

import lombok.extern.slf4j.Slf4j;
import ru.gosuslugi.pgu.core.lk.model.PersonIdentifier;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.dto.descriptor.HighloadParameters;
import ru.gosuslugi.pgu.dto.descriptor.types.OrderType;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.dto.OrderLight;
import ru.gosuslugi.pgu.fs.pgu.dto.PguServiceCodes;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Slf4j
public class PguOrderServiceStub implements PguOrderService {
    public static Long EXISTING_ORDER_ID = 0L;

    @Override
    public List<Order> findDrafts(String serviceCode, String targetCode) {
        return emptyList();
    }

    @Override
    public List<Order> findOrders(String serviceCode, String targetCode) {
        return emptyList();
    }

    @Override
    public List<Order> findDrafts(String serviceCode, String targetCode, Long userId) {
        return findOrders(serviceCode, targetCode);
    }

    @Override
    public Order findOrderByIdAndUserId(Long orderId, Long userId) {
        return findOrderByIdCached(orderId);
    }

    @Override
    public List<OrderLight> findDraftsLight(String serviceCode, String targetCode, Long userId) {
        return emptyList();
    }

    @Override
    public List<Order> findOrdersByStatus(String serviceCode, String targetCode, OrderStatuses orderStatus) {
        return emptyList();
    }

    @Override
    public List<Order> findOrdersWithoutStatuses(String serviceCode, String targetCode, List<Long> ignoreOrderStatuses) {
        return emptyList();
    }

    @Override
    public Boolean hasDuplicatesForOrder(String serviceCode, String targetCode, Map<String, Object> userAnswers) {
        return false;
    }

    @Override
    public Boolean saveChoosenValuesForOrder(String serviceCode, String targetCode, Long orderId, Map<String, Object> userAnswers) {
        return true;
    }

    @Override
    public PguServiceCodes getPguServiceCodes(String serviceCode, String targetCode) {
        PguServiceCodes pguServiceCodes = new PguServiceCodes();
        pguServiceCodes.setPassport(serviceCode);
        pguServiceCodes.setTarget(targetCode);
        return pguServiceCodes;
    }

    @Override
    public boolean allTerminated(String serviceCode, String targetCode) {
        return true;
    }

    /**
     * Returns last order for user
     * @param serviceCode - FRGU service id (passed from form)
     * @param targetCode - parameter that  can be changed by some department (passed from form)
     * @return {@code null} if order id was not set and Order if it was set
     * @see ru.gosuslugi.pgu.fs.controller.TestDemoController#updateOrderId(String)
     */
    @Override
    public Order findLastOrder(String serviceCode, String targetCode) {
        Order order = null;
        if(EXISTING_ORDER_ID > 0L ) {
            order = new Order();
            order.setId(EXISTING_ORDER_ID);
        }
        if(log.isInfoEnabled()) log.info("last order = {}", order);
        return order;
    }

    @Override
    public Boolean sendEmailInvitationToParticipant(Long orderId, PersonIdentifier participant) {
        if(log.isInfoEnabled()) log.info("email for orderId = {} was sent to {}", orderId, participant);
        return true;
    }

    @Override
    public Boolean deleteOrderById(Long id) {
        if(log.isInfoEnabled()) log.info("order with id = {} was deleted", id);
        return true;
    }

    @Override
    public Boolean deleteOrderByIdAndUserId(Long id, Long userId) {
        return deleteOrderById(id);
    }

    @Override
    public Order getOrderWithPaymentInfo(Long orderId) {
        return findLastOrder(null, null);
    }

    @Override
    public Order findOrderByIdCached(Long orderId) {
        return findLastOrder(null, null);
    }

    @Override
    public void setTechStatusToOrder(Long orderId, Long status) {

    }

    @Override
    public Long createOrderId(String serviceCode, String targetCode, OrderType orderType, HighloadParameters highloadParameters) {
        long start = 1000L;
        long end = 1000000L;
        long generated = start + (long) (Math.random() * (end - start));
        if(log.isInfoEnabled()) log.info("new order id = {}", generated);
        PguOrderServiceStub.EXISTING_ORDER_ID = generated;
        return generated;
    }

    @Override
    public void checkOrderExists(Long orderId) {

    }
}
