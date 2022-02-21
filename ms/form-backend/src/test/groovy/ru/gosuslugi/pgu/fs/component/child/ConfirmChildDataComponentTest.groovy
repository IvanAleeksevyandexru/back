package ru.gosuslugi.pgu.fs.component.child

import ru.atc.carcass.security.rest.model.DocsCollection
import ru.atc.carcass.security.rest.model.person.Kids
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.core.date.util.DateUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import spock.lang.Specification

import java.time.OffsetDateTime

class ConfirmChildDataComponentTest extends Specification {

    private static final String FOREIGN_BRTH_CERT_TYPE = "FID_BRTH_CERT";
    private static final String RF_BRTH_CERT_TYPE = "RF_BRTH_CERT";
    private static final String BRTH_CERT_TYPE = "BRTH_CERT";

    def 'Confirm child data component test' () {
        given:
        ConfirmChildDataComponent component = new ConfirmChildDataComponent()
        ComponentTestUtil.setAbstractComponentServices(component)

        FieldComponent fieldComponent = new FieldComponent(type: ComponentType.ConfirmChildData, attrs: [fields:[
                ["fieldName": "firstName", "label": "Имя"],
                ["fieldName": "lastName", "label": "Фамилия"],
                ["fieldName": "birthDate", "label": "Дата рождения"],
                ["fieldName": "gender", "label": "Пол"],
                ["fieldName": "rfBirthCertificateSeries", "label": "Серия"],
                ["fieldName": "rfBirthCertificateNumber", "label": "Номер"],
                ["fieldName": "rfBirthCertificateIssueDate", "label": "Дата выдачи"],
        ]])
        CycledApplicantAnswerItem cycledApplicantAnswerItem = new CycledApplicantAnswerItem(
                esiaData: [
                        "firstName": "Ваня",
                        "lastName": "Ванин",
                        "birthDate": "2003-10-09T00:00:00Z",
                        "gender": "M",
                        "type": esiaDataType,
                        "rfBirthCertificateSeries": "XVII-ШЮ",
                        "rfBirthCertificateNumber": "222222",
                        "rfBirthCertificateIssueDate": "2003-10-10T00:00:00.000+03:00",
                ]
        )

        when:
        component.processCycledComponent(fieldComponent, cycledApplicantAnswerItem)

        then:
        assert component.getType() == ComponentType.ConfirmChildData
        assert fieldComponent.getValue() ==
                '{"states":[{"groupName":"Ванин Ваня","fields":[{"label":"Дата рождения","value":"09.10.2003"},' +
                '{"label":"Пол","value":"M"}]},{"groupName":"Свидетельство о рождении","fields":[{"label":"Серия и номер","value":"XVII-ШЮ 222222"},' +
                '{"label":"Дата выдачи","value":"10.10.2003"}]}],' +
                '"storedValues":{"firstName":"Ваня","lastName":"Ванин","birthDate":"09.10.2003","gender":"M","rfBirthCertificateSeries":"XVII-ШЮ",' +
                '"rfBirthCertificateNumber":"222222","rfBirthCertificateIssueDate":"10.10.2003","docType":"' + certificateType + '"},"errors":[]}'

        where:
        esiaDataType | certificateType
        BRTH_CERT_TYPE | BRTH_CERT_TYPE
        RF_BRTH_CERT_TYPE  | RF_BRTH_CERT_TYPE
    }

