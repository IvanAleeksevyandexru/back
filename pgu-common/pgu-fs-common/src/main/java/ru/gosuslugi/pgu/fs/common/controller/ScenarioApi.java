package ru.gosuslugi.pgu.fs.common.controller;

import ru.gosuslugi.pgu.dto.DeliriumActionRequest;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.order.OrderInfoDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.order.OrderListInfoDto;

/**
 * Scenarios interaction api methods
 */
public interface ScenarioApi {

    /**
     * Method for getting first screen and  generating new orderId
     * @return scenario response
     */
    ScenarioResponse getServiceInitScreen(String serviceId, InitServiceDto initServiceDto);

    /**
     * Method for submit button handle
     */
    ScenarioResponse getNextStep(String serviceId, ScenarioRequest request);

    /**
     * Метод для сохранения промежуточного состояния заявления
     *
     * @param serviceId Идентификатр услугии
     * @param request Модель запроса сценария
     * @return Модель ответа сценария
     */
    ScenarioResponse saveCacheToDraft(String serviceId, ScenarioRequest request);

    /**
     * Method for calling delirium with action and performing next scenario step.
     */
    ScenarioResponse deliriumActionAndNextStep(String serviceId, DeliriumActionRequest request);

    /**
     * Method for return button handle
     */
    ScenarioResponse getPrevStep(String serviceId, ScenarioRequest request, Integer stepsBack);

    /**
     * Method for checking if user has already orderId and draft for a service
     * This method also check if it's an invitation scenario case
     * If orderId for user exists than check if current user is taking part as soapplicant
     * If current user's oid is found in participants than ScenarioResponse is returned with special flag (inviteCase=true)
     */
    OrderListInfoDto checkIfOrderIdExists(String serviceId, InitServiceDto initServiceDto);


}
