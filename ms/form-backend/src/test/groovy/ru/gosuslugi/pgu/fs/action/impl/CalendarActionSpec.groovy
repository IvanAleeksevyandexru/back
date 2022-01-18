package ru.gosuslugi.pgu.fs.action.impl

import ru.gosuslugi.pgu.common.core.exception.ValidationException
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.action.ActionRequestDto
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import spock.lang.Specification

class CalendarActionSpec extends Specification {
    MainDescriptorService mainDescriptorService = Stub(MainDescriptorService)
    JsonProcessingServiceImpl jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
    CalculatedAttributesHelper calculatedAttributesHelper = Stub(CalculatedAttributesHelper)

    CalendarAction calendarAction = new CalendarAction(mainDescriptorService, jsonProcessingService, calculatedAttributesHelper)

    def 'getVDateTimeUtc'() {
        when:
        def result = CalendarActionBuilder.getVDateTimeUtc(visitTimeISO)

        then:
        result == expectedResult

        where:
        visitTimeISO                | expectedResult
        '2021-01-14T09:45:00+03:00' | '20210114T064500Z'
        '2020-12-26T21:14:21Z'      | '20201226T181421Z'
    }

    def 'invokeTestWithTimeSlot'() {
        given:
        ServiceDescriptor serviceDescriptor = jsonProcessingService.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(),'-serviceDescriptor-mapService-timeSlot.json'), ServiceDescriptor.class)
        ScenarioDto scenarioDto = jsonProcessingService.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(),'-scenarioDto-mapService-timeSlot.json'), ScenarioDto.class)
        ActionRequestDto actionRequestDto = new ActionRequestDto(scenarioDto: scenarioDto)
        mainDescriptorService.getServiceDescriptor('1') >> serviceDescriptor
        calculatedAttributesHelper.getAllCalculatedValues(_, _, _) >> Map.of('subject', 'Замена водительского удостоверения')

        when:
        def response = calendarAction.invoke(actionRequestDto)

        then:
        response != null
        response.responseData != null && !response.responseData.isEmpty()
        response.responseData.get('type') == 'text/calendar'
        List<String> calendarLines = ((String) response.responseData.get('value')).split('\\r\\n')
        calendarLines.size() == 18
        calendarLines.get(0) == 'BEGIN:VCALENDAR'
        calendarLines.get(1) == 'PRODID:-//EPGU 2.0//iCal4j 2.0//EN'
        calendarLines.get(2) == 'VERSION:2.0'
        calendarLines.get(3) == 'CALSCALE:GREGORIAN'
        calendarLines.get(4) == 'METHOD:REQUEST'
        calendarLines.get(5) == 'BEGIN:VEVENT'
        calendarLines.get(6).matches('DTSTAMP:\\d{8}T\\d{6}Z')
        calendarLines.get(7) == 'DTSTART:20210422T064500Z'
        calendarLines.get(8) == 'DTEND:20210422T064500Z'
        calendarLines.get(9) == 'ORGANIZER;CN=\"Госуслуги\":mailto:no-reply@gosuslugi.ru'
        calendarLines.get(10) == 'SUMMARY:Замена водительского удостоверения'
        calendarLines.get(11) == 'LOCATION:Москва г\\, 2-я Измайловского Зверинца ул\\, д. 2А'
        calendarLines.get(12).matches('UID:.*')
        calendarLines.get(13).matches('DESCRIPTION:Услуга: Замена водительского удостоверения\\\\n Дата и время события: 22\\.04\\.2021 09:45 \\(в часовом поясе подразделения\\)\\\\n Ведомство: 1 ОЭР МО ГИБДД ТНРЭР № 3 ГУ МВД России по г. Москве  \\\\n Адрес: Москва г\\\\\\, 2-я Измайловского Зверинца ул\\\\\\, д\\. 2А.*')
        calendarLines.get(14) == 'SEQUENCE:1'
        calendarLines.get(15).matches('X-ALT-DESC;FMTTYPE=text/html:.*')
        calendarLines.get(16) == 'END:VEVENT'
        calendarLines.get(17) == 'END:VCALENDAR'
    }

    def 'invokeTestWithTimeSlotDoctor'() {
        given:
        ServiceDescriptor serviceDescriptor = jsonProcessingService.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), serviceDescriptorSuffix), ServiceDescriptor.class)
        ScenarioDto scenarioDto = jsonProcessingService.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), scenarioDtoSuffix), ScenarioDto.class)
        ActionRequestDto actionRequestDto = new ActionRequestDto(scenarioDto: scenarioDto)
        mainDescriptorService.getServiceDescriptor('1') >> serviceDescriptor

        when:
        def response = calendarAction.invoke(actionRequestDto)

        then:
        response != null
        response.responseData != null && !response.responseData.isEmpty()
        response.responseData.get('type') == 'text/calendar'
        List<String> calendarLines = ((String) response.responseData.get('value')).split('\\r\\n')
        calendarLines.size() == 18
        calendarLines.get(0) == 'BEGIN:VCALENDAR'
        calendarLines.get(1) == 'PRODID:-//EPGU 2.0//iCal4j 2.0//EN'
        calendarLines.get(2) == 'VERSION:2.0'
        calendarLines.get(3) == 'CALSCALE:GREGORIAN'
        calendarLines.get(4) == 'METHOD:REQUEST'
        calendarLines.get(5) == 'BEGIN:VEVENT'
        calendarLines.get(6).matches('DTSTAMP:\\d{8}T\\d{6}Z')
        calendarLines.get(7) == 'DTSTART:20210821T060000Z'
        calendarLines.get(8) == 'DTEND:20210821T060000Z'
        calendarLines.get(9) == 'ORGANIZER;CN=\"Госуслуги\":mailto:no-reply@gosuslugi.ru'
        calendarLines.get(10) == 'SUMMARY:Запись на прием к врачу'
        calendarLines.get(11) == 'LOCATION:Республика Татарстан\\, г.Казань\\, ул.Рихарда Зорге\\, д.103'
        calendarLines.get(12).matches('UID:.*')
        calendarLines.get(13).matches('DESCRIPTION:Услуга: Запись на прием к врачу\\\\n Дата и время события: 21\\.08\\.2021 09:00 \\(в часовом поясе подразделения\\)\\\\n Ведомство: Женская консультация\\\\, ГАУЗ "Городская поликлиника № 21" \\\\n Адрес: Республика Татарстан\\\\, г.Казань\\\\, ул.Рихарда Зорге\\\\, д.103.*')
        calendarLines.get(14) == 'SEQUENCE:1'
        calendarLines.get(15).matches('X-ALT-DESC;FMTTYPE=text/html:.*')
        calendarLines.get(16) == 'END:VEVENT'
        calendarLines.get(17) == 'END:VCALENDAR'

        where:
        serviceDescriptorSuffix                             | scenarioDtoSuffix
        '-serviceDescriptor-mapService-timeSlotDoctor.json' | '-scenarioDto-mapService-timeSlotDoctor.json'
        '-serviceDescriptor-medRef-timeSlotDoctor.json'     | '-scenarioDto-medRef-timeSlotDoctor.json'
        '-serviceDescriptor-refNum-timeSlotDoctor.json'     | '-scenarioDto-refNum-timeSlotDoctor.json'
    }

    def 'get exception if there is no time slot in answers'() {
        given:
        ServiceDescriptor serviceDescriptor = jsonProcessingService.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), '-serviceDescriptor-mapService-timeSlot.json'), ServiceDescriptor.class)
        ScenarioDto scenarioDto = jsonProcessingService.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), '-scenarioDto-noTimeSlot.json'), ScenarioDto.class)
        ActionRequestDto actionRequestDto = new ActionRequestDto(scenarioDto: scenarioDto)
        mainDescriptorService.getServiceDescriptor('1') >> serviceDescriptor

        when:
        calendarAction.invoke(actionRequestDto)

        then:
        thrown ValidationException
    }
}