    def 'Confirm child data with update kid info from ESIA test' () {
        given:
        def personalData = Mock(UserPersonalData)
        personalData.docs >> [
                new PersonDoc(
                        id: 'person1',
                        type: 'VERIFIED_DOC',
                        series: '111',
                        number: '111-777',
                        issuedBy: 'ЗАГС г.Магадан',
                        issueDate: '30.10.2010',
                        vrfStu: 'VERIFIED'),
                new PersonDoc(
                        id: 'person2',
                        type: 'UNVERIFIED_DOC',
                        series: '222',
                        number: '222-777',
                        issuedBy: 'ЗАГС г.Магадан',
                        issueDate: '30.10.2010',
                        vrfStu: 'NONE'),
                new PersonDoc(
                        id: 'person3',
                        type: 'RF_PASSPORT',
                        series: '1234',
                        number: '123456',
                        issuedBy: 'ЗАГС г.Магадан',
                        issueId: '888999',
                        issueDate: '30.10.2010',
                        vrfStu: 'VERIFIED'
                )
        ]

        personalData.kids >> List.of(
                new Kids(id: '1',
                        firstName: 'Петя', lastName: 'Петечкин', birthDate: '10.06.2005',
                        gender: 'M',
                        documents: new DocsCollection(
                                elements: [new PersonDoc(
                                        type: esiaDataType,
                                        series: 'XO044',
                                        number: '555-777',
                                        issuedBy: 'ЗАГС г.Магадан',
                                        issueDate: '30.10.2010',
                                        vrfStu: 'NONE'
                                )])
                ))
        ConfirmChildDataComponent component = new ConfirmChildDataComponent(personalData)
        ComponentTestUtil.setAbstractComponentServices(component)

        FieldComponent fieldComponent = new FieldComponent(type: ComponentType.ConfirmChildData, attrs: [fields:[
                ["fieldName": "firstName", "label": "Имя"],
                ["fieldName": "lastName", "label": "Фамилия"],
                ["fieldName": "birthDate", "label": "Дата рождения"],
                ["fieldName": "gender", "label": "Пол"],
                ["fieldName": "rfBirthCertificateSeries", "label": "Серия"],
                ["fieldName": "rfBirthCertificateNumber", "label": "Номер"],
                ["fieldName": "rfBirthCertificateIssueDate", "label": "Дата выдачи"],
        ]])
        CycledApplicantAnswerItem cycledApplicantAnswerItem = new CycledApplicantAnswerItem(
                esiaData: [
                        id: '1',
                        'firstName': 'Ваня',
                        'lastName': 'Ванин',
                        'birthDate': '2003-10-09T00:00:00Z',
                        'gender': 'M',
                        'type': esiaDataType,
                        'rfBirthCertificateSeries': 'XVII-ШЮ',
                        'rfBirthCertificateNumber': '222222',
                        'rfBirthCertificateIssueDate': '2003-10-10T00:00:00.000+03:00',
                ]
        )

        def expectedUpdatedEsiaData = [
                'actDate': null,
                'birthDate': '2005-06-10T00:00:00.000Z',
                'firstName': 'Петя',
                'gender': 'M',
                'id': '1',
                'isNew': false,
                'lastName': 'Петечкин',
                'middleName': '',
                'regAddr': '',
                'regDate': '',
                'relationshipToChild': '',
                'rfBirthCertificateActNumber': '-',
                'rfBirthCertificateIssueDate': DateUtil.toOffsetDateTime('30.10.2010', DateUtil.ESIA_DATE_FORMAT) ,
                'rfBirthCertificateIssuedBy': 'ЗАГС г.Магадан',
                'rfBirthCertificateNumber': '555-777',
                'rfBirthCertificateSeries': 'XO044',
                'snils': null,
                'type': certificateType,
        ] as HashMap<String, Object>

        when:
        component.processCycledComponent(fieldComponent, cycledApplicantAnswerItem)

        then:
        assert component.getType() == ComponentType.ConfirmChildData
        assert fieldComponent.getValue() ==
                '{"states":[{"groupName":"Петечкин Петя","fields":[{"label":"Дата рождения","value":"10.06.2005"},' +
                '{"label":"Пол","value":"M"}]},{"groupName":"Свидетельство о рождении","fields":[{"label":"Серия и номер","value":"XO044 555-777"},' +
                '{"label":"Дата выдачи","value":"30.10.2010"}]}],' +
                '"storedValues":{"firstName":"Петя","lastName":"Петечкин","birthDate":"10.06.2005","gender":"M","rfBirthCertificateSeries":"XO044",' +
                '"rfBirthCertificateNumber":"555-777","rfBirthCertificateIssueDate":"30.10.2010","docType":"' + certificateType + '"},"errors":[]}'

        def actualEsiaData = cycledApplicantAnswerItem.getEsiaData()
        actualEsiaData.size() == expectedUpdatedEsiaData.size()
        actualEsiaData == expectedUpdatedEsiaData

        where:
        esiaDataType | certificateType
        BRTH_CERT_TYPE | BRTH_CERT_TYPE
        RF_BRTH_CERT_TYPE  | RF_BRTH_CERT_TYPE
    }

