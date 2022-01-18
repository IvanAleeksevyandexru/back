package unit.ru.gosuslugi.pgu.fs.component

import ru.atc.carcass.security.rest.model.person.Person
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.component.FormDto
import ru.gosuslugi.pgu.fs.component.confirm.ConfirmPersonalUserDataComponent
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalUserData
import spock.lang.Specification

class ConfirmPersonalUserDataComponentSpec extends Specification {

    ConfirmPersonalUserDataComponent confirmPersonalUserDataComponent
    UserPersonalData userPersonalDataMock = Mock(UserPersonalData)

    def setup() {
        confirmPersonalUserDataComponent = new ConfirmPersonalUserDataComponent(userPersonalDataMock)
    }

    def 'Can get RF passport'() {
        given:
        def fieldComponent = new FieldComponent(
                attrs: [fields: getRFPassportFields() << [fieldName: 'firstName'] << [fieldName: 'birthPlace'] << [fieldName: 'citizenship']])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >>
                [new PersonDoc(
                        type: 'RF_PASSPORT',
                        series: '111',
                        number: '111-777',
                        issuedBy: 'ЗАГС г.Магадан',
                        issueDate: '30.10.2010',
                        issueId: '111222',
                        vrfStu: 'NONE'),
                 new PersonDoc(
                         type: 'RF_PASSPORT',
                         series: '222',
                         number: '222-777',
                         issuedBy: 'ЗАГС г.Хабаровск',
                         issueDate: '30.10.2010',
                         issueId: '111333',
                         vrfStu: 'VERIFIED')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().states[0].fields == []
        result.get().states[1].groupName == 'Паспорт гражданина РФ'
        result.get().states[1].fields.size() == 6
        def fields = result.get().states[1].fields
        fields[0].value == '222 222-777'
        fields[1].value == '30.10.2010'
        fields[2].value == 'ЗАГС г.Хабаровск'
        fields[3].value == '111-333'
        fields[4].value == 'Тамбов, ул Красного знамени, д 7Ч, кв. 26'
        fields[5].value == 'USA'
        def storedValues = result.get().storedValues
        storedValues.firstName == 'Tom'
        storedValues.birthDate == null
        storedValues.rfPasportSeries == '222'
        storedValues.rfPasportNumber == '222-777'
        storedValues.rfPasportIssueDate == '30.10.2010'
        storedValues.rfPasportIssuedBy == 'ЗАГС г.Хабаровск'
        storedValues.rfPasportIssuedById == '111333'
        storedValues.rfPasportIssuedByIdFormatted == '111-333'
        storedValues.birthPlace == 'Тамбов, ул Красного знамени, д 7Ч, кв. 26'
        storedValues.citizenship == 'USA'
        storedValues.citizenshipCode == 'us'
    }

    def 'Can get RF passport - exists only not verified passport'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: getRFPassportFields() << [fieldName: 'firstName']])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >>
                [new PersonDoc(
                        type: 'RF_PASSPORT',
                        series: '111',
                        number: '111-777',
                        issuedBy: 'ЗАГС г.Магадан',
                        issueDate: '30.10.2010',
                        issueId: '111222',
                        vrfStu: 'NONE')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        def fields = result.get().states[1].fields
        fields[0].value == '111 111-777'
        fields[1].value == '30.10.2010'
        fields[2].value == 'ЗАГС г.Магадан'
        fields[3].value == '111-222'
        def storedValues = result.get().storedValues
        storedValues.rfPasportSeries == '111'
        storedValues.rfPasportNumber == '111-777'
        storedValues.rfPasportIssueDate == '30.10.2010'
        storedValues.rfPasportIssuedBy == 'ЗАГС г.Магадан'
        storedValues.rfPasportIssuedById == '111222'
    }

    def 'If RF passport not exists - return only base data'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: getRFPassportFields() << [fieldName: 'firstName']])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >> []
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 1
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().storedValues.rfPasportSeries == null
    }

    def 'Can get foreign passport'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [
                fields: getForeignPassportFields() << [fieldName: 'firstName'] << [fieldName: 'birthPlace'] << [fieldName: 'citizenship']])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >>
                [new PersonDoc(
                        type: 'RF_PASSPORT',
                        series: '222',
                        number: '222-777',
                        issuedBy: 'ЗАГС г.Хабаровск',
                        issueDate: '30.10.2010',
                        issueId: '111333',
                        vrfStu: 'VERIFIED'),
                 new PersonDoc(
                         type: 'FID_DOC',
                         series: '111',
                         number: '111-777',
                         issuedBy: 'ЗАГС г.Магадан',
                         issueDate: '30.10.2010',
                         vrfStu: 'VERIFIED')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        def fields = result.get().states[1].fields
        fields.size() == 5
        fields[0].value == '111 111-777'
        fields[1].value == '30.10.2010'
        fields[2].value == 'ЗАГС г.Магадан'
        fields[3].value == 'Тамбов, ул Красного знамени, д 7Ч, кв. 26'
        fields[4].value == 'USA'
        def storedValues = result.get().storedValues
        storedValues.foreignPasportSeries == '111'
        storedValues.foreignPasportNumber == '111-777'
        storedValues.foreignPasportIssueDate == '30.10.2010'
        storedValues.foreignPasportIssuedBy == 'ЗАГС г.Магадан'
        storedValues.birthPlace == 'Тамбов, ул Красного знамени, д 7Ч, кв. 26'
        storedValues.citizenship == 'USA'
        storedValues.citizenshipCode == 'us'
        storedValues.rfPasportSeries == null
    }

    def 'If foreign passport not exists - return only base data'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: getForeignPassportFields() << [fieldName: 'firstName']])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >>
                [new PersonDoc(
                        type: 'FID_DOC',
                        series: '111',
                        number: '111-777',
                        issuedBy: 'ЗАГС г.Магадан',
                        issueDate: '30.10.2010',
                        issueId: '111222',
                        vrfStu: 'NONE')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 1
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().storedValues.rfPasportSeries == null
        result.get().storedValues.foreignPasportSeries == null
    }

    def 'Can get base date, without passports'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'firstName'], [fieldName: 'gender'], [fieldName: 'birthDate'], [fieldName: 'inn']]])

        when:
        mockUserPersonalDataPerson()
        mockUserPersonalDataDocs()
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 1
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().states[0].fields.size() == 3
        result.get().states[0].fields[0].value == '03.07.1962'
        result.get().states[0].fields[1].value == 'M'
        result.get().states[0].fields[2].label == 'ИНН'
        result.get().states[0].fields[2].value == 'inn_value'
        def storedValues = result.get().storedValues
        storedValues.firstName == 'Tom'
        storedValues.lastName == 'Cruise'
        storedValues.middleName == 'Tom'
        storedValues.gender == 'M'
        storedValues.birthDate == '03.07.1962'
        storedValues.citizenship == 'USA'
        storedValues.citizenshipCode == 'us'
        storedValues.inn == 'inn_value'
        storedValues.rfPasportSeries == null
        storedValues.foreignPasportSeries == null
    }

    def 'Can get snils'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'snils']]])

        when:
        mockUserPersonalDataPerson()
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 1
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().states[0].fields.size() == 1
        result.get().states[0].fields[0].value == '111555777'
    }

    def 'Can get user without RF passport'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: getRFPassportFields()])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >> []
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 1
        result.get().errors.size() == 1
        result.get().errors[0].type == 'error'
        result.get().errors[0].icon == 'red-line'
        result.get().errors[0].title == 'Нет паспортных данных'
        result.get().errors[0].desc == 'Вы не указали паспортные данные в профиле. Добавьте их, чтобы продолжить заполнять заявление'
    }

    def 'Can get user without foreign passport'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: getForeignPassportFields()])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >> []
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 1
        result.get().errors.size() == 1
        result.get().errors[0].type == 'error'
        result.get().errors[0].icon == 'red-line'
        result.get().errors[0].title == 'Нет паспортных данных'
        result.get().errors[0].desc == 'Вы не указали паспортные данные в профиле. Добавьте их, чтобы продолжить заполнять заявление'
    }

    def 'Can get user without required fields (RF passport + base fields + snils)'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: getRFPassportFields() << [fieldName: 'firstName']
                << [fieldName: 'birthPlace'] << [fieldName: 'birthDate'] << [fieldName: 'snils'] << [fieldName: 'inn']])

        when:
        userPersonalDataMock.person >> new Person(
                firstName: 'Tom',
                lastName: 'Cruise',
                middleName: 'Tom',
                gender: 'M',
                citizenship: 'USA',
                citizenshipCode: 'us')
        userPersonalDataMock.docs >> [new PersonDoc(
                type: 'RF_PASSPORT',
                vrfStu: 'VERIFIED')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().errors.size() == 1
        result.get().errors[0].type == 'error'
        result.get().errors[0].icon == 'red-line'
        result.get().errors[0].title == 'Добавьте данные, чтобы продолжить'
        result.get().errors[0].desc == 'Нажмите "Редактировать" и укажите в профиле:'
        result.get().errors[0].fields.size() == 9
        result.get().errors[0].fields.containsAll('дата рождения,', 'место рождения,', 'СНИЛС.', 'ИНН,', 'серия паспорта,',
                'номер паспорта,', 'дата выдачи паспорта,', 'кем выдан паспорт,', 'код подразделения,')
    }

    def 'Can get user without required foreign passport fields'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: getForeignPassportFields()])

        when:
        userPersonalDataMock.person >> new Person(
                firstName: 'Tom',
                lastName: 'Cruise',
                middleName: 'Tom',
                gender: 'M',
                citizenship: 'USA',
                citizenshipCode: 'us')
        userPersonalDataMock.docs >> [new PersonDoc(
                type: 'FID_DOC',
                vrfStu: 'VERIFIED')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().errors.size() == 1
        result.get().errors[0].type == 'error'
        result.get().errors[0].icon == 'red-line'
        result.get().errors[0].title == 'Добавьте данные, чтобы продолжить'
        result.get().errors[0].desc == 'Нажмите "Редактировать" и укажите в профиле:'
        result.get().errors[0].fields.size() == 3
        result.get().errors[0].fields.containsAll('номер паспорта,', 'дата выдачи паспорта,', 'кем выдан паспорт.')
    }

    def 'Can get user without one required field'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: getRFPassportFields() << [fieldName: 'firstName']
                << [fieldName: 'birthPlace'] << [fieldName: 'birthDate'] << [fieldName: 'snils']])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >>
                [new PersonDoc(
                        type: 'RF_PASSPORT',
                        number: '222-777',
                        issuedBy: 'ЗАГС г.Хабаровск',
                        issueDate: '30.10.2010',
                        issueId: '111333',
                        vrfStu: 'VERIFIED')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().errors.size() == 1
        result.get().errors[0].type == 'error'
        result.get().errors[0].icon == 'red-line'
        result.get().errors[0].title == 'Добавьте данные, чтобы продолжить'
        result.get().errors[0].desc == 'Нажмите "Редактировать" и укажите в профиле серию паспорта'
        result.get().errors[0].fields == null
    }

    def 'Can get warns'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'firstName'], [fieldName: 'gender'], [fieldName: 'birthDate']]])

        when:
        mockUserPersonalDataPerson()
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().errors.isEmpty()

        when:
        fieldComponent.attrs.put('warn', [[title: 'title 1', desc: 'desc 1'], [title: 'title 2', desc: 'desc 2']])
        result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().errors.size() == 2
        def error = result.get().errors.get(0)
        error.icon == 'yellow-line'
        error.type == 'warn'
        error.title == 'title 1'
        error.desc == 'desc 1'
        error.fields == null
    }

    def 'Can get user without Frgn passport check'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [checkFRGN: 'false', fields: getFrgnPassportFields()])

        when:
        mockUserPersonalDataPerson()

        userPersonalDataMock.docs >> [new PersonDoc(
                type: 'FRGN_PASS',
                series: '111',
                number: '111-777',
                vrfStu: 'VERIFIED')]

        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 1
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().states[0].fields.size() == 1
        result.get().states[0].fields[0].value == '03.07.1962'

        def storedValues = result.get().storedValues
        storedValues.firstName == 'Tom'
        storedValues.lastName == 'Cruise'
        storedValues.rfPasportSeries == null
        storedValues.frgnPasportSeries == null
        storedValues.frgnPasportNumber == null

        result.get().errors.size() == 1
        result.get().errors[0].type == 'error'
        result.get().errors[0].icon == 'red-line'
        result.get().errors[0].title == 'Нет паспортных данных'
        result.get().errors[0].desc == 'Вы не указали паспортные данные в профиле. Добавьте их, чтобы продолжить заполнять заявление'
    }

    def 'Can get user with Frgn passport and without RF passport'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [checkFRGN: 'true', fields: getFrgnPassportFields()])

        when:
        mockUserPersonalDataPerson()

        userPersonalDataMock.docs >> [new PersonDoc(
                type: 'FRGN_PASS',
                series: '111',
                number: '111-777',
                issuedBy: 'ЗАГС г.Магадан',
                issueDate: '30.10.2010',
                expiryDate: '09.09.9999',
                firstName: 'Jack',
                lastName: 'Nicholson',
                vrfStu: 'VERIFIED')]

        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().states[0].groupName == 'Cruise Tom Tom'

        result.get().states[0].fields.size() == 1
        result.get().states[0].fields[0].value == '03.07.1962'

        def storedValues = result.get().storedValues
        storedValues.firstName == 'Tom'
        storedValues.lastName == 'Cruise'
        storedValues.birthDate == '03.07.1962'
        storedValues.birthPlace == 'Тамбов, ул Красного знамени, д 7Ч, кв. 26'
        storedValues.citizenship == 'USA'
        storedValues.rfPasportSeries == null

        result.get().states[1].groupName == 'Заграничный паспорт гражданина РФ'
        storedValues.frgnPasportSeries == '111'
        storedValues.frgnPasportNumber == '111-777'
        storedValues.frgnPasportIssueDate == '30.10.2010'
        storedValues.frgnPasportIssuedBy == 'ЗАГС г.Магадан'
        storedValues.frgnPasportExpiryDate == '09.09.9999'
        storedValues.frgnPasportLastName == 'Nicholson'
        storedValues.frgnPasportFirstName == 'Jack'

        result.get().errors.size() == 0
    }

    def 'Can get user with expired Frgn passport'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [checkFRGN: 'true', fields: getFrgnPassportFields()])

        when:
        mockUserPersonalDataPerson()

        userPersonalDataMock.docs >> [new PersonDoc(
                type: 'FRGN_PASS',
                series: '111',
                number: '111-777',
                issuedBy: 'ЗАГС г.Магадан',
                issueDate: '30.10.2010',
                expiryDate: '05.03.2021',
                firstName: 'Jack',
                lastName: 'Nicholson',
                vrfStu: 'VERIFIED')]

        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().states[0].fields.size() == 1
        result.get().states[0].fields[0].value == '03.07.1962'

        def storedValues = result.get().storedValues
        storedValues.firstName == 'Tom'
        storedValues.lastName == 'Cruise'
        storedValues.rfPasportSeries == null

        result.get().states[1].groupName == 'Заграничный паспорт гражданина РФ'
        storedValues.frgnPasportSeries == '111'
        storedValues.frgnPasportNumber == '111-777'
        storedValues.frgnPasportIssueDate == '30.10.2010'
        storedValues.frgnPasportIssuedBy == 'ЗАГС г.Магадан'
        storedValues.frgnPasportExpiryDate == '05.03.2021'
        storedValues.frgnPasportLastName == 'Nicholson'
        storedValues.frgnPasportFirstName == 'Jack'

        result.get().errors.size() == 1
        result.get().errors[0].type == 'error'
        result.get().errors[0].icon == 'red-line'
        result.get().errors[0].title == 'Истёк срок действия загранпаспорта'
        result.get().errors[0].desc == 'Укажите в профиле актуальные данные по документу, удостоверяющему личность, чтобы продолжить заполнять заявление'
    }

    def 'Can get incorrect birth place'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: getRFPassportFields() << [fieldName: 'firstName'] << [fieldName: 'birthPlace']])

        when:
        ComponentResponse initialValue = ComponentResponse.of(new FormDto(storedValues: new ConfirmPersonalUserData(birthPlace: 'SGHGHJ R@#^&')))
        confirmPersonalUserDataComponent.preValidate(initialValue, fieldComponent, new ScenarioDto())
        then:

        initialValue.get().getErrors().size() == 1
        initialValue.get().getErrors().get(0).getTitle() == 'Проверьте место рождения'
        initialValue.get().getErrors().get(0).getDesc() == 'Поле может содержать только русские буквы, цифры и символы: «.», «,», «;», «:», «-», «\'», «"», «(», «)», «/», «№»'
    }

    def 'Can get incorrect birth place when validation from JSON'() {
        given:
        def fieldComponent = new FieldComponent(
                attrs: [fields: [[fieldName: 'birthPlace',attrs: ['validation': [[ 'type'     : 'RegExp', 'value'    : '[а-яА-ЯёЁ\\d\\s().",№:;\\-/\']{1,255}',
                 'errorMsg' : 'Проверьте место рождения',
                 'errorDesc': 'Поле может содержать только русские буквы, цифры и символы: «.», «,», «;», «:», «-», «\'», «"», «(», «)», «/», «№»'
                 ]]]
                 ]]])

        when:
        ComponentResponse initialValue = ComponentResponse.of(new FormDto(storedValues: new ConfirmPersonalUserData(birthPlace: 'SGHGHJ R@#^&')))
        confirmPersonalUserDataComponent.preValidate(initialValue, fieldComponent, new ScenarioDto())
        then:

        initialValue.get().getErrors().size() == 1
        initialValue.get().getErrors().get(0).getTitle() == 'Проверьте место рождения'
        initialValue.get().getErrors().get(0).getDesc() == 'Поле может содержать только русские буквы, цифры и символы: «.», «,», «;», «:», «-», «\'», «"», «(», «)», «/», «№»'
    }

    def 'Can get snils below RF passport data'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'rfPasportSeries'], [fieldName: 'snils']]])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >> [new PersonDoc(type: 'RF_PASSPORT', series: '222', number: '222-777')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().states[1].groupName == 'Паспорт гражданина РФ'
        result.get().states[1].fields.size() == 2
        result.get().states[1].fields[0].label == 'Серия и номер'
        result.get().states[1].fields[0].value == '222 222-777'
        result.get().states[1].fields[1].label == 'СНИЛС'
        result.get().states[1].fields[1].value == '111555777'
    }

    def 'Can get snils below FRGN passport data'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [checkFRGN: 'true', fields: [[fieldName: 'rfPasportSeries'], [fieldName: 'snils']]])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >> [new PersonDoc(type: 'FRGN_PASS', vrfStu: 'VERIFIED', expiryDate: '05.03.2021')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().states[1].groupName == 'Заграничный паспорт гражданина РФ'
        result.get().states[1].fields.size() == 4
        result.get().states[1].fields[3].label == 'СНИЛС'
        result.get().states[1].fields[3].value == '111555777'
    }

    def 'Can get snils below foreign passport data'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'foreignPasportSeries'], [fieldName: 'snils']]])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >> [new PersonDoc(type: 'FID_DOC', vrfStu: 'VERIFIED')]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().states.size() == 2
        result.get().states[0].groupName == 'Cruise Tom Tom'
        result.get().states[1].groupName == 'Документ, удостоверяющий личность'
        result.get().states[1].fields.size() == 1
        result.get().states[1].fields[0].label == 'СНИЛС'
        result.get().states[1].fields[0].value == '111555777'
    }

    def 'Can get medical policy (OMS)'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'omsNumber']]])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >> [new PersonDoc(type: docType, vrfStu: verified, series: series, number: number)]
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        (result.get()?.states?.get(0)?.fields?.isEmpty() ? null : result.get().states.get(0).fields.get(0).label) == label
        (result.get()?.states?.get(0)?.fields?.isEmpty() ? null : result.get().states.get(0).fields.get(0).value) == value
        result.get().storedValues.omsSeries == expectedSeries
        result.get().storedValues.omsNumber == expectedNumber

        where:
        docType     | verified       | series | number   | expectedSeries | expectedNumber | label                      | value
        'OTH'       | 'VERIFIED'     | '1111' | '111111' | null           | null           | null                       | null
        'MDCL_PLCY' | 'NOT_VERIFIED' | '2222' | '222222' | '2222'         | '222222'       | 'Серия и номер полиса ОМС' | '2222 222222'
        'MDCL_PLCY' | 'VERIFIED'     | '3333' | '333333' | '3333'         | '333333'       | 'Серия и номер полиса ОМС' | '3333 333333'
        'MDCL_PLCY' | 'VERIFIED'     | null   | '444444' | null           | '444444'       | 'Номер полиса ОМС'         | '444444'
    }

    def 'If required medical policy not present'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'omsNumber']]])

        when:
        mockUserPersonalDataPerson()
        userPersonalDataMock.docs >> []
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)

        then:
        result.get().errors.get(0).desc == 'Нажмите "Редактировать" и укажите в профиле полис ОМС'
    }

    def 'Can get RegExp validation errors'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'birthPlace', attrs: ['validation':[[
                    'type':'RegExp',
                    'value':'[а-яА-ЯёЁ\\d\\s().",№:;\\-/\']{1,255}',
                    'errorMsg':'Проверьте место рождения',
                    'errorDesc': 'Поле может содержать только русские буквы, цифры и символы: «.», «,», «;», «:», «-», «\'», «"», «(», «)», «/», «№»'
               ]]]
            ]]])
        ComponentResponse initialValue = ComponentResponse.of(new FormDto(storedValues: new ConfirmPersonalUserData(birthPlace: 'Тамбов R@#^&')))

        when:
        confirmPersonalUserDataComponent.preValidate(initialValue, fieldComponent, new ScenarioDto())

        then:
        initialValue.get().getErrors().size() == 1
        initialValue.get().getErrors().get(0).getTitle() == 'Проверьте место рождения'
        initialValue.get().getErrors().get(0).getDesc() == 'Поле может содержать только русские буквы, цифры и символы: «.», «,», «;», «:», «-», «\'», «"», «(», «)», «/», «№»'
    }

    def 'Can get RegExp validation'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'birthPlace', attrs: ['validation':[[
                     'type':'RegExp',
                     'value':'[а-яА-ЯёЁ\\d\\s().",№:;\\-/\']{1,255}',
                     'errorMsg':'Проверьте место рождения',
                     'errorDesc': 'Поле может содержать только русские буквы, цифры и символы: «.», «,», «;», «:», «-», «\'», «"», «(», «)», «/», «№»'
                 ]]]
             ]]])
        ComponentResponse initialValue = ComponentResponse.of(new FormDto(storedValues: new ConfirmPersonalUserData(birthPlace: 'Тамбов ')))

        when:
        confirmPersonalUserDataComponent.preValidate(initialValue, fieldComponent, new ScenarioDto())

        then:
        initialValue.get().getErrors().size() == 0
    }

    def 'Can get gender and fullGender'() {
        given:
        def fieldComponent = new FieldComponent(attrs: [fields: [[fieldName: 'gender'],[fieldName: 'genderFull']]])

        when:
        userPersonalDataMock.person >> new Person(
                gender: gender)
        def result = confirmPersonalUserDataComponent.getInitialValue(fieldComponent)
        def storedValues = result.get().storedValues

        then:
        result.get().states.size() == 1
        result.get().states[0].fields[0].value == expFildValueGender
        result.get().states[0].fields[1].value == expFildValueGenderFull
        storedValues.gender == expStoredValueGender
        storedValues.genderFull == expStoredValueGenderFull

        where:
        gender|expFildValueGender|expFildValueGenderFull|expStoredValueGender|expStoredValueGenderFull
        'M'   |'M'                |'Мужской'            |'M'                 | 'Мужской'
        'F'   |'F'                |'Женский'            |'F'                 | 'Женский'
    }

    def mockUserPersonalDataPerson() {
        userPersonalDataMock.person >> new Person(
                firstName: 'Tom',
                lastName: 'Cruise',
                middleName: 'Tom',
                birthDate: '03.07.1962',
                birthPlace: 'Тамбов, ул Красного знамени, д 7Ч, кв. 26',
                gender: 'M',
                snils: '111555777',
                inn: 'inn_value',
                citizenship: 'USA',
                citizenshipCode: 'us')
    }

    def mockUserPersonalDataDocs() {
        userPersonalDataMock.docs >>
                [new PersonDoc(
                        type: 'RF_PASSPORT',
                        series: '222',
                        number: '222-777',
                        issuedBy: 'ЗАГС г.Хабаровск',
                        issueDate: '30.10.2010',
                        issueId: '111333',
                        vrfStu: 'VERIFIED')]
    }

    static def getRFPassportFields() {
        [[fieldName: 'rfPasportSeries'],
         [fieldName: 'rfPasportNumber'],
         [fieldName: 'rfPasportIssueDate'],
         [fieldName: 'rfPasportIssuedBy'],
         [fieldName: 'rfPasportIssuedById']]
    }

    static def getForeignPassportFields() {
        [[fieldName: 'foreignPasportSeries'],
         [fieldName: 'foreignPasportNumber'],
         [fieldName: 'foreignPasportIssueDate'],
         [fieldName: 'foreignPasportIssuedBy'],
         [fieldName: 'foreignPasportIssuedById']]
    }

    static def getFrgnPassportFields() {
        [[fieldName: 'rfPasportSeries'],
         [fieldName: 'birthDate'],
         [fieldName: 'birthPlace'],
         [fieldName: 'citizenship'],
         [fieldName: 'frgnPasportSeries'],
         [fieldName: 'frgnPasportNumber'],
         [fieldName: 'frgnPasportIssueDate'],
         [fieldName: 'frgnPasportIssuedBy'],
         [fieldName: 'frgnPasportExpiryDate'],
         [fieldName: 'frgnPasportLastName'],
         [fieldName: 'frgnPasportFirstName']]
    }
}