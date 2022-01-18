package ru.gosuslugi.pgu.fs.pgu.client.impl;

import java.util.HashSet;
import java.util.Set;

/**
 * Статусы для ордеров услуг
 * @see ru.gosuslugi.pgu.core.lk.model.order.Order
 * 1  Заявление зарегистрировано
 * 2  Заявление получено ведомством
 * 3  Услуга оказана
 * 4  Отказано в предоставлении услуги
 * 5  Ошибка отправки заявления в ведомство
 * 32  Подписано всеми Заявителями
 * 36  Принято в обработку
 * 70  Все заявления отменены
 * 0  Черновик заявления
 * 6  Заявление принято к рассмотрению
 * 7  Промежуточные результаты по заявлению
 * 12  Входящее Сообщение
 * 50  Запрос на получение услуги
 * 51  Запрос подтвержден
 * 52  Запрос отклонен
 * 53  Срок подтверждения истек
 * 54  Запрос не удался
 * 26  Приглашение на приём в МФЦ
 * 90  Заявление на согласование у второго заявителя
 * 9  Заявление отменяется
 * 10  Заявление отменено
 * 11  Не удалось отменить заявление
 * -1  Ошибка обработки результата
 * 8  Неизвестный статус
 * 15  Заявление требует исправления
 * 14  Ожидание дополнительной информации
 * 16  Исходящее Сообщение
 * 80  Черновик согласия
 * 31  Ожидание подписания другими Заявителями
 * 81  Согласие отправлено
 * 82  Отказ дать согласие
 * 83  Запрос согласия отменен
 * 101  Заявление зарегистрировано
 * 40  Онлайн услуга оказана
 * 41  Онлайн услуга не оказана
 * 33  Направлено в МФЦ
 * 34  Принято
 * 35  Документ готов
 * 42  Доставка отменена
 * 37  В процессе доставки
 * 38  Документ доставлен
 * 39  Документ выдан
 * 43  Доставка отменена МФЦ
 * 29  Направлено в МФЦ
 * 17  Зарегистрировано на портале
 * 104  Доверенность недействительна
 * 18  Отправка заявления невозможна
 * 84  Заявление аннулировано
 * 44  Жалоба принята к рассмотрению
 * 30  Ошибка отправки заявления в ведомство
 * 21  Заявление отправлено в ведомство
 * 22  Ошибка получения заявления ведомством
 * 24  Ошибка обработки заявления
 * 25  Приглашение на прием по заявлению
 * 88  Данные сохранены
 * 45  Назначено рассмотрение жалобы
 * 46  Жалоба удовлетворена
 * 47  Жалоба отклонена
 * 48  Автоматизированная проверка заявления не пройдена
 * 49  Невозможно определить подразделение
 * 55  Возврат денежных средств осуществлен
 * 56  Назначено рассмотрение заявления
 * 57  Заявление удовлетворено
 * 58  Заявление отклонено
 * 60  Договор ожидает подписи со стороны заявителя
 * 61  Договор дорабатывается ведомством
 * 62  Договор ожидает подписи со стороны ведомства
 */
public enum OrderStatuses {
    RESULT_CULCULATION_ERROR(-1),
    DRAFT(0L),
    EXECUTED(3L),
    DENIED(4L),
    ERROR_SEND_REQUEST(5L),
    ORDER_CANCELED(10),
    REGISTERED_ON_PORTAL(17L),
    SEND_ORDER_NOT_POSSIBLE(18L),
    ERROR_RECEIVING(22L),
    ERROR_PROCESSING(24L),
    SEND_REQUEST_ERROR(30L),
    DOCUMENT_DELIVERED(38L),
    DOCUMENT_ISSUED(39L),
    ONLINE_ORDER_EXECUTED(40L),
    ONLINE_ORDER_NOT_EXECUTED(41L),
    DELIVERY_CANCELED(42L),
    DELIVERY_CANCELED_MFC(43L),
    CLAIM_ACCEPTED(46L),
    CLAIM_FAILED(47L),
    AUTOMATED_VALIDATION_FAILED(48L),
    ERROR_DETECTED_DEPARTMENT(49L),
    REQUEST_DENIED(52L),
    REQUEST_EXPIRED(53L),
    REQUEST_FAIL(54L),
    REJECT_MONEY_COMPLETED(55L),
    ORDER_ACCEPTED(57L),
    ORDER_FAILED(58L),
    ALL_CANCELED(70L),
    ORDER_NULLIFY(84L);

    private final long statusId;

    OrderStatuses(long statusId) {
        this.statusId = statusId;
    }

    public long getStatusId() {
        return statusId;
    }

    /**
     * Возвращает сэт статусов не активных ордеров, не в работе, терминальные
     * @return сэт статусов
     */
    public static Set<Long> terminated() {
        Set<Long> statuses = new HashSet<>();

        statuses.add(RESULT_CULCULATION_ERROR.getStatusId());
        statuses.add(DRAFT.getStatusId());
        statuses.add(EXECUTED.getStatusId());
        statuses.add(DENIED.getStatusId());
        statuses.add(ERROR_SEND_REQUEST.getStatusId());
        statuses.add(ORDER_CANCELED.getStatusId());
        statuses.add(SEND_ORDER_NOT_POSSIBLE.getStatusId());
        statuses.add(ERROR_RECEIVING.getStatusId());
        statuses.add(ERROR_PROCESSING.getStatusId());
        statuses.add(SEND_REQUEST_ERROR.getStatusId());
        statuses.add(DOCUMENT_DELIVERED.getStatusId());
        statuses.add(DOCUMENT_ISSUED.getStatusId());
        statuses.add(ONLINE_ORDER_EXECUTED.getStatusId());
        statuses.add(ONLINE_ORDER_NOT_EXECUTED.getStatusId());
        statuses.add(ORDER_FAILED.getStatusId());
        statuses.add(ORDER_NULLIFY.getStatusId());
        return statuses;
    }
}