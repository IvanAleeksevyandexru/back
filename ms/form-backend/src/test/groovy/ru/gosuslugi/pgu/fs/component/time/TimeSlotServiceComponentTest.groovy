package ru.gosuslugi.pgu.fs.component.time

import com.fasterxml.jackson.core.type.TypeReference
import groovy.json.JsonSlurper
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.exception.BookingUnavailableException
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime

class TimeSlotServiceComponentTest extends Specification {

    def 'Time slot service helper test'() {
        given:
        def fieldComponent = fieldComponentWithEmptyAttrs()
        def component = createTimeSlotServiceComponent()

        when:
        ComponentTestUtil.setAbstractComponentServices(component)
        def initialValueFirst = component.getInitialValue(fieldComponent)
        def initialValueSecond = component.getInitialValue(fieldComponent, [] as ScenarioDto)
        def type = component.getType()

        then:
        initialValueFirst != null
        initialValueSecond != null
        type == ComponentType.TimeSlot
    }

    def 'it should return valid initial value (Empty Arguments Component)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def calculatedAttributesHelper = Mock(CalculatedAttributesHelper)
        def parseAttrValuesHelper = Mock(ParseAttrValuesHelper)
        def mainDescriptorService = Mock(MainDescriptorService)

        def scenarioDto = [] as ScenarioDto
        def fieldComponent = fieldComponentWithEmptyAttrs()
        def component = new TimeSlotServiceComponent(parseAttrValuesHelper, userPersonalData, calculatedAttributesHelper, mainDescriptorService)

        when:
        userPersonalData.getUserId() >> { 1000298933L }
        calculatedAttributesHelper.getAllCalculatedValues(_ as String, _ as FieldComponent, _ as ScenarioDto) >> { [] as HashMap<String, Object> }

        def componentResponse = component.getInitialValue(fieldComponent, scenarioDto)
        def responseAsMap = (([] as JsonSlurper).parseText(componentResponse.get())) as HashMap<String, Object>

