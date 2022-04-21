package unit.ru.gosuslugi.pgu.fs.component


import ru.atc.carcass.security.rest.model.orgs.Org
import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.component.confirm.ConfirmLegalDataComponent
import ru.gosuslugi.pgu.fs.service.impl.UserDataServiceImpl
import spock.lang.Specification

class ConfirmLegalDataComponentSpec extends Specification {

    public static final String ORG_FULL_NAME = 'ИП Ипешный'
    ConfirmLegalDataComponent confirmLegalDataComponent
    UserOrgData userOrgData = Mock(UserOrgData)
    UserPersonalData userPersonalData = Mock(UserPersonalData)

    def setup() {
        userOrgData.org >> new Org(
                id: '123',
                fullName: ORG_FULL_NAME,
                ogrn: '12345',
                inn: '67890')
        userOrgData.chiefs >> [
                new Person(
                        userId: '111',
                        firstName: 'Марк',
                        lastName: 'Морковкин',
                        birthDate: '10.01.2010'
                )
        ]
        userPersonalData.getUserId() >> 111
        confirmLegalDataComponent = new ConfirmLegalDataComponent(
                userOrgData,
                new UserDataServiceImpl(userPersonalData, userOrgData)
        )
    }

    def 'Can get organization info'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'fullName'],
                                                                 [fieldName: 'ogrn', label: 'ОГРНИП'],
                                                                 [fieldName: 'inn', label: 'ИНН']]])

        when:
        def result = confirmLegalDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 1
        result.get().states[0].groupName == ORG_FULL_NAME
        result.get().states[0].fields.size() == 2
        def fields = result.get().states[0].fields
        fields[0].value == '12345'
        fields[0].label == 'ОГРНИП'
        fields[1].value == '67890'
        fields[1].label == 'ИНН'
        def storedValues = result.get().storedValues
        storedValues.fullName == ORG_FULL_NAME
        storedValues.ogrn == '12345'
        storedValues.inn == '67890'
    }

    def 'Can get organization and chief info'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'fullName', label: 'Полное наименование'],
                                                                 [fieldName: 'shortName', label: 'Краткое наименование'],
                                                                 [fieldName: 'ogrn', label: 'ОГРНИП'],
                                                                 [fieldName: 'inn', label: 'ИНН'],
                                                                 [fieldName: 'kpp', label: 'КПП'],
                                                                 [fieldName: 'chiefFirstName', label: ''],
                                                                 [fieldName: 'chiefLastName', label: ''],
                                                                 [fieldName: 'chiefMiddleName', label: ''],
                                                                 [fieldName: 'chiefBirthDate', label: 'Дата рождения']]])

        when:
        def result = confirmLegalDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().states[1].groupName == 'Данные о руководителе'
        result.get().states[1].fields.size() == 2
        def fields = result.get().states[1].fields
        fields[0].value == 'Морковкин Марк'
        fields[0].label == ''
        fields[1].value == '10.01.2010'
        fields[1].label == 'Дата рождения'
        def storedValues = result.get().storedValues
        storedValues.chiefFirstName == 'Марк'
        storedValues.chiefBirthDate == '10.01.2010'
    }
}
