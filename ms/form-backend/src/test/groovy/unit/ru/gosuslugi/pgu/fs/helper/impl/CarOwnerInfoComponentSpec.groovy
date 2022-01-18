package unit.ru.gosuslugi.pgu.fs.helper.impl

import ru.atc.carcass.security.rest.model.person.Person
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.component.userdata.CarOwnerInfoComponent
import spock.lang.Specification

class CarOwnerInfoComponentSpec extends Specification {

    CarOwnerInfoComponent component

    def setup() {
        UserPersonalData userPersonalData = Mock(UserPersonalData)
        userPersonalData.getPerson() >> new Person(lastName: 'Иванов', firstName: 'Иван', middleName: 'Иванович')
        userPersonalData.getDocs() >> [new PersonDoc(type: 'RF_PASSPORT', series: '111', number: '222', issueDate: '20.11.2010')]

        component = new CarOwnerInfoComponent()
    }

    def 'Can get initial value'() {
        given:
        FieldComponent fieldComponent = createFieldComponent()
        ScenarioDto scenarioDto = new ScenarioDto(
                applicantAnswers: [carList: new ApplicantAnswer(value:
                        """
                        {"vehicleInfo":{"restrictions": [],"ownerPeriods": [],"ptsNum": "555","restrictionsFlag": true,
                            "owner":{"lastName": "Иванов","documentNumSer": "777111","idDocumentType": "Свидетельство о регистрации"}},
                        "notaryInfo":{"isPledged":false},
                        "vehicleServiceCallResult":"SUCCESS",
                        "notaryServiceCallResult":"SUCCESS"}
                        """)])
        def result

        when:
        result = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        result
        def dto =  result.get()
        dto.ownerInfo.fullName == 'Иванов'
        dto.ownerInfo.document.documentType == 'Свидетельство о регистрации'
        dto.ownerInfo.document.seriesAndNumber == '777111'
        dto.ownerInfo.document.issueDate == ""
        dto.vehicleServiceCallResult.name() == 'SUCCESS'
        dto.vehicleInfo.restrictions == []
        dto.vehicleInfo.ptsNum == '555'
        fieldComponent.attrs['actions'] == [[label: 'Нельзя продавать', showIs: 'cannotBeSold']]

        when:
        fieldComponent = createFieldComponent()
        scenarioDto.applicantAnswers['carList'] = new ApplicantAnswer(value:
                """
                {"vehicleInfo":{"restrictions":[],"ownerPeriods":[],"ptsNum":"555","restrictionsFlag":false,"searchingTransportFlag":false},
                "notaryInfo":{"isPledged":false},
                "vehicleServiceCallResult":"SUCCESS",
                "notaryServiceCallResult":"SUCCESS"}
                """)
        result = component.getInitialValue(fieldComponent, scenarioDto)

        then:
        fieldComponent.attrs['actions'] == [[label: 'Можно продавать', showIs: 'canBeSold']]
        result.get().ownerInfo == null
    }

    static def createFieldComponent() {
        new FieldComponent
                (attrs: [carListRef: 'carList',
                         actions   : [[label: 'Можно продавать', showIs: 'canBeSold'], [label: 'Нельзя продавать', showIs: 'cannotBeSold']]])
    }
}