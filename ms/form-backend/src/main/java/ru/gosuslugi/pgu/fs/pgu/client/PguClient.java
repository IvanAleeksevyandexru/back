package ru.gosuslugi.pgu.fs.pgu.client;

import ru.gosuslugi.pgu.core.lk.model.PersonIdentifier;
import ru.gosuslugi.pgu.core.lk.model.feed.Feed;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.core.lk.model.order.dto.request.SetOrderAttributeDTO;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.dto.HighLoadOrderResponseDto;
import ru.gosuslugi.pgu.fs.pgu.dto.HighLoadOrderRequestDto;
import ru.gosuslugi.pgu.fs.pgu.dto.OrderLight;
import ru.gosuslugi.pgu.fs.pgu.dto.feed.FeedDto;
import ru.gosuslugi.pgu.fs.pgu.dto.PguServiceCodes;
import ru.gosuslugi.pgu.fs.pgu.dto.PoweredOrderWithAuthDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PguClient {
    Order findLastOrder(String serviceCode, String targetCode);

    List<Order> findOrders(String serviceCode, String targetCode);

    List<Order> findOrders(String serviceCode, String targetCode, Long userId);

    List<Order> findOrdersByStatus(String serviceCode, String targetCode, OrderStatuses orderStatus);

    List<Order> findOrdersWithoutStatuses(String serviceCode, String targetCode, List<Long> ignoreOrderStatuses);

    Order findOrderById(Long orderId);

    Order createOrder(PoweredOrderWithAuthDTO draftDto);

    Boolean deleteOrder(Long orderId);

    Boolean deleteOrder(Long orderId, Long userId);

    Boolean sendEmailInvitationToParticipant(Long orderId, PersonIdentifier participant);

    Order findOrderWithPayment(Long orderId);

    /**
     * Проверяет, что пользовательские данные уникальны по отношению к предыдущим заявлениям
     *
     * @return {@code false} - в случае, если дубликаты не обнаружены
     */
    Boolean hasDuplicatesForOrder(String serviceCode, String targetCode, Map<String, Object> userAnswers);

    /**
     * Сохраняет выбранные значения для заявления
     * @return
     */
    Boolean saveChoosenValuesForOrder(String serviceCode, String targetCode, Long orderId, Map<String, Object> userAnswers);

    /**
     * Получить по serviceId и targetId значения соответствующие значения EPGU
     * @return объект {@link PguServiceCodes} с 2 полями passport и target, которые нужны для кооректных запросов в ЛК
     */
    PguServiceCodes getPguServiceCodes(String serviceCode, String targetCode);

    void setTechStatusToOrder(Long orderId, Long status);

    /**
     * Находит запись в ленте событий пользователя по указанному типу и идентификатору
     * @param type тип записи
     * @param id идентификатор
     * @return запись, {@code null}, если запись не найдена
     */
    FeedDto findFeed(Feed.FeedType type, Long id);

    boolean checkAuthorityForService(String authorityId, String targetId);

    /**
     * Обновляет аттрибуты у ордера
     * @param setOrderAttributeDTO
     * @param orderId
     * @return
     */
    ru.gosuslugi.lk.api.order.Order setOrderAttributes(SetOrderAttributeDTO setOrderAttributeDTO, Long orderId);

    HighLoadOrderResponseDto createHighloadOrder(HighLoadOrderRequestDto highLoadOrderPguDto);

    void checkOrderExists(Long orderId);

    List<OrderLight> findOrdersLight(String serviceCode, String targetCode, Long userId);

    Order findOrderByIdAndUserId(Long orderId, Long userId);
}