    def 'should empty birthDate required field' () {
        given:
        ConfirmChildDataComponent component = new ConfirmChildDataComponent()
        ComponentTestUtil.setAbstractComponentServices(component)

        FieldComponent fieldComponent = new FieldComponent(type: ComponentType.ConfirmChildData, attrs: [fields:[
                ["fieldName": "firstName", "label": "Имя"],
                ["fieldName": "lastName", "label": "Фамилия"],
                ["fieldName": "birthDate", "label": "Дата рождения", "required":true],
                ["fieldName": "gender", "label": "Пол", "required":true],
                ["fieldName": "rfBirthCertificateSeries", "label": "Серия", "required":true],
                ["fieldName": "rfBirthCertificateNumber", "label": "Номер", "required":true],
                ["fieldName": "rfBirthCertificateIssueDate", "label": "Дата выдачи", "required":true],
        ]])
        CycledApplicantAnswerItem cycledApplicantAnswerItem = new CycledApplicantAnswerItem(
                esiaData: [
                        "firstName": "Ваня",
                        "lastName": "Ванин",
                        "gender": "M",
                        "type": esiaDataType,
                        "rfBirthCertificateSeries": "XVII-ШЮ",
                        "rfBirthCertificateNumber": "222222",
                        "rfBirthCertificateIssueDate": "2003-10-10T00:00:00.000+03:00",
                ]
        )

        when:
        component.processCycledComponent(fieldComponent, cycledApplicantAnswerItem)

        then:
        assert component.getType() == ComponentType.ConfirmChildData
        assert fieldComponent.getValue() ==
                '{"states":[{"groupName":"Ванин Ваня","fields":[' +
                '{"label":"Пол","value":"M"}]},{"groupName":"Свидетельство о рождении","fields":[{"label":"Серия и номер","value":"XVII-ШЮ 222222"},' +
                '{"label":"Дата выдачи","value":"10.10.2003"}]}],' +
                '"storedValues":{"firstName":"Ваня","lastName":"Ванин","birthDate":"","gender":"M","rfBirthCertificateSeries":"XVII-ШЮ",' +
                '"rfBirthCertificateNumber":"222222","rfBirthCertificateIssueDate":"10.10.2003","docType":"' + certificateType + '"},"errors":[{"icon":"red-line","type":"error","title":"Добавьте данные, чтобы продолжить","desc":"Нажмите \\"Редактировать\\" и укажите в профиле Дату рождения"}]}'

        where:
        esiaDataType | certificateType
        BRTH_CERT_TYPE | BRTH_CERT_TYPE
        RF_BRTH_CERT_TYPE  | RF_BRTH_CERT_TYPE
    }

    def 'should error name no matcher template' () {
        given:
        ConfirmChildDataComponent component = new ConfirmChildDataComponent()
        ComponentTestUtil.setAbstractComponentServices(component)

        FieldComponent fieldComponent = new FieldComponent(type: ComponentType.ConfirmChildData, attrs: [fields:[
                ["fieldName": "firstName", "label": "Имя"],
                ["fieldName": "lastName", "label": "Фамилия"],
                ["fieldName": "birthDate", "label": "Дата рождения", "required":true],
                ["fieldName": "gender", "label": "Пол", "required":true],
                ["fieldName": "rfBirthCertificateSeries", "label": "Серия", "required":true],
                ["fieldName": "rfBirthCertificateNumber", "label": "Номер", "required":true],
                ["fieldName": "rfBirthCertificateIssueDate", "label": "Дата выдачи", "required":true],
        ]])
        CycledApplicantAnswerItem cycledApplicantAnswerItem = new CycledApplicantAnswerItem(
                esiaData: [
                        "firstName": "Anton123",
                        "lastName": "Ванин",
                        "gender": "M",
                        "type": esiaDataType,
                        "birthDate": "2003-10-10T00:00:00Z",
                        "rfBirthCertificateSeries": "XVII-ШЮ",
                        "rfBirthCertificateNumber": "222222",
                        "rfBirthCertificateIssueDate": "2003-10-10T00:00:00Z",
                ]
        )

        when:
        component.processCycledComponent(fieldComponent, cycledApplicantAnswerItem)

        then:
        assert component.getType() == ComponentType.ConfirmChildData
        assert fieldComponent.getValue() ==
                '{"states":[{"groupName":"Ванин Anton123","fields":[{"label":"Дата рождения","value":"10.10.2003"},' +
                '{"label":"Пол","value":"M"}]},{"groupName":"Свидетельство о рождении","fields":[{"label":"Серия и номер","value":"XVII-ШЮ 222222"},' +
                '{"label":"Дата выдачи","value":"10.10.2003"}]}],' +
                '"storedValues":{"firstName":"Anton123","lastName":"Ванин","birthDate":"10.10.2003","gender":"M","rfBirthCertificateSeries":"XVII-ШЮ",' +
                '"rfBirthCertificateNumber":"222222","rfBirthCertificateIssueDate":"10.10.2003","docType":"' + certificateType + '"},"errors":[{"icon":"red-line","type":"error","title":"Проверьте имя","desc":"Поле может содержать только русские буквы"}]}'

        where:
        esiaDataType | certificateType
        BRTH_CERT_TYPE | BRTH_CERT_TYPE
        RF_BRTH_CERT_TYPE  | RF_BRTH_CERT_TYPE
    }

