package unit.ru.gosuslugi.pgu.fs.helper.impl

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.userdata.CarInfoComponent
import ru.gosuslugi.pgu.fs.component.userdata.model.CarInfoComponentDto
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.*
import ru.gosuslugi.pgu.pgu_common.gibdd.service.GibddDataService
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class CarInfoComponentSpec extends Specification {

    CarInfoComponent component
    GibddDataService gibddDataServiceMock

    FieldComponent fieldComponent = new FieldComponent(id: 'carInfo', attrs: [tx: 'tx'], arguments: [vin: '100'])
    ComponentResponse<CarInfoComponentDto> result

    def setup() {
        UserPersonalData userPersonalData = Mock(UserPersonalData)
        userPersonalData.getPerson() >> new Person()

        gibddDataServiceMock = Mock(GibddDataService)

        component = new CarInfoComponent(gibddDataServiceMock, userPersonalData)
        component.requestTimeout = 5
    }

    def 'Can get initial value'() {
        when:
        gibddDataServiceMock.getAsyncVehicleInfo(_ as VehicleInfoRequest) >> simpleGetAsyncVehicleInfoResponse()
        gibddDataServiceMock.getAsyncFederalNotaryInfo(_ as FederalNotaryRequest) >> simpleGetAsyncFederalNotaryInfoResponse()
        result = component.getInitialValue(fieldComponent, simpleScenarioDto())

        then:
        result.get().vehicleInfo.status == ''
        result.get().notaryInfo.isPledged
        result.get().vehicleServiceCallResult == ExternalServiceCallResult.SUCCESS
        result.get().notaryServiceCallResult == ExternalServiceCallResult.SUCCESS
    }

    def 'Can get initial value if vehicle not found'() {
        when:
        gibddDataServiceMock.getAsyncVehicleInfo(_ as VehicleInfoRequest) >> simpleGetAsyncVehicleInfoResponse(null, ExternalServiceCallResult.NOT_FOUND_ERROR)
        gibddDataServiceMock.getAsyncFederalNotaryInfo(_ as FederalNotaryRequest) >> simpleGetAsyncFederalNotaryInfoResponse()
        result = component.getInitialValue(fieldComponent, simpleScenarioDto())

        then:
        result.get().vehicleInfo == null
        result.get().vehicleServiceCallResult == ExternalServiceCallResult.NOT_FOUND_ERROR
        result.get().notaryServiceCallResult == ExternalServiceCallResult.SUCCESS
    }

    def 'Can get initial value if vehicle service return error'() {
        when:
        gibddDataServiceMock.getAsyncVehicleInfo(_ as VehicleInfoRequest) >> simpleGetAsyncVehicleInfoResponse(null, ExternalServiceCallResult.EXTERNAL_SERVER_ERROR)
        gibddDataServiceMock.getAsyncFederalNotaryInfo(_ as FederalNotaryRequest) >> simpleGetAsyncFederalNotaryInfoResponse()
        result = component.getInitialValue(fieldComponent, simpleScenarioDto())

        then:
        result.get().vehicleInfo == null
        result.get().vehicleServiceCallResult == ExternalServiceCallResult.EXTERNAL_SERVER_ERROR
        result.get().notaryServiceCallResult == ExternalServiceCallResult.SUCCESS
    }

    def 'Can get initial value if federal notary service return error'() {
        when:
        gibddDataServiceMock.getAsyncVehicleInfo(_ as VehicleInfoRequest) >> simpleGetAsyncVehicleInfoResponse()
        gibddDataServiceMock.getAsyncFederalNotaryInfo(_ as FederalNotaryRequest) >> simpleGetAsyncFederalNotaryInfoResponse(null, ExternalServiceCallResult.EXTERNAL_SERVER_ERROR)
        result = component.getInitialValue(fieldComponent, simpleScenarioDto())

        then:
        result.get().notaryInfo == null
        result.get().vehicleServiceCallResult == ExternalServiceCallResult.SUCCESS
        result.get().notaryServiceCallResult == ExternalServiceCallResult.EXTERNAL_SERVER_ERROR
    }

    def 'Can get initial value if vehicle and notary services return error'() {
        when:
        gibddDataServiceMock.getAsyncVehicleInfo(_ as VehicleInfoRequest) >> simpleGetAsyncVehicleInfoResponse(null, ExternalServiceCallResult.EXTERNAL_SERVER_ERROR)
        gibddDataServiceMock.getAsyncFederalNotaryInfo(_ as FederalNotaryRequest) >> simpleGetAsyncFederalNotaryInfoResponse(null, ExternalServiceCallResult.EXTERNAL_SERVER_ERROR)
        result = component.getInitialValue(fieldComponent, simpleScenarioDto())

        then:
        result.get().vehicleInfo == null
        result.get().notaryInfo == null
        result.get().vehicleServiceCallResult == ExternalServiceCallResult.EXTERNAL_SERVER_ERROR
        result.get().notaryServiceCallResult == ExternalServiceCallResult.EXTERNAL_SERVER_ERROR
    }

    def 'Can validate submit value'() {
        given:
        Map<String, String> incorrectAnswers = [:]
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry('key', '0')
        ScenarioDto scenarioDto = new ScenarioDto(orderId: 1L, display: new DisplayRequest(components: [new FieldComponent(id: entry.key, arguments: [vin: '100'])]))

        when:
        gibddDataServiceMock.getAsyncVehicleInfo(_ as VehicleInfoRequest) >> simpleGetAsyncVehicleInfoResponse()
        gibddDataServiceMock.getAsyncFederalNotaryInfo(_ as FederalNotaryRequest) >> simpleGetAsyncFederalNotaryInfoResponse()
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.size() == 1
        incorrectAnswers.get(entry.getKey()) == 'повторный запрос данных'
        scenarioDto.display.components[0].value == simpleComponentValue()

        when:
        incorrectAnswers = [:]
        entry = AnswerUtil.createAnswerEntry('key', '{}')
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.size() == 0
    }

    def 'Can get cached value'() {
        given:
        def value

        when: 'осуществляется переход назад - берем данные из кэша'
        value = component.getStoredValue('100', fieldComponent.id, new ScenarioDto(orderId: 1L,
                finishedAndCurrentScreens: ['s1', 's2', 's3'],
                display: new DisplayRequest(id: 's4'),
                cachedAnswers: [carInfo: new ApplicantAnswer(value: simpleComponentValue())]))

        then:
        value

        when: 'обновляем страницу - берем данные из кэша'
        value = component.getStoredValue('100', fieldComponent.id, new ScenarioDto(orderId: 1L,
                finishedAndCurrentScreens: ['s1', 's2', 's3'],
                display: new DisplayRequest(id: 's3', components: [new FieldComponent(id: 'carInfo', value: simpleComponentValue())])))

        then:
        value

        when: 'данных в кэше нет'
        value = component.getStoredValue('100', fieldComponent.id, new ScenarioDto(orderId: 1L,
                finishedAndCurrentScreens: ['s1', 's2', 's3'],
                display: new DisplayRequest(id: 's4')))

        then:
        !value

        when: 'данных в кэше нет, но есть в ответах - берем данные'
        value = component.getStoredValue('100', fieldComponent.id, new ScenarioDto(orderId: 1L,
                finishedAndCurrentScreens: ['s1', 's2', 's3'],
                display: new DisplayRequest(id: 's4'),
                applicantAnswers: [carInfo: new ApplicantAnswer(value: simpleComponentValue())]))

        then:
        value

        when: 'ищется автомобиль с другим VIN в кэше'
        value = component.getStoredValue('200', fieldComponent.id, new ScenarioDto(orderId: 1L,
                finishedAndCurrentScreens: ['s1', 's2', 's3'],
                display: new DisplayRequest(id: 's4'),
                cachedAnswers: [carInfo: new ApplicantAnswer(value: simpleComponentValue())]))

        then:
        !value

        when: 'ищется автомобиль с нужным VIN - берем данные из кэша'
        value = component.getStoredValue('100', fieldComponent.id, new ScenarioDto(orderId: 1L,
                finishedAndCurrentScreens: ['s1', 's2', 's3'],
                display: new DisplayRequest(id: 's4'),
                cachedAnswers: [carInfo: new ApplicantAnswer(value: simpleComponentValue())]))

        then:
        value
    }

    static def simpleComponentValue() {
        '{"vin":"100","vehicleInfo":{"status":"","legals":true,"restrictions":[],"ownerPeriods":[]},"notaryInfo":{"isPledged":true},"vehicleServiceCallResult":"SUCCESS","notaryServiceCallResult":"SUCCESS"}'
    }

    static def simpleScenarioDto() {
        new ScenarioDto(orderId: 1L,
                finishedAndCurrentScreens: ['s1', 's2', 's2a', 's3'],
                display: new DisplayRequest(id: 's2a'),
                cachedAnswers: [carInfo: new ApplicantAnswer(value: simpleComponentValue())])
    }

    static def simpleGetAsyncVehicleInfoResponse(VehicleInfo data = new VehicleInfo(status: ''), ExternalServiceCallResult callResult = ExternalServiceCallResult.SUCCESS) {
        CompletableFuture.supplyAsync({ ->
            new GibddServiceResponse<VehicleInfo>(data: data, externalServiceCallResult: callResult) })
    }

    static def simpleGetAsyncFederalNotaryInfoResponse(FederalNotaryInfo data = new FederalNotaryInfo(true), ExternalServiceCallResult callResult = ExternalServiceCallResult.SUCCESS) {
        CompletableFuture.supplyAsync({ ->
            new GibddServiceResponse<FederalNotaryInfo>(data: data, externalServiceCallResult: callResult) })
    }
}