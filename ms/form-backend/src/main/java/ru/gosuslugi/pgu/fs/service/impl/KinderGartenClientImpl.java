package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokRequestDto;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto.BarBarBokResponseDtoBuilder;
import ru.gosuslugi.pgu.common.certificate.service.BarbarbokClient;
import ru.gosuslugi.pgu.common.certificate.service.marshaller.KinderGartenXmlMarshaller;
import ru.gosuslugi.pgu.common.kindergarten.xsd.dto.FormData;
import ru.gosuslugi.pgu.common.kindergarten.xsd.dto.GetApplicationAdmissionRequestType;
import ru.gosuslugi.pgu.common.kindergarten.xsd.dto.GetApplicationRequestType;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.kindergarten.KinderGartenStatusMessage;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.EpguRegionResponse;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenInputParams;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenOutputParams;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.RequestType;
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguOrderClientImpl;
import ru.gosuslugi.pgu.fs.service.RegionInfoService;
import ru.gosuslugi.pgu.fs.service.KinderGartenClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.error;
import static ru.gosuslugi.pgu.dto.kindergarten.KinderGartenHandlerStatus.NsiError;
import static ru.gosuslugi.pgu.dto.kindergarten.KinderGartenHandlerStatus.SmevError;

@Component
@Slf4j
@RequiredArgsConstructor
public class KinderGartenClientImpl implements KinderGartenClient {

    private final KinderGartenXmlMarshaller kinderGartenXmlMarshaller;
    private final PguOrderClientImpl orderClient;
    private final RegionInfoService regionService;
    private final BarbarbokClient barbarbokClient;

    @Override
    public KinderGartenOutputParams createXmlByInputParams(FieldComponent component, KinderGartenInputParams inputParams) {
        RequestType requestType = getRequestTypeByName(inputParams.getRequestType(), component.getId());
        String userSelectedRegion = getUserSelectedRegion(inputParams.getOrderId());// ОКАТО
        EpguRegionResponse regionResponse;
        try {
            regionResponse = regionService.getRegion(userSelectedRegion, inputParams.getPassCode(), inputParams.getTimeout());
        } catch (FormBaseWorkflowException e) {
            return new KinderGartenOutputParams(null, requestType, userSelectedRegion, null,
                    new KinderGartenStatusMessage(NsiError, ExceptionUtils.getRootCauseMessage(e)));
        }
        FormData formData = createFormData(component.getId(), requestType, inputParams.getOrderId(), regionResponse.getRoutingCode());
        String xmlRequest;
        try {
            xmlRequest = kinderGartenXmlMarshaller.marshal(formData);
        } catch (Exception e) {
            return new KinderGartenOutputParams(null, requestType, userSelectedRegion, regionResponse,
                    new KinderGartenStatusMessage(SmevError, ExceptionUtils.getRootCauseMessage(e)));
        }
        return new KinderGartenOutputParams(xmlRequest, requestType, userSelectedRegion, regionResponse, null);
    }

    @Override
    public BarBarBokResponseDtoBuilder sendMessage(String xml, String smevVersion, long orderId, int timeout) {
        Duration connectTimeout = Duration.of(timeout, ChronoUnit.MILLIS);
        BarBarBokRequestDto barBarBokRequestDto = new BarBarBokRequestDto(xml, connectTimeout.toSeconds(),
                connectTimeout.toSeconds(), connectTimeout.toSeconds(), smevVersion);
        BarBarBokResponseDtoBuilder builder = tryToSendMessage(barBarBokRequestDto, orderId, connectTimeout);
        return builder;
    }

    private BarBarBokResponseDtoBuilder tryToSendMessage(BarBarBokRequestDto barBarBokRequestDto, long orderId, Duration connectTimeout) {
        try {
            BarBarBokResponseDto responseDto = barbarbokClient.getUnsafe(barBarBokRequestDto, (int) connectTimeout.toMillis());
            return responseDto.toBuilder();
        } catch (Exception e) {
            error(log, () -> String.format("Unable to receive kindergarten info from SMEV. smevVersion=%s, orderId=%s", barBarBokRequestDto.getSmevVersion(), orderId));
            return BarBarBokResponseDto.builder()
                    .status(SmevError.name())
                    .errorMessage(ExceptionUtils.getRootCauseMessage(e));
        }
    }

    /**
     * Получение ОКАТО из ордера, найденного по orderId
     * @param orderId id id черновика-заявления
     * @return ОКАТО
     */
    private String getUserSelectedRegion(long orderId) {
        Order order = orderClient.findOrderById(orderId);
        return order.getUserSelectedRegion();
    }

    private RequestType getRequestTypeByName(String typeOfRequest, String componentId) {
        try {
            return RequestType.valueOf(typeOfRequest);
        } catch (IllegalArgumentException e) {
            throw new FormBaseException(
                    String.format("Неправильно заполнен атрибут типа запроса компонента %s, текущее значение %s", componentId, typeOfRequest), e);
        }
    }

    /**
     * Формирует объект для преобразования в xml, являющийся запросом по детским садам.
     *
     * @param componentId id компонента
     * @param requestType тип запроса (от него зависит подкорневой тег в xml)
     * @param orderId     id черновика-заявления
     * @param routingCode код берётся из рест апи информации по региону, это не ОКАТО
     * @return объект запроса
     */
    private FormData createFormData(String componentId, RequestType requestType, long orderId, String routingCode) {
        FormData formData = new FormData();
        switch (requestType) {
            case edit:
                GetApplicationRequestType applicationRequestType = new GetApplicationRequestType();
                applicationRequestType.setOrderId(orderId);
                formData.setGetApplicationRequest(applicationRequestType);
                formData.setOktmo(routingCode);
                return formData;
            case admission:
                GetApplicationAdmissionRequestType admissionRequestType = new GetApplicationAdmissionRequestType();
                admissionRequestType.setOrderId(orderId);
                formData.setGetApplicationAdmissionRequest(admissionRequestType);
                formData.setOktmo(routingCode);
                return formData;
            default:
                throw new FormBaseException(String.format("Не удалось распознать тип запроса в атрибуте typeOfRequest компонента %s", componentId));
        }
    }
}
