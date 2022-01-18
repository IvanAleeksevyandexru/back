package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenInputParams;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenOutputParams;

/** Клиент для отправки и получения информации по дет.садам через СМЭВ-адаптер. */
public interface KinderGartenClient {

    /**
     * Вычисляет тип запроса, определяет userSelectedRegion, генерирует запрос в виде xml.
     * @param component компонент
     * @param inputParams входные параметры
     * @return выходной результат
     */
    KinderGartenOutputParams createXmlByInputParams(FieldComponent component, KinderGartenInputParams inputParams);

    /**
     * Отправляет сообщение
     * @param xml сообщение-запрос для СМЭВ
     * @param smevVersion версия СМЭВ
     * @param timeout таймаут
     * @return ответное сообщение из СМЭВ
     */
    BarBarBokResponseDto.BarBarBokResponseDtoBuilder sendMessage(String xml, String smevVersion, long orderId, int timeout);
}
