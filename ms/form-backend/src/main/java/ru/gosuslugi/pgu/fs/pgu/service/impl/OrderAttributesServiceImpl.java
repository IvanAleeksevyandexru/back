package ru.gosuslugi.pgu.fs.pgu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import ru.gosuslugi.lk.api.order.Order;
import ru.gosuslugi.pgu.core.lk.model.order.OrderAttribute;
import ru.gosuslugi.pgu.core.lk.model.order.dto.request.SetOrderAttributeDTO;
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguOrderClientImpl;
import ru.gosuslugi.pgu.fs.pgu.service.OrderAttributesService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAttributesServiceImpl implements OrderAttributesService {

    private final PguOrderClientImpl orderClient;

    @Override
    @CacheEvict(key="#orderId", cacheResolver = "requestCacheResolver", cacheNames = "findOrderById")
    public Order updateOrderStatus(Long orderStatus, Long orderId) {
        final Map<OrderAttribute, Object> attributes = new HashMap<>();
        attributes.put(OrderAttribute.ORG_STATUS_ID, orderStatus);
        final SetOrderAttributeDTO setOrderAttributeDTO = new SetOrderAttributeDTO();
        setOrderAttributeDTO.setOrderAttributeMap(attributes);
        return orderClient.setOrderAttributes(setOrderAttributeDTO, orderId);
    }

    @Override
    @CacheEvict(key="#orderId", cacheResolver = "requestCacheResolver", cacheNames = "findOrderById")
    public Order updateTechOrderStatus(Long orderStatus, Long orderId) {
        final Map<OrderAttribute, Object> attributes = new HashMap<>();
        attributes.put(OrderAttribute.TECH_STATUS_ID, orderStatus);
        final SetOrderAttributeDTO setOrderAttributeDTO = new SetOrderAttributeDTO();
        setOrderAttributeDTO.setOrderAttributeMap(attributes);
        return orderClient.setOrderAttributes(setOrderAttributeDTO, orderId);
    }

    @Override
    public Order updateTechOrderStatusNoCache(Long orderStatus, Long orderId) {
        final Map<OrderAttribute, Object> attributes = new HashMap<>();
        attributes.put(OrderAttribute.TECH_STATUS_ID, orderStatus);
        final SetOrderAttributeDTO setOrderAttributeDTO = new SetOrderAttributeDTO();
        setOrderAttributeDTO.setOrderAttributeMap(attributes);
        return orderClient.setOrderAttributes(setOrderAttributeDTO, orderId);
    }

}
