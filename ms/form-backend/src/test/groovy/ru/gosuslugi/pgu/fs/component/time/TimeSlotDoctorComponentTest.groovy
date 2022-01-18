package ru.gosuslugi.pgu.fs.component.time

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.TimeSlotDoctorInput
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TimeSlotDoctorComponentTest extends Specification {
    def DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    TimeSlotDoctorComponent timeSlotDoctorComponent

    def setup() {
        def userPersonalData = Mock(UserPersonalData)
        def jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
        def serviceIdVariable = new ServiceIdVariable(Stub(MainDescriptorService), jsonProcessingService, Stub(RuleConditionService))
        def parseAttrValuesHelper = new ParseAttrValuesHelper(Stub(VariableRegistry), jsonProcessingService, Stub(ProtectedFieldService))
        def calculatedAttributesHelper = new CalculatedAttributesHelper(serviceIdVariable, parseAttrValuesHelper, null)
        calculatedAttributesHelper.postConstruct()
        timeSlotDoctorComponent = new TimeSlotDoctorComponent(userPersonalData, calculatedAttributesHelper, parseAttrValuesHelper)
    }

    def "Empty preset values check"() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.TimeSlotDoctor)
        def scenarioDto = new ScenarioDto()

        when: "No attrs in component description"
        ComponentResponse<TimeSlotDoctorInput> initialValue = timeSlotDoctorComponent.getInitialValue(fieldComponent, scenarioDto);

        then:
        initialValue.get() == null
    }

    def "Not empty preset values check (smev 3)"() {
        given:
        def fieldComponent = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-smev_component.json"), FieldComponent)
        def scenarioDto = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), '-smev_scenarioDto.json'), ScenarioDto)
        def currentDate = LocalDate.now()

        when:
        ComponentResponse<TimeSlotDoctorInput> initialValue = timeSlotDoctorComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        initialValue.get() != null
        JsonProcessingUtil.toJson(initialValue.get()) ==
                "{\"orderId\":764013769,\"eserviceId\":\"10000025167\",\"serviceId\":\"10001000603\",\"serviceCode\":\"-10001000603\",\"department\":\"{\\\"value\\\":\\\"221\\\",\\\"parentValue\\\":null,\\\"title\\\":\\\"Женская консультация, ГАУЗ \\\\\\\"Городская поликлиника № 21\\\\\\\"\\\",\\\"isLeaf\\\":true,\\\"children\\\":null,\\\"attributes\\\":[{\\\"name\\\":\\\"MO_Oid\\\",\\\"type\\\":\\\"STRING\\\",\\\"value\\\":{\\\"asString\\\":\\\"1.2.643.5.1.13.13.12.2.16.1179.0.221506\\\",\\\"asLong\\\":null,\\\"asDecimal\\\":null,\\\"asDateTime\\\":null,\\\"asDate\\\":null,\\\"asBoolean\\\":null,\\\"typeOfValue\\\":\\\"STRING\\\",\\\"value\\\":\\\"1.2.643.5.1.13.13.12.2.16.1179.0.221506\\\"},\\\"valueAsOfType\\\":\\\"1.2.643.5.1.13.13.12.2.16.1179.0.221506\\\"},{\\\"name\\\":\\\"Address_MO\\\",\\\"type\\\":\\\"STRING\\\",\\\"value\\\":{\\\"asString\\\":\\\"Республика Татарстан, г.Казань, ул.Рихарда Зорге, д.107\\\",\\\"asLong\\\":null,\\\"asDecimal\\\":null,\\\"asDateTime\\\":null,\\\"asDate\\\":null,\\\"asBoolean\\\":null,\\\"typeOfValue\\\":\\\"STRING\\\",\\\"value\\\":\\\"Республика Татарстан, г.Казань, ул.Рихарда Зорге, д.107\\\"},\\\"valueAsOfType\\\":\\\"Республика Татарстан, г.Казань, ул.Рихарда Зорге, д.107\\\"},{\\\"name\\\":\\\"Reg_Phone\\\",\\\"type\\\":\\\"STRING\\\",\\\"value\\\":{\\\"asString\\\":\\\"(843) 466-60-67\\\",\\\"asLong\\\":null,\\\"asDecimal\\\":null,\\\"asDateTime\\\":null,\\\"asDate\\\":null,\\\"asBoolean\\\":null,\\\"typeOfValue\\\":\\\"STRING\\\",\\\"value\\\":\\\"(843) 466-60-67\\\"},\\\"valueAsOfType\\\":\\\"(843) 466-60-67\\\"}],\\\"source\\\":null,\\\"attributeValues\\\":{\\\"Address_MO\\\":\\\"Республика Татарстан, г.Казань, ул.Рихарда Зорге, д.107\\\",\\\"MO_Oid\\\":\\\"1.2.643.5.1.13.13.12.2.16.1179.0.221506\\\",\\\"Reg_Phone\\\":\\\"(843) 466-60-67\\\"},\\\"objectId\\\":0,\\\"center\\\":[49.225593,55.744064],\\\"baloonContent\\\":[{\\\"value\\\":\\\"Республика Татарстан, г.Казань, ул.Рихарда Зорге, д.107\\\",\\\"label\\\":\\\"Адрес\\\"},{\\\"value\\\":\\\"(843) 466-60-67\\\",\\\"label\\\":\\\"Телефон\\\"}],\\\"agreement\\\":true,\\\"idForMap\\\":0,\\\"expanded\\\":true,\\\"okato\\\":\\\"55000000000\\\"}\",\"timeSlotRequestAttrs\":[{\"name\":\"Startdate\",\"value\":\"" + DATE_TIME_FORMATTER.format(currentDate) + "\"},{\"name\":\"Starttime\",\"value\":\"00:00\"},{\"name\":\"Enddate\",\"value\":\"" + DATE_TIME_FORMATTER.format(currentDate.plusDays(14)) + "\"},{\"name\":\"Endtime\",\"value\":\"23:59\"},{\"name\":\"Session_Id\",\"value\":\"61965db9-5bb2-4fa7-9528-eb4c3a5ca7a8\"},{\"name\":\"Service_Id\",\"value\":\"\"},{\"name\":\"ServiceSpec_Id\",\"value\":\"\"},{\"name\":\"MO_Id\",\"value\":\"221\"}],\"bookingRequestAttrs\":[{\"name\":\"doctor\",\"value\":\"\"},{\"name\":\"anotherperson\",\"value\":\"N\"},{\"name\":\"genderperson\",\"value\":\"Мужской\"},{\"name\":\"ageperson\",\"value\":\"\"},{\"name\":\"pacientname\",\"value\":\"\"}],\"organizationId\":\"221\",\"bookAttributes\":\"[{\\\"name\\\":\\\"Session_Id\\\",\\\"value\\\":\\\"61965db9-5bb2-4fa7-9528-eb4c3a5ca7a8\\\"}]\",\"userSelectedRegion\":\"55000000000\"}"
    }

    def "Not empty preset values check (med ref case)"() {
        given:
        def fieldComponent = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-ref_component.json"), FieldComponent)
        def scenarioDto = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-ref_scenarioDto.json"), ScenarioDto)
        def currentDate = LocalDate.now()

        when:
        ComponentResponse<TimeSlotDoctorInput> initialValue = timeSlotDoctorComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        initialValue.get() != null
        JsonProcessingUtil.toJson(initialValue.get()) ==
                "{\"orderId\":764013797,\"eserviceId\":\"10000025167\",\"serviceId\":\"10001000603\",\"serviceCode\":\"-10001000603\",\"timeSlotRequestAttrs\":[{\"name\":\"Startdate\",\"value\":\"" + DATE_TIME_FORMATTER.format(currentDate) + "\"},{\"name\":\"Starttime\",\"value\":\"00:00\"},{\"name\":\"Enddate\",\"value\":\"" + DATE_TIME_FORMATTER.format(currentDate.plusDays(14)) + "\"},{\"name\":\"Endtime\",\"value\":\"23:59\"},{\"name\":\"Session_Id\",\"value\":\"de9d5a48-827c-4f61-a4f5-5cc2ad80884f\"},{\"name\":\"Service_Id\",\"value\":\"A16.08.010.001\"},{\"name\":\"ServiceSpec_Id\",\"value\":\"53\"},{\"name\":\"MO_Id\",\"value\":\"\"}],\"bookingRequestAttrs\":[{\"name\":\"doctor\",\"value\":\"врач-оториноларинголог\"},{\"name\":\"anotherperson\",\"value\":\"N\"},{\"name\":\"genderperson\",\"value\":\"Мужской\"},{\"name\":\"ageperson\",\"value\":\"\"},{\"name\":\"pacientname\",\"value\":\"\"}],\"bookAttributes\":\"[{\\\"name\\\":\\\"Session_Id\\\",\\\"value\\\":\\\"de9d5a48-827c-4f61-a4f5-5cc2ad80884f\\\"}]\",\"userSelectedRegion\":\"55000000000\"}"
    }

}
