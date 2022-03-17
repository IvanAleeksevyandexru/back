package ru.gosuslugi.pgu.fs.service.impl

import ru.atc.carcass.security.rest.model.orgs.Org
import ru.atc.carcass.security.rest.model.orgs.OrgType
import ru.atc.carcass.security.rest.model.person.EsiaRole
import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.components.ComponentAttributes
import ru.gosuslugi.pgu.dto.ApplicantDto
import ru.gosuslugi.pgu.dto.ApplicantRole
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.common.variable.TargetIdVariable
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import spock.lang.Specification

class AdditionalAttributesHelperSpec extends Specification {
    def "UpdateAdditionalAttributes for Applicant"() {
        Person person = new Person(
                userId: "1000234",
                snils: "194 429 977 25",
                inn: "270901963751",
                firstName: "Иван",
                lastName: "Иванов",
                middleName: "Иванович",
                birthDate: "10.10.2000",
                gender: "M",
                citizenship: "Россия",
                citizenshipCode: "RUS",
                birthCountryCode: "RUS"
        )
        String serviceId = "106"
        String targetId = "-106"
        String userTimeZone = "+3"
        String systemAuthority = "authority"

        AdditionalAttributesHelper attributesHelper = createAttributeHelper(
                person,
                serviceId,
                targetId,
                userTimeZone,
                systemAuthority
        )

        ScenarioDto scenarioDto = new ScenarioDto(
                display: new DisplayRequest(isTerminal: true),
                orderId: 1L
        )

        when:
        attributesHelper.updateAdditionalAttributes(scenarioDto, null)

        then:
        // атрибуты текущего заявителя
        scenarioDto.getAdditionalParameters().keySet().stream().filter({ key -> !key.startsWith("master") }).count() == 22
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.ORDER_ATTR_NAME) == "1"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.OID_ATTR_NAME) == person.getUserId()
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.SERVICE_ID_ATTR_NAME) == serviceId
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.TARGET_ID_ATTR_NAME) == targetId
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.SNILS) == person.getSnils()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.ORG_INN_ATTR) == person.getInn()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.FIRST_NAME_ATTR) == person.getFirstName()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.LAST_NAME_ATTR) == person.getLastName()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.MIDDLE_NAME_ATTR) == person.getMiddleName()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.BIRTH_DATE_ATTR) == person.getBirthDate()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.GENDER_ATTR) == person.getGender()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.CITIZENSHIP_ATTR) == person.getCitizenship()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.CITIZENSHIP_CODE_ATTR) == person.getCitizenshipCode()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.BIRTHDATE_CODE_ATTR) == person.getBirthCountryCode()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.TIMEZONE_ATTR) == userTimeZone
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.SYSTEM_AUTHORITY_ATTR_NAME) == systemAuthority
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.USER_ORG_CHIEF_ATTR) == "chief"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.ORG_TYPE_ATTR) == "LEGAL"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.LEG_ATTR) == "leg"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.LEG_CODE_ATTR) == "legCode"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.ORG_EMPOWERMENTS_ATTR_NAME) == "1000234"

        // атрибуты основного заявителя
        scenarioDto.getAdditionalParameters().keySet().stream().filter({ key -> key.startsWith("master") }).count() == 18
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.MASTER_ORDER_ID_ATTR_NAME) == "1"
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(AdditionalAttributesHelper.OID_ATTR_NAME)) == person.getUserId()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.SNILS)) == person.getSnils()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.FIRST_NAME_ATTR)) == person.getFirstName()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.LAST_NAME_ATTR)) == person.getLastName()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.MIDDLE_NAME_ATTR)) == person.getMiddleName()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.BIRTH_DATE_ATTR)) == person.getBirthDate()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.GENDER_ATTR)) == person.getGender()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.CITIZENSHIP_ATTR)) == person.getCitizenship()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.CITIZENSHIP_CODE_ATTR)) == person.getCitizenshipCode()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.BIRTHDATE_CODE_ATTR)) == person.getBirthCountryCode()
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(ComponentAttributes.TIMEZONE_ATTR)) == userTimeZone
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(AdditionalAttributesHelper.SYSTEM_AUTHORITY_ATTR_NAME)) == systemAuthority
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(AdditionalAttributesHelper.USER_ORG_CHIEF_ATTR)) == "chief"
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(AdditionalAttributesHelper.LEG_ATTR)) == "leg"
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(AdditionalAttributesHelper.LEG_CODE_ATTR)) == "legCode"
        scenarioDto.getAdditionalParameters().get(attributesHelper.getMasterAttributeName(AdditionalAttributesHelper.ORG_TYPE_ATTR)) == "LEGAL"
    }

    def "UpdateAdditionalAttributes for none-Applicant"() {
        Person person = new Person(
                userId: "1000234",
                snils: "194 429 977 25",
                inn: "270901963751",
                firstName: "Иван",
                lastName: "Иванов",
                middleName: "Иванович",
                birthDate: "10.10.2000",
                gender: "M",
                citizenship: "Россия",
                citizenshipCode: "RUS",
                birthCountryCode: "RUS"
        )
        String serviceId = "106"
        String targetId = "-106"
        String userTimeZone = "+3"
        String systemAuthority = "authority"

        AdditionalAttributesHelper attributesHelper = createAttributeHelper(person, serviceId, targetId, userTimeZone, systemAuthority)

        ScenarioDto scenarioDto = new ScenarioDto(
                display: new DisplayRequest(isTerminal: true),
                orderId: 1L,
                participants: [
                        "1000234" : new ApplicantDto(role: ApplicantRole.Coapplicant)
                ]
        )

        when:
        attributesHelper.updateAdditionalAttributes(scenarioDto, null)

        then:
        // атрибуты текущего заявителя
        scenarioDto.getAdditionalParameters().keySet().stream().filter({ key -> !key.startsWith("master") }).count() == 22
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.ORDER_ATTR_NAME) == "1"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.OID_ATTR_NAME) == person.getUserId()
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.SERVICE_ID_ATTR_NAME) == serviceId
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.TARGET_ID_ATTR_NAME) == targetId
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.SNILS) == person.getSnils()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.ORG_INN_ATTR) == person.getInn()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.FIRST_NAME_ATTR) == person.getFirstName()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.LAST_NAME_ATTR) == person.getLastName()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.MIDDLE_NAME_ATTR) == person.getMiddleName()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.BIRTH_DATE_ATTR) == person.getBirthDate()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.GENDER_ATTR) == person.getGender()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.CITIZENSHIP_ATTR) == person.getCitizenship()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.CITIZENSHIP_CODE_ATTR) == person.getCitizenshipCode()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.BIRTHDATE_CODE_ATTR) == person.getBirthCountryCode()
        scenarioDto.getAdditionalParameters().get(ComponentAttributes.TIMEZONE_ATTR) == userTimeZone
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.SYSTEM_AUTHORITY_ATTR_NAME) == systemAuthority
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.USER_ORG_CHIEF_ATTR) == "chief"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.ORG_TYPE_ATTR) == "LEGAL"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.LEG_ATTR) == "leg"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.LEG_CODE_ATTR) == "legCode"
        scenarioDto.getAdditionalParameters().get(AdditionalAttributesHelper.ORG_EMPOWERMENTS_ATTR_NAME) == "1000234"

        // атрибуты основного заявителя
        scenarioDto.getAdditionalParameters().keySet().stream().filter({ key -> key.startsWith("master") }).count() == 0
    }

    private AdditionalAttributesHelper createAttributeHelper(Person person, String serviceId, String targetId, String userTimeZone, String systemAuthority) {
        UserPersonalData userPersonalData = Mock(UserPersonalData)
        userPersonalData.getPerson() >> person
        userPersonalData.getUserId() >> Long.parseLong(person.getUserId())
        userPersonalData.getContacts() >> []
        userPersonalData.getCurrentRole() >> new EsiaRole(
                chief: "chief"
        )

        ServiceIdVariable serviceIdVariable = Mock(ServiceIdVariable)
        serviceIdVariable.getValue(_) >> serviceId

        TargetIdVariable targetIdVariable = Mock(TargetIdVariable)
        targetIdVariable.getValue(_) >> targetId

        UserCookiesService userCookiesService = Mock(UserCookiesService)
        userCookiesService.getUserTimezone() >> userTimeZone

        UserOrgData userOrgData = Mock(UserOrgData)
        userOrgData.getSystemAuthority() >> systemAuthority
        userOrgData.getOrg() >> new Org(
                type: OrgType.LEGAL,
                leg: "leg",
                legCode: "legCode"
        )
        userOrgData.getOrgRole() >> new EsiaRole(
                chief: "chief"
        )
        userOrgData.getChief() >> "chief"

        def empowermentService = Mock(EmpowermentService)
        empowermentService.getUserEmpowerments()>>Set.of("1000234")

        AdditionalAttributesHelper attributesHelper = new AdditionalAttributesHelper(
                userPersonalData,
                userOrgData,
                userCookiesService,
                serviceIdVariable,
                targetIdVariable,
                empowermentService
        )
        attributesHelper
    }

    def "getMasterAttributeName"() {
        AdditionalAttributesHelper attributesHelper = new AdditionalAttributesHelper(null, null, null, null, null, null)

        when:
        def actualResult = attributesHelper.getMasterAttributeName(attributeName)

        then:
        actualResult == expectedResult

        where:
        attributeName | expectedResult
        "test"        | "masterTest"
        "TEST"        | "masterTEST"
        "tEST"        | "masterTEST"
    }
}