        then:
        responseAsMap['userId']             == '1000298933'
        responseAsMap['waitingTimeExpired'] == false
    }

    def 'it should return valid initial value (Component Without Attributes)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def calculatedAttributesHelper = Mock(CalculatedAttributesHelper)
        def parseAttrValuesHelper = Mock(ParseAttrValuesHelper)
        def mainDescriptorService = Mock(MainDescriptorService)

        def scenarioDto = [] as ScenarioDto
        def fieldComponent = fieldComponentWithoutAttrs()
        def component = new TimeSlotServiceComponent(parseAttrValuesHelper, userPersonalData, calculatedAttributesHelper, mainDescriptorService)

        when:
        calculatedAttributesHelper.getAllCalculatedValues(_ as String, _ as FieldComponent, _ as ScenarioDto) >> { [] as HashMap<String, Object> }

        def componentResponse = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        componentResponse == ComponentResponse.empty()
    }

    def 'it should return valid initial value (TimeSlot with externalIntegration attr)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def calculatedAttributesHelper = Mock(CalculatedAttributesHelper)
        def parseAttrValuesHelper = Mock(ParseAttrValuesHelper)
        def mainDescriptorService = Mock(MainDescriptorService)

        def scenarioDto = [orderId: 1528050391, masterOrderId: 1L] as ScenarioDto
        def fieldComponent = fieldComponentVaccination()
        def component = new TimeSlotServiceComponent(parseAttrValuesHelper, userPersonalData, calculatedAttributesHelper, mainDescriptorService)

        when:
        userPersonalData.getUserId() >> { 1000298933L }
        fieldComponent.setArguments([ageperson: '39', pacientname: 'Мельникова Алена Владимировна', genderperson: 'Женский',]) // simulate resolved linkedValues
        parseAttrValuesHelper.getAttributeValue([type: 'REF', value: 'ms_moscow'] as HashMap<String, String>, scenarioDto) >> { departmentAsString() }
        calculatedAttributesHelper.getAllCalculatedValues(_ as String, _ as FieldComponent, _ as ScenarioDto) >> {
            [
                    eserviceId        : '10000104378',
                    serviceId         : '-10000006633',
                    serviceCode       : '-10000006633',
                    organizationId    : '125',
                    bookAttributes    : '[{\"name\":\"Session_Id\",\"value\":\"b2289fb3-2495-4dc9-8891-0ce5faa16fef\"}]',
                    Session_Id        : 'b2289fb3-2495-4dc9-8891-0ce5faa16fef',
                    Resource_Id       : '1986a1e8-aa1b-487e-8c76-28d045ac13b3',
                    MO_Id             : '125',
                    Starttime         : '00:00',
                    Endtime           : '23:59',
                    Enddate           : '31',
                    Service_Id        : '2011',
                    userSelectedRegion: '45000000000',
                    doctorname        : 'Гражданская_вакцинация/Повторная_вакцинация_V1.',
                    doctor            : 'Вакцинация от COVID-19',
                    anotherperson     : 'Y',
                    doctorid          : '1986a1e8-aa1b-487e-8c76-28d045ac13b3'
            ] as HashMap<String, Object>
        }

        def componentResponse = component.getInitialValue(fieldComponent, scenarioDto)
        def responseAsMap = (([] as JsonSlurper).parseText(componentResponse.get())) as HashMap<String, Object>

        then:
        /* calculations */
        responseAsMap['eserviceId']         == '10000104378'
        responseAsMap['serviceId']          == '-10000006633'
        responseAsMap['serviceCode']        == '-10000006633'
        responseAsMap['organizationId']     == '125'
        responseAsMap['bookAttributes']     == '[{"name":"Session_Id","value":"b2289fb3-2495-4dc9-8891-0ce5faa16fef"}]'
        responseAsMap['Session_Id']         == 'b2289fb3-2495-4dc9-8891-0ce5faa16fef'
        responseAsMap['Resource_Id']        == '1986a1e8-aa1b-487e-8c76-28d045ac13b3'
        responseAsMap['MO_Id']              == '125'
        responseAsMap['Starttime']          == '00:00'
        responseAsMap['Endtime']            == '23:59'
        responseAsMap['Enddate']            == '31'
        responseAsMap['Service_Id']         == '2011'
        responseAsMap['userSelectedRegion'] == '45000000000'
        responseAsMap['doctorname']         == 'Гражданская_вакцинация/Повторная_вакцинация_V1.'
        responseAsMap['doctor']             == 'Вакцинация от COVID-19'
        responseAsMap['anotherperson']      == 'Y'
        responseAsMap['doctorid']           == '1986a1e8-aa1b-487e-8c76-28d045ac13b3'

        /* linkedValues (arguments) */
        responseAsMap['ageperson']          == '39'
        responseAsMap['genderperson']       == 'Женский'
        responseAsMap['pacientname']        == 'Мельникова Алена Владимировна'

        /* additional props & attrs */
        responseAsMap['userId']             == '1000298933'
        responseAsMap['orderId']            == 1528050391
        responseAsMap['parentOrderId']      == 1
        responseAsMap['waitingTimeExpired'] == false
        responseAsMap['department']         == departmentAsString()
    }

    def 'it should thrown an error for unavailable booking'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def calculatedAttributesHelper = Mock(CalculatedAttributesHelper)
        def parseAttrValuesHelper = Mock(ParseAttrValuesHelper)
        def mainDescriptorService = Mock(MainDescriptorService)

        def scenarioDto = [] as ScenarioDto
        def fieldComponent = fieldComponentWithUnavailableBooking()
        def component = new TimeSlotServiceComponent(parseAttrValuesHelper, userPersonalData, calculatedAttributesHelper, mainDescriptorService)

        when:
        userPersonalData.getUserId() >> { 1000298933L }
        fieldComponent.setArguments([ageperson: '39', pacientname: 'Мельникова Алена Владимировна', genderperson: 'Женский',]) // simulate resolved linkedValues
        parseAttrValuesHelper.getAttributeValue([type: 'REF', value: 'ms_moscow'] as HashMap<String, String>, scenarioDto) >> { departmentAsString() }
        calculatedAttributesHelper.getAllCalculatedValues(_ as String, _ as FieldComponent, _ as ScenarioDto) >> {
            [
                    eserviceId        : '10000104378',
                    serviceId         : '-10000006633',
                    serviceCode       : '-10000006633',
                    organizationId    : 'BOOKING_UNAVAILABLE_EMPTY_ORG_ID',
                    bookAttributes    : '[{\"name\":\"Session_Id\",\"value\":\"b2289fb3-2495-4dc9-8891-0ce5faa16fef\"}]',
                    Session_Id        : 'b2289fb3-2495-4dc9-8891-0ce5faa16fef',
                    Resource_Id       : '1986a1e8-aa1b-487e-8c76-28d045ac13b3',
                    MO_Id             : '125',
                    Starttime         : '00:00',
                    Endtime           : '23:59',
                    Enddate           : '31',
                    Service_Id        : '2011',
                    userSelectedRegion: '45000000000',
                    doctorname        : 'Гражданская_вакцинация/Повторная_вакцинация_V1.',
                    doctor            : 'Вакцинация от COVID-19',
                    anotherperson     : 'Y',
                    doctorid          : '1986a1e8-aa1b-487e-8c76-28d045ac13b3'
            ] as HashMap<String, Object>
        }

        // target action
        component.getInitialValue(fieldComponent, scenarioDto)

        then:
        def exception = thrown(BookingUnavailableException)
        exception.message == 'Запись в данное подразделение невозможна'
    }

    def 'it should validate after submit (positive)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def calculatedAttributesHelper = Mock(CalculatedAttributesHelper)
        def parseAttrValuesHelper = Mock(ParseAttrValuesHelper)
        def mainDescriptorService = Mock(MainDescriptorService) {
            it.getServiceDescriptor("100") >> { new ServiceDescriptor() }
        }

        def incorrectAnswers = [] as HashMap<String, String>
        def answerEntry = AnswerUtil.createAnswerEntry('ts_moscow', answerEntryValueVaccination())

        def scenarioDto = [orderId: 1528050391, masterOrderId: 1L, serviceDescriptorId: "100"] as ScenarioDto
        def displayRequest = [components: [fieldComponentVaccination()]] as DisplayRequest
        def fieldComponent = fieldComponentVaccination()
        def component = new TimeSlotServiceComponent(parseAttrValuesHelper, userPersonalData, calculatedAttributesHelper, mainDescriptorService)

        when:
        ComponentTestUtil.setAbstractComponentServices(component)
        scenarioDto.setDisplay(displayRequest)
        userPersonalData.getUserId() >> { 1000298933L }
        fieldComponent.setArguments([
                ageperson   : '39',
                pacientname : 'Мельникова Алена Владимировна',
                genderperson: 'Женский',
        ]) // simulate resolved linkedValues
        parseAttrValuesHelper.getAttributeValue([type: 'REF', value: 'ms_moscow'] as HashMap<String, String>, scenarioDto) >> { departmentAsString() }
        calculatedAttributesHelper.getAllCalculatedValues(_ as String, _ as FieldComponent, _ as ScenarioDto) >> {
            [
                    eserviceId        : '10000104378',
                    serviceId         : '-10000006633',
                    serviceCode       : '-10000006633',
                    organizationId    : '125',
                    bookAttributes    : '[{\"name\":\"Session_Id\",\"value\":\"b2289fb3-2495-4dc9-8891-0ce5faa16fef\"}]',
                    Session_Id        : 'b2289fb3-2495-4dc9-8891-0ce5faa16fef',
                    Resource_Id       : '1986a1e8-aa1b-487e-8c76-28d045ac13b3',
                    MO_Id             : '125',
                    Starttime         : '00:00',
                    Endtime           : '23:59',
                    Enddate           : '31',
                    Service_Id        : '2011',
                    userSelectedRegion: '45000000000',
                    doctorname        : 'Гражданская_вакцинация/Повторная_вакцинация_V1.',
                    doctor            : 'Вакцинация от COVID-19',
                    anotherperson     : 'Y',
                    doctorid          : '1986a1e8-aa1b-487e-8c76-28d045ac13b3'
            ] as HashMap<String, Object>
        }

        // target action
        component.validateAfterSubmit(incorrectAnswers, answerEntry, scenarioDto, fieldComponent)
        def answerEntryAsMapAfterSubmit = (([] as JsonSlurper).parseText(answerEntry.getValue().getValue())) as HashMap<String, Object>

        then:
        incorrectAnswers.isEmpty()
        answerEntryAsMapAfterSubmit.containsKey('currentTime')
        answerEntryAsMapAfterSubmit.containsKey('timeStart')
        answerEntryAsMapAfterSubmit.containsKey('timeFinish')
    }

    def 'it should validate after submit (negative)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def calculatedAttributesHelper = Mock(CalculatedAttributesHelper)
        def parseAttrValuesHelper = Mock(ParseAttrValuesHelper)
        def mainDescriptorService = Mock(MainDescriptorService) {
            it.getServiceDescriptor("100") >> { new ServiceDescriptor() }
        }
        def incorrectAnswers = [] as HashMap<String, String>
        def answerEntry = AnswerUtil.createAnswerEntry('ts_moscow', answerEntryValueVaccination())

        def scenarioDto = [orderId: 1528050391, masterOrderId: 1L, serviceDescriptorId: "100"] as ScenarioDto
        def displayRequest = [components: [fieldComponentWithUnavailableBookingAttr()]] as DisplayRequest
        def fieldComponent = fieldComponentWithUnavailableBookingAttr()
        def component = new TimeSlotServiceComponent(parseAttrValuesHelper, userPersonalData, calculatedAttributesHelper, mainDescriptorService)

        when:
        ComponentTestUtil.setAbstractComponentServices(component)
        scenarioDto.setDisplay(displayRequest)
        userPersonalData.getUserId() >> { 1000298933L }
        fieldComponent.setArguments([ageperson: '39', pacientname: 'Мельникова Алена Владимировна', genderperson: 'Женский',]) // simulate resolved linkedValues
        parseAttrValuesHelper.getAttributeValue([type: 'REF', value: 'ms_moscow'] as HashMap<String, String>, scenarioDto) >> { departmentAsString() }
        calculatedAttributesHelper.getAllCalculatedValues(_ as String, _ as FieldComponent, _ as ScenarioDto) >> {
            [
                    eserviceId        : '10000104378',
                    serviceId         : '-10000006633',
                    serviceCode       : '-10000006633',
                    organizationId    : '125',
                    bookAttributes    : '[{\"name\":\"Session_Id\",\"value\":\"b2289fb3-2495-4dc9-8891-0ce5faa16fef\"}]',
                    Session_Id        : 'b2289fb3-2495-4dc9-8891-0ce5faa16fef',
                    Resource_Id       : '1986a1e8-aa1b-487e-8c76-28d045ac13b3',
                    MO_Id             : '125',
                    Starttime         : '00:00',
                    Endtime           : '23:59',
                    Enddate           : '31',
                    Service_Id        : '2011',
                    userSelectedRegion: '45000000000',
                    doctorname        : 'Гражданская_вакцинация/Повторная_вакцинация_V1.',
                    doctor            : 'Вакцинация от COVID-19',
                    anotherperson     : 'Y',
                    doctorid          : '1986a1e8-aa1b-487e-8c76-28d045ac13b3'
            ] as HashMap<String, Object>
        }

        // target action
        component.validateAfterSubmit(incorrectAnswers, answerEntry, scenarioDto, fieldComponent)

        then:
        def exception = thrown(FormBaseException)
        exception.message == "Компонент Timeslot не содержит аттрибута['timePeriodInMinutes'] указание временного периода для таймера"
    }

    def "isWaitingTimeExpired: #scenario"() {
        expect:
        scenario
        flag

        where:
        scenario << [
                'Слот еще не был выбран',
                'Слот был выбран и есть в кеше и просрочен', 'Слот был выбран и нет в кеше но есть в ответах и просрочен',
                'Слот был выбран и есть в кеше и НЕ просрочен', 'Слот был выбран и нет в кеше но есть в ответах и НЕ просрочен'
        ]

        flag << [
                !isWaitingTimeExpired(createTimeSlotServiceComponent(), fieldComponentWithEmptyAttrs(), new ScenarioDto()),
                isWaitingTimeExpired(createTimeSlotServiceComponent(), fieldComponentWithEmptyAttrs(), createTimeDependentFromCacheScenarioDto(true)),
                isWaitingTimeExpired(createTimeSlotServiceComponent(), fieldComponentWithEmptyAttrs(), createTimeDependentFromAnswersScenarioDto(true)),
                !isWaitingTimeExpired(createTimeSlotServiceComponent(), fieldComponentWithEmptyAttrs(), createTimeDependentFromCacheScenarioDto(false)),
                !isWaitingTimeExpired(createTimeSlotServiceComponent(), fieldComponentWithEmptyAttrs(), createTimeDependentFromAnswersScenarioDto(false))
        ]
    }

    def createTimeSlotServiceComponent() {
        ParseAttrValuesHelper parseAttrValuesHelper = new ParseAttrValuesHelper(null, null, null)
        UserPersonalData personalData = new UserPersonalData(userId: 1000298933)
        MainDescriptorService mainDescriptorService = Mock(MainDescriptorService)
        CalculatedAttributesHelper calculatedAttributesHelper = Mock(CalculatedAttributesHelper) {
            it.getAllCalculatedValues(_ as String, _ as FieldComponent, _ as ScenarioDto) >> { new HashMap<String, Object>() }
        }
        def timeSlotServiceComponent = new TimeSlotServiceComponent(parseAttrValuesHelper, personalData, calculatedAttributesHelper, mainDescriptorService)
        ComponentTestUtil.setAbstractComponentServices(timeSlotServiceComponent)
        return timeSlotServiceComponent
    }

    static def isWaitingTimeExpired(TimeSlotServiceComponent slotServiceComponent, FieldComponent component, ScenarioDto scenarioDto) {
        def value = slotServiceComponent.getInitialValue(component, scenarioDto).get()
        def map = JsonProcessingUtil.fromJson(value, new TypeReference<Map<String, Object>>() {})
        map['waitingTimeExpired']
    }

    static def fieldComponentWithEmptyAttrs() {
        [type: ComponentType.TimeSlot, attrs: [:], id: 'slotComponentId'] as FieldComponent
    }

    static def fieldComponentWithoutAttrs() {
        [type: ComponentType.TimeSlot, id: 'slotComponentId'] as FieldComponent
    }

    static def fieldComponentWithUnavailableBookingAttr() {
        [id: 'ts_empty', type: 'TimeSlot', attrs: [organizationId: 'BOOKING_UNAVAILABLE_EMPTY_ORG_ID']] as FieldComponent
    }

    static def fieldComponentWithUnavailableBooking() {
        [id: 'ts_empty', type: 'TimeSlot', attrs: [:]] as FieldComponent
    }

    static def fieldComponentVaccination() {
        [
                id          : 'ts_moscov',
                type        : 'TimeSlot',
                label       : '',
                attrs       : [
                        attributeNameWithAddress: 'Address_MO',
                        timePeriodInMinutes     : '300',
                        timeSlotType            : [type: 'CONST', value: 'VACCINATION'],
                        daysToShow              : 30,
                        startSection            : 'today',
                        isMonthsRangeVisible    : true,
                        externalIntegration     : 'medicalInfo',
                        department              : [type: 'REF', value: 'ms_moscow'],
                        calculations            : [
                                [attributeName: 'eserviceId', value: '10000104378', valueType: 'value'],
                                [attributeName: 'serviceId', value: '-10000006633', valueType: 'value'],
                                [attributeName: 'serviceCode', value: '-10000006633', valueType: 'value'],
                                [attributeName: 'organizationId', value: '125', valueType: 'value'],
                                [attributeName: 'bookAttributes', value: '[{\"name\":\"Session_Id\",\"value\":\"b2289fb3-2495-4dc9-8891-0ce5faa16fef\"}]', valueType: 'value'],
                                [attributeName: 'Session_Id', value: 'b2289fb3-2495-4dc9-8891-0ce5faa16fef', valueType: 'value'],
                                [attributeName: 'Resource_Id', value: '1986a1e8-aa1b-487e-8c76-28d045ac13b3', valueType: 'value'],
                                [attributeName: 'MO_Id', value: '125', valueType: 'value'],
                                [attributeName: 'Starttime', value: '00:00', valueType: 'value'],
                                [attributeName: 'Endtime', value: '23:59', valueType: 'value'],
                                [attributeName: 'Enddate', value: '31', valueType: 'value'],
                                [attributeName: 'Service_Id', value: '2011', valueType: 'value'],
                                [attributeName: 'userSelectedRegion', value: '45000000000', valueType: 'value'],
                                [attributeName: 'doctorname', value: 'Гражданская_вакцинация/Повторная_вакцинация_V1.', valueType: 'value'],
                                [attributeName: 'doctor', value: 'Вакцинация от COVID-19', valueType: 'value'],
                                [attributeName: 'anotherperson', value: 'Y', valueType: 'value'],
                                [attributeName: 'doctorid', value: '1986a1e8-aa1b-487e-8c76-28d045ac13b3', valueType: 'value'],
                        ]
                ],
                linkedValues: [
                        [argument: 'ageperson', defaultValue: '39'] as LinkedValue,
                        [argument: 'pacientname', defaultValue: 'Мельникова Алена Владимировна'] as LinkedValue,
                        [argument: 'genderperson', defaultValue: 'Женский'] as LinkedValue
                ],
                value       : ''
        ] as FieldComponent
    }

    static def answerEntryValueVaccination() {
        '{"orderId":1528050391,"timeSlotType":"VACCINATION","userId":"1000298933","timeSlotRequestAttrs":[{"name":"Session_Id","value":"2553c2be-e945-477c-b9a9-f6808dc134d1"},{"name":"Resource_Id","value":"8e7677b2-7ad9-4330-a78e-d5e152a8c70a"},{"name":"MO_Id","value":"125"},{"name":"Startdate","value":"18.10.2021"},{"name":"Enddate","value":"18.11.2021"},{"name":"Starttime","value":"00:00"},{"name":"Endtime","value":"23:59"},{"name":"Service_Id","value":"2011"},{"name":"ServiceSpec_Id","value":""}],"bookAttributes":"[{\\"name\\":\\"Session_Id\\",\\"value\\":\\"2553c2be-e945-477c-b9a9-f6808dc134d1\\"}]","bookingRequestParams":[{"name":"pacientname","value":"Мельникова Алена Владимировна"},{"name":"doctorname","value":"Гражданская_вакцинация/Повторная_вакцинация_V1."},{"name":"doctor","value":"Вакцинация от COVID-19"},{"name":"anotherperson","value":"Y"},{"name":"doctorid","value":"8e7677b2-7ad9-4330-a78e-d5e152a8c70a"},{"name":"ageperson","value":"39"},{"name":"genderperson","value":"Женский"}],"waitingTimeExpired":false,"department":"{\\"value\\":\\"125\\",\\"parentValue\\":null,\\"title\\":\\"Государственное бюджетное учреждение здравоохранения \\\\\\"Детская городская поликлиника № 143 Департамента здравоохранения города Москвы\\\\\\"  филиал № 4 \\",\\"isLeaf\\":true,\\"children\\":null,\\"attributes\\":[{\\"name\\":\\"Address_MO\\",\\"type\\":\\"STRING\\",\\"value\\":{\\"asString\\":\\"г. Москва, Хвалынский бульвар, д. 10\\",\\"asLong\\":null,\\"asDecimal\\":null,\\"asDateTime\\":null,\\"asDate\\":null,\\"asBoolean\\":null,\\"typeOfValue\\":\\"STRING\\",\\"value\\":\\"г. Москва, Хвалынский бульвар, д. 10\\"},\\"valueAsOfType\\":\\"г. Москва, Хвалынский бульвар, д. 10\\"},{\\"name\\":\\"Reg_Phone\\",\\"type\\":\\"STRING\\",\\"value\\":{\\"asString\\":\\"(495) 122-02-21\\",\\"asLong\\":null,\\"asDecimal\\":null,\\"asDateTime\\":null,\\"asDate\\":null,\\"asBoolean\\":null,\\"typeOfValue\\":\\"STRING\\",\\"value\\":\\"(495) 122-02-21\\"},\\"valueAsOfType\\":\\"(495) 122-02-21\\"}],\\"source\\":null,\\"attributeValues\\":{\\"Address_MO\\":\\"г. Москва, Хвалынский бульвар, д. 10\\",\\"Reg_Phone\\":\\"(495) 122-02-21\\"},\\"objectId\\":0,\\"center\\":[37.847281,55.697072],\\"baloonContent\\":[{\\"value\\":\\"г. Москва, Хвалынский бульвар, д. 10\\",\\"label\\":\\"Адрес\\"},{\\"value\\":\\"(495) 122-02-21\\",\\"label\\":\\"Телефон\\"}],\\"agreement\\":true,\\"idForMap\\":0,\\"expanded\\":true,\\"okato\\":\\"45000000000\\"}"}'
    }

    static def createTimeDependentFromCacheScenarioDto(Boolean isExpired) {
        [cachedAnswers: [slotComponentId: createTimeDependentApplicantAnswer(isExpired)]] as ScenarioDto
    }

    static def createTimeDependentFromAnswersScenarioDto(Boolean isExpired) {
        [applicantAnswers: [slotComponentId: createTimeDependentApplicantAnswer(isExpired)]] as ScenarioDto
    }

    static def createTimeDependentApplicantAnswer(Boolean isExpired) {
        def dateTimeNowMinusOneHour = ZonedDateTime.now(ZoneId.of("Z")).minusHours(1)
        def dateTimeNowPlusOneHour = ZonedDateTime.now(ZoneId.of("Z")).plusHours(1)

        isExpired
                ? [visited: true, value: applicantAnswerValueWithTimeFinishAt(dateTimeNowMinusOneHour)] as ApplicantAnswer
                : [visited: true, value: applicantAnswerValueWithTimeFinishAt(dateTimeNowPlusOneHour)] as ApplicantAnswer
    }

    static def departmentAsString() {
        '{"value":"125","parentValue":null,"title":"Государственное бюджетное учреждение здравоохранения \"Детская городская поликлиника № 143 Департамента здравоохранения города Москвы\"  филиал № 4 ","isLeaf":true,"children":null,"attributes":[{"name":"Address_MO","type":"STRING","value":{"asString":"г. Москва, Хвалынский бульвар, д. 10","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"г. Москва, Хвалынский бульвар, д. 10"},"valueAsOfType":"г. Москва, Хвалынский бульвар, д. 10"},{"name":"Reg_Phone","type":"STRING","value":{"asString":"(495) 122-02-21","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"(495) 122-02-21"},"valueAsOfType":"(495) 122-02-21"}],"source":null,"attributeValues":{"Address_MO":"г. Москва, Хвалынский бульвар, д. 10","Reg_Phone":"(495) 122-02-21"},"objectId":0,"center":[37.847281,55.697072],"baloonContent":[{"value":"г. Москва, Хвалынский бульвар, д. 10","label":"Адрес"},{"value":"(495) 122-02-21","label":"Телефон"}],"agreement":true,"idForMap":0,"expanded":true,"okato":"45000000000"}'
    }

    static def applicantAnswerValueWithTimeFinishAt(timeShift) {
        """{"bookId":"1500315f-a65a-40eb-97ed-3ed8bd53447c","esiaId":"1000466341","status":{"statusCode":201,"statusMessage":"Забронировано"},"timeSlot":{"slotId":"7fbb2711-b289-4348-bcf9-310d1f3483d8","serviceId":"10000000000456","organizationId":"R7700005","areaId":"Дом музыки","visitTime":1610805600000,"visitTimeStr":"2021-01-16T14:00:00.000","visitTimeISO":"2021-01-16T14:00:00Z","queueNumber":null,"duration":null,"attributes":[]},"error":null,"timeStart":"2021-02-16T10:09:52.742Z","timeFinish":"${timeShift}","department":{"value":"R7700005","parentValue":null,"title":"Гагаринский отдел ЗАГС Управления ЗАГС Москвы","isLeaf":true,"children":null,"attributes":[{"name":"ZAGS_NAME","type":"STRING","value":{"asString":"Гагаринский отдел ЗАГС Управления ЗАГС Москвы","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"Гагаринский отдел ЗАГС Управления ЗАГС Москвы"},"valueAsOfType":"Гагаринский отдел ЗАГС Управления ЗАГС Москвы"},{"name":"zags_address","type":"STRING","value":{"asString":"г Москва, пр-кт Ленинский , д.44","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"г Москва, пр-кт Ленинский , д.44"},"valueAsOfType":"г Москва, пр-кт Ленинский , д.44"},{"name":"TYPE","type":"STRING","value":{"asString":"ZAGS","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"ZAGS"},"valueAsOfType":"ZAGS"},{"name":"SHOW_ON_MAP","type":"STRING","value":{"asString":"true","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"true"},"valueAsOfType":"true"},{"name":"SOLEMN","type":"STRING","value":{"asString":"true","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"true"},"valueAsOfType":"true"},{"name":"AREA_DESCR","type":"STRING","value":{"asString":null,"asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":null},"valueAsOfType":null},{"name":"DATAK","type":"STRING","value":{"asString":null,"asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":null},"valueAsOfType":null},{"name":"AREA_NAME","type":"STRING","value":{"asString":null,"asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":null},"valueAsOfType":null},{"name":"CODE","type":"STRING","value":{"asString":"R7700005","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"R7700005"},"valueAsOfType":"R7700005"},{"name":"FULLNAME","type":"STRING","value":{"asString":"Гагаринский отдел ЗАГС Управления ЗАГС Москвы","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"Гагаринский отдел ЗАГС Управления ЗАГС Москвы"},"valueAsOfType":"Гагаринский отдел ЗАГС Управления ЗАГС Москвы"},{"name":"ADDRESS","type":"STRING","value":{"asString":"г Москва, пр-кт Ленинский , д.44","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"г Москва, пр-кт Ленинский , д.44"},"valueAsOfType":"г Москва, пр-кт Ленинский , д.44"},{"name":"PHONE","type":"STRING","value":{"asString":"8(499)137-32-42","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"8(499)137-32-42"},"valueAsOfType":"8(499)137-32-42"},{"name":"EMAIL","type":"STRING","value":{"asString":"zags@mos.ru","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"zags@mos.ru"},"valueAsOfType":"zags@mos.ru"},{"name":"PR2","type":"STRING","value":{"asString":"true","asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":"true"},"valueAsOfType":"true"},{"name":"GET_CONSENT","type":"STRING","value":{"asString":null,"asLong":null,"asDecimal":null,"asDateTime":null,"asDate":null,"asBoolean":null,"typeOfValue":"STRING","value":null},"valueAsOfType":null}],"source":null,"attributeValues":{"ZAGS_NAME":"Гагаринский отдел ЗАГС Управления ЗАГС Москвы","AREA_NAME":null,"PHONE":"8(499)137-32-42","SOLEMN":"true","zags_address":"г Москва, пр-кт Ленинский , д.44","EMAIL":"zags@mos.ru","GET_CONSENT":null,"PR2":"true","CODE":"R7700005","SHOW_ON_MAP":"true","ADDRESS":"г Москва, пр-кт Ленинский , д.44","DATAK":null,"TYPE":"ZAGS","AREA_DESCR":null,"FULLNAME":"Гагаринский отдел ЗАГС Управления ЗАГС Москвы"},"idForMap":0,"center":[37.56715,55.701318],"baloonContent":[{"value":"г Москва, пр-кт Ленинский , д.44","label":"Адрес"},{"value":"8(499)137-32-42","label":"Телефон"},{"value":"zags@mos.ru","label":"Email"}],"agreement":true,"expanded":true,"okato":"45000000000"}}"""
    }
}