    def 'when validation rules from json'() {
        given:
        ConfirmChildDataComponent component = new ConfirmChildDataComponent()
        ComponentTestUtil.setAbstractComponentServices(component)

        FieldComponent fieldComponent = new FieldComponent(type: ComponentType.ConfirmChildData, attrs: [
                actions: [[label: "Изменить",
                           value: "Изменить",
                           type: "profileEdit",
                           action: "editPassportData"]],
                fields: [
                        [fieldName: "firstName", label: "Имя", attrs: ["validation": [
                                [type: "RegExp", value: '^[0-9]{2}$', errorMsg: "FirstName errorMsg", errorDesc: "FirstName errorDesc"],
                        ]]],
                        [fieldName: "middleName", label: "Отчество", required: false, attrs: ["validation": [
                                [type: "RegExp", value: "[а-яА-ЯёЁ]{1,255}", errorMsg: "MiddleName errorMsg", errorDesc: "MiddleName errorDesc"],
                        ]]],
                        [fieldName: "lastName", label: "Фамилия", attrs: [validation: [
                                [type: "RegExp", value: ".+", errorMsg: "LastName errorMsg", errorDesc: "LastName errorDesc"],
                        ]]],
                        [fieldName: "snils", label: "СНИЛС", attrs: [validation: [
                                [type: "RegExp", value: "[а-яА-ЯёЁ]{5,10}", errorMsg: "Snils errorMsg", errorDesc: "Snils errorDesc"],
                        ]]],
                ]])

        CycledApplicantAnswerItem cycledApplicantAnswerItem = new CycledApplicantAnswerItem(
                esiaData: [
                        firstName: "00",
                        middleName: "Petrovich",
                        lastName: "Sidorov45623",
                        gender: "M",
                        type: FOREIGN_BRTH_CERT_TYPE,
                        snils: "000",
                ]
        )

        when:
        component.processCycledComponent(fieldComponent, cycledApplicantAnswerItem)

        then:
        assert !fieldComponent.getValue().contains('FirstName errorMsg')
        assert fieldComponent.getValue().contains('MiddleName errorMsg')
        assert !fieldComponent.getValue().contains('LastName errorMsg')
        assert fieldComponent.getValue().contains('Snils errorMsg')
    }

