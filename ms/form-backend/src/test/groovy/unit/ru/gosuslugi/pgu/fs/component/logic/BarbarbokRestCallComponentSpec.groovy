package unit.ru.gosuslugi.pgu.fs.component.logic

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.BackRestCallResponseDto
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.component.logic.BarbarbokRestCallComponent
import ru.gosuslugi.pgu.fs.component.logic.RestCallComponent
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto
import ru.gosuslugi.pgu.fs.service.BackRestCallService
import spock.lang.Specification

class BarbarbokRestCallComponentSpec extends Specification {

    private static data = "<ns1:InputData><ns1:FamilyName>Маск</ns1:FamilyName><ns1:FirstName>Илон</ns1:FirstName><ns1:Patronymic>Петрович</ns1:Patronymic><ns1:BirthDate>1971-06-28</ns1:BirthDate><ns1:UnitedPolicyNumber>2223410829888321</ns1:UnitedPolicyNumber></ns1:InputData>"
    private static templateName = "currentAttachment"

    private ScenarioDto scenarioDto
    private BarbarbokRestCallComponent component

    @SuppressWarnings("GroovyAccessibility")
    def setup() {
        def restCallComponent = new RestCallComponent("restCallUrl")
        def backRestCallService = Stub(BackRestCallService) {
            it.sendRequest(_ as RestCallDto) >>
                    new BackRestCallResponseDto(200, Map.<String, Object> of("key", "value"))
        }
        def userPersonalData = Stub(UserPersonalData) {
            it.getToken() >> "token"
        }
        def objectMapper = new ObjectMapper()

        scenarioDto = new ScenarioDto()
        scenarioDto.setServiceCode("10000000308")

        component = new BarbarbokRestCallComponent(
                restCallComponent,
                backRestCallService,
                userPersonalData,
                "smevConverterUrl"
        )
        component.objectMapper = objectMapper
        component.jsonProcessingService = new JsonProcessingServiceImpl(objectMapper)
    }

    def "getInitialValue success for pull"() {
        given:
        def fieldComponent = [
                attrs: [
                        path  : "path/path",
                        method: "get",
                        templateName: templateName
                ] as Map
        ] as FieldComponent

        when:
        component.preProcess(fieldComponent, scenarioDto)

        then:
        component.getType() == ComponentType.BarbarbokRestCall
        !fieldComponent.getAttrs().find()
    }

    def "getInitialValue success for push"() {
        given:
        def fieldComponent = [
                attrs: [
                        data  : data
                ] as Map
        ] as FieldComponent

        when:
        component.preProcess(fieldComponent, scenarioDto)

        then:
        !fieldComponent.getAttrs().find()
    }

    def "getInitialValue success for get"() {
        given:
        def fieldComponent = [
                attrs: [
                        data  : data,
                        templateName: templateName
                ] as Map
        ] as FieldComponent

        when:
        component.preProcess(fieldComponent, scenarioDto)

        then:
        !fieldComponent.getAttrs().find()
    }
}