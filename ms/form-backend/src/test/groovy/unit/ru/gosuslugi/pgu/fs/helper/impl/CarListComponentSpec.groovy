package unit.ru.gosuslugi.pgu.fs.helper.impl

import ru.atc.carcass.security.rest.model.person.Person
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.userdata.CarListComponent
import ru.gosuslugi.pgu.fs.component.userdata.model.CarListComponentDto
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.ExternalServiceCallResult
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryInfo
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryRequest
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.OwnerVehiclesRequest
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleInfo
import ru.gosuslugi.pgu.pgu_common.gibdd.service.GibddDataService
import spock.lang.Specification

import java.time.LocalDate

class CarListComponentSpec extends Specification {

    CarListComponent component
    GibddDataService gibddDataServiceMock

    FieldComponent fieldComponent = new FieldComponent(attrs: [tx: 'tx'])

    def setup() {
        UserPersonalData userPersonalData = Mock(UserPersonalData)
        userPersonalData.getPerson() >> new Person()
        userPersonalData.getDocs() >> [new PersonDoc(type: 'RF_PASSPORT', series: '111', number: '222')]

        gibddDataServiceMock = Mock(GibddDataService)

        component = new CarListComponent(gibddDataServiceMock, userPersonalData)
    }

    def 'Can get initial value'() {
        given:
        ComponentResponse< CarListComponentDto> response

        when:
        gibddDataServiceMock.getOwnerVehiclesInfo(_ as OwnerVehiclesRequest) >> [new VehicleInfo(status: '')]
        response = component.getInitialValue(fieldComponent, Stub(ScenarioDto))

        then:
        response.get().vehicles.size() == 1
        response.get().vehicleServiceCallResult == ExternalServiceCallResult.SUCCESS
    }

    def 'Can get initial value if vehicles not found'() {
        given:
        ComponentResponse< CarListComponentDto> response

        when:
        gibddDataServiceMock.getOwnerVehiclesInfo(_ as OwnerVehiclesRequest) >> []
        response = component.getInitialValue(fieldComponent, Stub(ScenarioDto))

        then:
        response.get().vehicles.size() == 0
        response.get().vehicleServiceCallResult == ExternalServiceCallResult.NOT_FOUND_ERROR
    }

    def 'Can get initial value if service throw exception'() {
        given:
        ComponentResponse< CarListComponentDto> response

        when:
        gibddDataServiceMock.getOwnerVehiclesInfo(_ as OwnerVehiclesRequest) >> {
            throw new ExternalServiceException('')
        }
        response = component.getInitialValue(fieldComponent, Stub(ScenarioDto))

        then:
        response.get().vehicleServiceCallResult == ExternalServiceCallResult.EXTERNAL_SERVER_ERROR
    }

    def 'Can validate submit value'() {
        given:
        Map<String, String> incorrectAnswers = [:]
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry('key', '0')
        ScenarioDto scenarioDto = new ScenarioDto(orderId: 1L, display:
                new DisplayRequest(components: [new FieldComponent(id: entry.key, arguments: [vin: 'vin'])]))

        when:
        gibddDataServiceMock.getOwnerVehiclesInfo(_ as OwnerVehiclesRequest) >> [new VehicleInfo(status: '')]
        gibddDataServiceMock.getFederalNotaryInfo(_ as FederalNotaryRequest) >> new FederalNotaryInfo(true)
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.size() == 1
        incorrectAnswers.get(entry.getKey()) == 'повторный запрос данных'
        scenarioDto.display.components[0].value ==
                '{"vehicles":[{"status":"","legals":true,"restrictions":[],"ownerPeriods":[]}],"vehicleServiceCallResult":"SUCCESS"}'

        when:
        incorrectAnswers = [:]
        entry = AnswerUtil.createAnswerEntry('key', '{"vin":"vin"}')
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.size() == 0
        entry.value.value == '{"vehicleInfo":{"legals":true,"vin":"vin","restrictions":[],"ownerPeriods":[],"regActions":[]},' +
                '"notaryInfo":{"isPledged":true},"approveDate":"' + LocalDate.now().toString() + '","vehicleServiceCallResult":"SUCCESS","notaryServiceCallResult":"SUCCESS"}'
    }
}