    def 'dates validation rules test'() {
        given:
        ConfirmChildDataComponent component = new ConfirmChildDataComponent()
        ComponentTestUtil.setAbstractComponentServices(component)

        FieldComponent fieldComponent = new FieldComponent(type: ComponentType.ConfirmChildData, attrs: [
                fields: [
                        ["fieldName": "firstName", "label": "Имя"],
                        [fieldName: "birthDate", label: "Дата рождения", attrs: [
                                validation: [ validation ],
                        ]],
                ]])

        CycledApplicantAnswerItem cycledApplicantAnswerItem = new CycledApplicantAnswerItem(
                esiaData: [
                        firstName: "00",
                        type: FOREIGN_BRTH_CERT_TYPE,
                        birthDate: value,
                ]
        )

        when:
        component.processCycledComponent(fieldComponent, cycledApplicantAnswerItem)

        then:
        contains == fieldComponent.getValue().contains(message)

        where:
        value                           | contains | message                                         | validation
        getCurrentDate()                | false    | 'День рождения должен быть сегодня'             | [type: 'equalsDate', value: 'today', ref: '', condition: '', errorMsg: message]
        '2000-01-01T00:00:00.000+03:00' | true     | 'День рождения должен быть сегодня'             | [type: 'equalsDate', value: 'today', ref: '', errorMsg: message]
        '2000-01-01T00:00:00.000+03:00' | true     | 'Возраст должен быть не более 10 лет'           | [type: 'minDate', value: 'today', ref: '', 'add': ['year': -10], errorMsg: message]
        '2021-01-01T00:00:00.000+03:00' | false    | 'Возраст должен быть не более 10 лет'           | [type: 'minDate', value: 'today', ref: '', 'add': ['year': -10], errorMsg: message]
        '2050-01-01T00:00:00.000+03:00' | true     | 'День рождения должен быть как минимум сегодня' | [type: 'maxDate', value: 'today', ref: '', errorMsg: message]
        getCurrentDate()                | false    | 'День рождения должен быть как минимум сегодня' | [type: 'maxDate', value: 'today', ref: '', errorMsg: message]
    }

    private static String getCurrentDate() {
        OffsetDateTime.now().toString()
    }

    def 'foreign certificate validation rules test'() {
        given:
        def component = new ConfirmChildDataComponent()
        ComponentTestUtil.setAbstractComponentServices(component)
        def seriesErrorMsg = 'Серия свидетельства не более 2 символов'
        def numberErrorMsg = 'Номер свидетельства может содержать только цифры'

        def fieldComponent = [type: ComponentType.ConfirmChildData, attrs: [
                fields: [
                        [fieldName: "rfBirthCertificateSeries", label: "Серия", required: true],
                        [fieldName: "rfBirthCertificateNumber", label: "Номер", required: true],
                        [fieldName: "rfBirthCertificateIssueDate", "label": "Дата выдачи", required: true],
                        [fieldName: "foreignBirthCertificateSeries", attrs: ["validation": [
                                [type: "RegExp", value: '^.{0,2}$', errorMsg: seriesErrorMsg],
                        ]]],
                        [fieldName: "foreignBirthCertificateNumber", attrs: ["validation": [
                                [type: "RegExp", value: '^\\d+$', errorMsg: numberErrorMsg],
                        ]]],
                ]]] as FieldComponent

        def cycledApplicantAnswerItem =
                [esiaData: [
                        type: certificateType,
                        rfBirthCertificateSeries: "BCX",
                        rfBirthCertificateNumber: "1234ABC",
                ]] as CycledApplicantAnswerItem

        when:
        component.processCycledComponent(fieldComponent, cycledApplicantAnswerItem)

        then:
        seriesValid == !fieldComponent.getValue().contains(seriesErrorMsg)
        numberValid == !fieldComponent.getValue().contains(numberErrorMsg)

        where:
        certificateType | seriesValid | numberValid
        FOREIGN_BRTH_CERT_TYPE | false       | false
        BRTH_CERT_TYPE | true       | true
        RF_BRTH_CERT_TYPE  | true        | true
    }

    def 'actDate validation'() {
        given:
        ConfirmChildDataComponent component = new ConfirmChildDataComponent()
        ComponentTestUtil.setAbstractComponentServices(component)

        FieldComponent fieldComponent = new FieldComponent(type: ComponentType.ConfirmChildData, attrs: [
                fields: [
                        [fieldName: "rfBirthCertificateSeries", label: "Серия", required: true],
                        [fieldName: "rfBirthCertificateNumber", label: "Номер", required: true],
                        ["fieldName": "actDate", "label": "Дата актовой записи"]
                ]])

        String date = "20.10.2010"
        Map<String, Object> externalData = new HashMap<>(
                firstName: "00",
                type: FOREIGN_BRTH_CERT_TYPE,
                actDate: date,
        )
        expect:
        assert date == component.getCycledInitialValue(fieldComponent, externalData).get().getStates().get(1).getFields().get(0).getValue()
    }
}