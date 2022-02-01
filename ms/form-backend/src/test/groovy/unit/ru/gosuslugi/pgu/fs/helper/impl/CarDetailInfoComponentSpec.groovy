package unit.ru.gosuslugi.pgu.fs.helper.impl

import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.component.userdata.CarDetailInfoComponent
import ru.gosuslugi.pgu.fs.component.userdata.model.CarInfoComponentDto
import ru.gosuslugi.pgu.pgu_common.gibdd.service.GibddDataService
import ru.gosuslugi.pgu.ratelimit.client.RateLimitService
import spock.lang.Specification

class CarDetailInfoComponentSpec extends Specification {

    CarDetailInfoComponent component
    GibddDataService gibddDataServiceMock
    RateLimitService rateLimitServiceMock
    FieldComponent fieldComponent = new FieldComponent(id: 'carInfo', attrs: [tx: 'tx'], arguments: [vin: '100'])
    ComponentResponse<CarInfoComponentDto> result

    def setup() {
        UserPersonalData userPersonalData = Mock(UserPersonalData)
        userPersonalData.getPerson() >> new Person()


        gibddDataServiceMock = Mock(GibddDataService)
        rateLimitServiceMock = Mock(RateLimitService)

        component = new CarDetailInfoComponent(gibddDataServiceMock, userPersonalData, rateLimitServiceMock)
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
}