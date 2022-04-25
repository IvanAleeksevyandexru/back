package unit.ru.gosuslugi.pgu.fs.service.impl

import net.minidev.json.JSONArray
import ru.atc.carcass.security.rest.model.EsiaAddress
import ru.atc.carcass.security.rest.model.EsiaContact
import ru.atc.carcass.security.rest.model.orgs.Org
import ru.atc.carcass.security.rest.model.orgs.OrgType
import ru.atc.carcass.security.rest.model.person.EsiaRole
import ru.atc.carcass.security.rest.model.person.Person
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import ru.gosuslugi.pgu.fs.service.impl.ProtectedFieldServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.UserDataServiceImpl
import spock.lang.Specification

class ProtectedFieldServiceImplSpec extends Specification {

    ProtectedFieldService service

    def setup() {
        UserPersonalData userPersonalDataMock = Mock(UserPersonalData)
        UserOrgData userOrgDataMock = Mock(UserOrgData)
        EmpowermentService empowermentServiceMock = Mock(EmpowermentService)
        userPersonalDataMock.person >> ([
                birthDate      : '01.01.2000',
                gender         : 'M',
                citizenshipCode: 'RUS',
                firstName      : 'ivan',
                middleName     : 'ivanovich',
                lastName       : 'petrov',
                assuranceLevel : 'AL20'
        ] as Person)
        userPersonalDataMock.contacts >> [
                [type: 'CPH', vrfStu: 'NOT_VERIFIED', value: '+7(834)6578678'] as EsiaContact,
                [type: 'MBT', vrfStu: 'NOT_VERIFIED', value: '+7(111)1111111'] as EsiaContact,
                [type: 'EML', vrfStu: 'VERIFIED', value: 'test@mail.com'] as EsiaContact
        ]
        userPersonalDataMock.docs >> [
                [type: 'RF_PASSPORT', vrfStu: 'VERIFIED', series: '1111', number: '111111', issueDate: '01.01.2020', issuedBy: 'УФМС'] as PersonDoc,
                [type: 'FRGN_PASS', vrfStu: 'VERIFIED', series: '2222', number: '222222', issueDate: '02.02.2020', issuedBy: 'УФМС'] as PersonDoc,
                [type: 'RF_DRIVING_LICENSE', vrfStu: 'VERIFIED', series: '3333', number: '333333', issueDate: '03.03.2020', issuedBy: 'УФМС', expiryDate: '03.03.2023'] as PersonDoc
        ]
        userPersonalDataMock.addresses >> [
                [type: 'PRG', addressStr: 'обл. Рязанская, р-н. Рязанский, д. Наумово, ул. Заречная, д. 1, кв. 12'] as EsiaAddress,
                [type: 'PLV', addressStr: 'обл. Самарская, г. Самара, снт. СДНТ Утес, линия. 1-я, д. 22, корп. 22, кв. 22'] as EsiaAddress
        ]

        userPersonalDataMock.currentRole >> ([chief: 'CHIEF'] as EsiaRole)
        userPersonalDataMock.getOrgId() >> 1
        userPersonalDataMock.userId >> 1
        userOrgDataMock.org >> ([type: OrgType.AGENCY] as Org)
        empowermentServiceMock.userEmpowerments >> ['PWR_1', 'PWR_2', 'PWR_3']

        def userDataServiceImpl = new UserDataServiceImpl(userPersonalDataMock, userOrgDataMock)

        service = new ProtectedFieldServiceImpl(userPersonalDataMock, userOrgDataMock, userDataServiceImpl, empowermentServiceMock)
        service.init()
    }

    def 'Can get value'() {
        when:
        def actualValue = service.getValue(name)

        then:
        actualValue == expectedValue

        where:
        name                       || expectedValue
        'citizenshipCode'          || 'RUS'
        'gender'                   || 'M'
        'birthDate'                || '2000-01-01'
        'orgType'                  || 'AGENCY'
        'userRole'                 || 'CHIEF'
        'notExists'                || null
        'firstName'                || 'ivan'
        'middleName'               || 'ivanovich'
        'lastName'                 || 'petrov'
        'powers'                   || ['PWR_1', 'PWR_2', 'PWR_3'] as JSONArray
        'assuranceLevel'           || 'AL20'
        'contactPhoneNumber'       || '+7(834)6578678'
        'mobilePhoneNumber'        || '+7(111)1111111'
        'email'                    || 'test@mail.com'
        'rfPassportSeries'         || '1111'
        'rfPassportNumber'         || '111111'
        'rfPassportIssueDate'      || '01.01.2020'
        'rfPassportIssuedBy'       || 'УФМС'
        'foreignPassportSeries'    || '2222'
        'foreignPassportNumber'    || '222222'
        'foreignPassportIssueDate' || '02.02.2020'
        'foreignPassportIssuedBy'  || 'УФМС'
        'livingAddress'            || 'обл. Самарская, г. Самара, снт. СДНТ Утес, линия. 1-я, д. 22, корп. 22, кв. 22'
        'registrationAddress'      || 'обл. Рязанская, р-н. Рязанский, д. Наумово, ул. Заречная, д. 1, кв. 12'
        'drivingLicenseSeries'     || '3333'
        'drivingLicenseNumber'     || '333333'
        'drivingLicenseIssueDate'  || '03.03.2020'
        'drivingLicenseExpireDate' || '03.03.2023'
        'drivingLicenseIssuedBy'   || 'УФМС'
    }
}
