package ru.gosuslugi.pgu.common.certificate.service;

import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokRequestDto;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto;

/**
 * Клиент для работы со СМЭВ3 адаптером.
 *
 * @see <a href="http://kuber.gosuslugi.local:15227/barbarbok/swagger-ui.html#/internal-api-controller">
 * Rest API для запроса данных через СМЭВ 3</a>
 * @see <a href="https://confluence.egovdev.ru/pages/viewpage.action?pageId=193302173">
 * Необходимые поля для запроса в сервис барбарбок</a>
 */
public interface BarbarbokClient {
    /**
     * Отправка запроса и получение синхронного ответа
     *
     * @param dto объект с телом для запроса в барбарбок
     * @return ответное сообщение от барбарбок
     */
    BarBarBokResponseDto get(BarBarBokRequestDto dto);

    /**
     * Отправка запроса и получение синхронного ответа
     *
     * @param dto объект с телом для запроса в барбарбок
     * @param timeout ограничение времени для RestTemplate
     * @return ответное сообщение от барбарбок
     */
    BarBarBokResponseDto get(BarBarBokRequestDto dto, int timeout);

    /**
     * Отправка запроса и получение синхронного ответа с пробросом наружу unchecked исключений
     *
     * @param dto объект с телом для запроса в барбарбок
     * @param timeout ограничение времени для RestTemplate
     * @return ответное сообщение от барбарбок
     */
    BarBarBokResponseDto getUnsafe(BarBarBokRequestDto dto, int timeout);
}
