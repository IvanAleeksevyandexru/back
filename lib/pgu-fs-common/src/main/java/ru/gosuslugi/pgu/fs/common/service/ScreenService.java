package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;

public interface ScreenService {


    /**
     * Method for getting first screen of a service
     * If orderId is not null then draft should be loaded from Casandra
     * in order to continue filling application
     * @param serviceId         Идентификатор услуги
     * @param initServiceDto    DTO
     * @return Сценарий
     */
    ScenarioResponse getInitScreen(String serviceId, InitServiceDto initServiceDto);

    /**
     * Method for getting next screen according to scenario
     * @param request - receives ServiceRequest dto to get user application fields in order to determine next step according to scenarios
     *                then updated display section, and move currentValue into applicantAnswers
     * @return Сценарий
     */
    ScenarioResponse getNextScreen(ScenarioRequest request, String serviceId);

    ScenarioResponse skipStep(ScenarioRequest request,String serviceId);

    /**
     * Method for getting previous screen based on currents scenario attribute
     * @param request - receives ServiceRequest dto to get user application fields in order to determine next step according to scenarios
     *                then updated display section, and move currentValue into applicantAnswers
     * @return Сценарий
     */
    ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId);

    /**
     * Method for getting previous screen based on currents scenario attribute
     * @param request       receives ServiceRequest dto to get user application fields in order to determine next step according to scenarios
     *                      then updated display section, and move currentValue into applicantAnswers
     * @param serviceId     serviceId
     * @param stepsBack     number of steps back
     * @return              scenario response
     */
    ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId, Integer stepsBack);

    void saveCacheToDraft(String serviceId, ScenarioRequest request);
}
