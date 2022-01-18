package ru.gosuslugi.pgu.fs.service.impl;

import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto.BarBarBokResponseDtoBuilder;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenInputParams;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenOutputParams;
import ru.gosuslugi.pgu.fs.service.KinderGartenClient;

public class KinderGartenClientStub implements KinderGartenClient {
    @Override
    public KinderGartenOutputParams createXmlByInputParams(FieldComponent component, KinderGartenInputParams inputParams) {
        return new KinderGartenOutputParams();
    }

    @Override
    public BarBarBokResponseDtoBuilder sendMessage(String xml, String smevVersion, long orderId, int timeout) {
        return BarBarBokResponseDto.builder();
    }
}
