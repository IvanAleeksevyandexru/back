package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.PguTimer;

public interface TimerService {

    /**
     * Возвращает таймер по указанным параметрам
     * @param orderId идентификатор черновика
     * @param timerCode код таймера
     * @return таймер, найденный по параметрам, {@code null}, если таймер не найден
     */
    PguTimer getTimer(Long orderId, String timerCode);
}
