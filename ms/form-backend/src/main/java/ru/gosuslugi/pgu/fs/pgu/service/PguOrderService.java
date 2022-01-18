package ru.gosuslugi.pgu.fs.pgu.service;

import ru.gosuslugi.pgu.core.lk.model.PersonIdentifier;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.dto.descriptor.HighloadParameters;
import ru.gosuslugi.pgu.dto.descriptor.types.OrderType;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.dto.OrderLight;
import ru.gosuslugi.pgu.fs.pgu.dto.PguServiceCodes;

import java.util.List;
import java.util.Map;

public interface PguOrderService {

    /**
     * Method for getting orderIds for current service in draft state
     * @param serviceCode - FRGU service id (passed from form)
     * @param targetCode - parameter that  can be changed by some department (passed from form)
     * @return list of orders
     */
    List<Order> findDrafts(String serviceCode, String targetCode);

    /**
     * Найти все ордера услуг пользователя
     * @param serviceCode код услуги
     * @param targetCode код цели услуги
     * @return лист ордеров на слуги
     */
    List<Order> findOrders(String serviceCode, String targetCode);

    /**
     * Найти все ордера услуг пользователя
     * @param serviceCode код услуги
     * @param targetCode код цели услуги
     * @param userId id пользователя
     * @return лист ордеров на слуги
     */
    List<Order> findDrafts(String serviceCode, String targetCode, Long userId);

    /**
     * Найти все ордера услуг пользователя
     * @param serviceCode код услуги
     * @param targetCode код цели услуги
     * @param userId id пользователя
     * @return лист ордеров с минимальным набором данных по услуге
     */
    List<OrderLight> findDraftsLight(String serviceCode, String targetCode, Long userId);


    /**
     * Найти все ордера пользователя по услуге
     * @param serviceCode код услуги
     * @param targetCode код цели услуги
     * @param orderStatus статус ордера
     * @return лист ордеров на слуги
     */
    List<Order> findOrdersByStatus(String serviceCode, String targetCode, OrderStatuses orderStatus);

    /**
     * Найти все ордера пользователя по услуге
     * @param serviceCode код услуги
     * @param targetCode код цели услуги
     * @param ignoreOrderStatuses статусы ордеров для игнорирования
     * @return лист ордеров на слуги
     */
    List<Order> findOrdersWithoutStatuses(String serviceCode, String targetCode, List<Long> ignoreOrderStatuses);

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
     * @return PguServiceCodes - 2 поля passport и target, которые нужны для кооректных запросов в ЛК
     */
    PguServiceCodes getPguServiceCodes(String serviceCode, String targetCode);

    /**
     * Возвращет состояние заявок
     * @param serviceCode код услуги
     * @param targetCode код //TODO чего??? как он правильно назвается???
     * @return true если нет заявок, которые находятся в процессе выполнения
     */
    boolean allTerminated(String serviceCode, String targetCode);

    /**
     * Создает новое заявление указанного типа
     * @param serviceCode - идентификатор услуги
     * @param targetCode - идентификатор цели услуги
     * @param orderType - тип заявления
     * @return идентификатор заявления
     */
    Long createOrderId(String serviceCode, String targetCode, OrderType orderType, HighloadParameters highloadParameters);

    /**
     * Method for getting orderId that was not finished for a service
     * @param serviceCode
     * @param targetCode
     * @return the very first not completed orderId
     */
    Order findLastOrder(String serviceCode, String targetCode);

    Boolean sendEmailInvitationToParticipant(Long orderId, PersonIdentifier participant);

    Boolean deleteOrderById(Long id);

    Boolean deleteOrderByIdAndUserId(Long orderId, Long userId);

    Order findOrderByIdAndUserId(Long orderId, Long userId);

    /**
     * Method for getting order with filled information about payments
     * @param orderId - orederId to search
     * @return Order with specified id and payments info
     */
    Order getOrderWithPaymentInfo(Long orderId);

    Order findOrderByIdCached(Long orderId);

    void setTechStatusToOrder(Long orderId, Long status);

    void checkOrderExists(Long orderId);
}
