package ru.gosuslugi.pgu.fs.pgu.client;

import org.springframework.http.HttpEntity;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.core.lk.model.order.dto.request.SetOrderAttributeDTO;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.dto.HighLoadOrderRequestDto;
import ru.gosuslugi.pgu.fs.pgu.dto.HighLoadOrderResponseDto;
import ru.gosuslugi.pgu.fs.pgu.dto.ListOrderResponse;
import ru.gosuslugi.pgu.fs.pgu.dto.OrderLight;
import ru.gosuslugi.pgu.fs.pgu.dto.PoweredOrderWithAuthDTO;

import java.util.List;
import java.util.Map;

public interface PguOrderClient {

    Order findLastOrder(String serviceCode, String targetCode);
    List<Order> findOrders(String serviceCode, String targetCode);
    List<Order> findOrders(String serviceCode, String targetCode, Long userId);
    List<Order> findOrders(String serviceCode, String targetCode, HttpEntity<ListOrderResponse> httpEntity);
    List<Order> findOrdersByStatus(String serviceCode, String targetCode, OrderStatuses orderStatus);
    List<Order> findOrdersWithoutStatuses(String serviceCode, String targetCode, List<Long> ignoreOrderStatuses);
    Order createOrder(PoweredOrderWithAuthDTO orderDto);
    Boolean deleteOrder(Long orderId);
    Boolean deleteOrder(Long orderId, Long userId);
    Boolean deleteOrder(Long orderId, HttpEntity<Object> httpEntity);
    Order findOrderWithPayment(Long orderId);

    /**
     * Проверяет, что пользовательские данные уникальны по отношению к предыдущим заявлениям
     *
     * @return {@code false} - в случае, если дубликаты не обнаружены
     */
    Order findOrderById(Long orderId);
    Order findOrderByIdAndUserId(Long orderId, Long userId);
    HighLoadOrderResponseDto createHighloadOrder(HighLoadOrderRequestDto highLoadOrderPguDto);
    List<OrderLight> findOrdersLight(String serviceCode, String targetCode, Long userId);
    Boolean hasDuplicatesForOrder(String serviceCode, String targetCode, Map<String, Object> userAnswers);

    /**
     * Сохраняет выбранные значения для заявления
     * @return
     */
    Boolean saveChoosenValuesForOrder(String serviceCode, String targetCode, Long orderId, Map<String, Object> userAnswers);
    void setTechStatusToOrder(Long orderId, Long status);

    /**
     * Обновляет аттрибуты у ордера
     * @param setOrderAttributeDTO
     * @param orderId
     * @return
     */
    ru.gosuslugi.lk.api.order.Order setOrderAttributes(SetOrderAttributeDTO setOrderAttributeDTO, Long orderId);

}
