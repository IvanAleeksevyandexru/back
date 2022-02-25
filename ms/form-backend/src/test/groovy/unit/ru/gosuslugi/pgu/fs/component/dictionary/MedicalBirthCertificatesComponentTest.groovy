package unit.ru.gosuslugi.pgu.fs.component.dictionary


import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ServiceInfoDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.service.impl.ComponentReferenceServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.UserCookiesServiceImpl
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.dictionary.MedicalBirthCertificatesComponent
import ru.gosuslugi.pgu.fs.pgu.client.PguMedicalBirthCertificatesClient
import ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate.MedicalBirthCertificate
import ru.gosuslugi.pgu.fs.service.impl.DictionaryListPreprocessorServiceImpl
import spock.lang.Specification

class MedicalBirthCertificatesComponentTest extends Specification {

    static String componentId = 'mbcs1'

    @SuppressWarnings("GroovyAccessibility")
    def 'Main test'() {
        given:
        def fieldComponent = getFieldComponent()
        def scenarioDto = ComponentTestUtil.mockScenario(new HashMap<String, ApplicantAnswer>(), new HashMap<String, ApplicantAnswer>(), new ServiceInfoDto())
        def oid = 1077132806
        def userPersonalData = Stub(UserPersonalData) {
            it.getUserId() >> oid
        }

        def medicalBirthCertificatesClient = Stub(PguMedicalBirthCertificatesClient) {
            it.getMedicalBirthCertificates(_ as String, oid) >> getMedicalBirthCertificates()
        }

        def jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper());
        def componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, new UserCookiesServiceImpl(), ComponentTestUtil.getLinkedValuesService(jsonProcessingService))
        def dictionaryListPreprocessorService = new DictionaryListPreprocessorServiceImpl(JsonProcessingUtil.getObjectMapper(), jsonProcessingService, componentReferenceService)
        def medicalBirthCertificatesComponent = new MedicalBirthCertificatesComponent(medicalBirthCertificatesClient, userPersonalData, dictionaryListPreprocessorService)
        ComponentTestUtil.setAbstractComponentServices(medicalBirthCertificatesComponent)

        def incorrectAnswers = new HashMap<String, String>()
        fieldComponent.addArgument("dictionaryRefLabel", '${docInfo.series} ${docInfo.number}');
        fieldComponent.addArgument("dictionaryRefCode", '${docInfo.issueDate}');

        when:
        def result = medicalBirthCertificatesComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        result.get() == '''[{"oid":"1077132806","id":"4daf3d14-5f72-4e8b-9305-c5e105fe9d0f","version":"3290","createdOn":"1636733776225","updatedOn":"1642756129714","receiptDocDate":"1642756129714","relevance":"actual","status":"verified_by_validate","lastName":"сялямов","firstName":"людвиг","birthPlace":"осетия","gender":"F","birthDate":"16.07.1996","departmentDoc":"Министерство здравоохранения Российской Федерации","medicalOrg":"ГБУЗ \\"РД № 10 ДЗМ\\"","medicalOrgAddress":"117303, г. Москва, ул. Азовская, д. 22","type":"MDCL_BRTH_CERT","signatureLink":{"objectId":"2840742","objectTypeId":"41","mnemonic":"41АB-123456.sig"},"xmlLink":{"objectId":"2840742","objectTypeId":"40","mnemonic":"41АB-123456.xml"},"nbInfo":{"gender":"F","birthDate":"23.05.2018","birthTime":"08:23","weight":"3100","height":"55","genderCode":"2","birthAreaCode":"1"},"docInfo":{"series":"41АB","number":"123456","issueDate":"23.05.2018"},"labodeli":{"firstAppearance":"10","childbirthTook":"врач-акушер-гинеколог","childbirthPlace":"в стационаре","childbirthPlaceCode":"1","childbirthInfo":"многоплодные роды","childrenBorn":"2"},"mothInfo":{"fullName":"Богатырева Маргарита Васильевна","birthDate":"11.02.1980","registrationArea":"Город","registrationAreaCode":"1","familyStatus":"состоит в зарегистрированном браке","familyStatusCode":"1","education":"профессиональное: среднее","employment":"была занята в экономике: квалифицированные рабочие"}}]'''

        when:
        incorrectAnswers = [:] as Map
        medicalBirthCertificatesComponent.validateAfterSubmit(incorrectAnswers, componentId, JsonProcessingUtil.toJson(getSelectedCert()))

        then:
        incorrectAnswers.isEmpty()

        when:
        incorrectAnswers = [:] as Map
        medicalBirthCertificatesComponent.validateAfterSubmit(incorrectAnswers, componentId, JsonProcessingUtil.toJson(getFakeSelectedCert()))

        then:
        incorrectAnswers.containsKey(componentId)
    }

    static def getSelectedCert() {
        [
                id          : '23.05.2018',
                text        : '41АB 123456',
                unselectable: false,
                originalItem: [
                        label: '41АB 123456',
                        value: getMedicalBirthCertificate()
                ] as Map<String, Object>
        ] as LinkedHashMap
    }

    static def getFakeSelectedCert() {
        [
                id          : '23.05.2018',
                text        : '41АB 123456',
                unselectable: false,
                originalItem: [
                        label: '41АB 123456',
                        value: new MedicalBirthCertificate()
                ] as Map<String, Object>
        ] as LinkedHashMap
    }

    static def getFieldComponent() {
        [
                id          : componentId,
                type        : ComponentType.MedicalBirthCertificates,
                linkedValues: [
                        new LinkedValue(argument: 'dictionaryRefLabel', defaultValue: '${docInfo.series} ${docInfo.number}'),
                        new LinkedValue(argument: 'dictionaryRefCode', defaultValue: '${docInfo.issueDate}')
                ] as List<LinkedValue>,
        ] as FieldComponent
    }

    static def getApplicantAnswer() {
        [visited: true, value: JsonProcessingUtil.toJson(getMedicalBirthCertificate())] as ApplicantAnswer
    }

    static def getMedicalBirthCertificate() {
        JsonProcessingUtil.fromJson('''{
                        "oid": "1077132806",
                        "id": "4daf3d14-5f72-4e8b-9305-c5e105fe9d0f",
                        "version": 3290,
                        "createdOn": 1636733776225,
                        "updatedOn": 1642756129714,
                        "receiptDocDate": 1642756129714,
                        "relevance": "actual",
                        "status": "verified_by_validate",
                        "lastName": "сялямов",
                        "firstName": "людвиг",
                        "birthPlace": "осетия",
                        "gender": "F",
                        "birthDate": "16.07.1996",
                        "departmentDoc": "Министерство здравоохранения Российской Федерации",
                        "medicalOrg": "ГБУЗ \\"РД № 10 ДЗМ\\"",
                        "medicalOrgAddress": "117303, г. Москва, ул. Азовская, д. 22",
                        "docInfo": {
                            "series": "41АB",
                            "number": "123456",
                            "issueDate": "23.05.2018"
                        },
                        "nbInfo": {
                            "lastName": "Богатырева",
                            "birthDate": "23.05.2018",
                            "birthTime": "08:23",
                            "gender": "F",
                            "genderCode": 2,
                            "weight": "3100",
                            "height": "55",
                            "birthAreaCode": 1
                        },
                        "mothInfo": {
                            "fullName": "Богатырева Маргарита Васильевна",
                            "birthDate": "11.02.1980",
                            "registrationArea": "Город",
                            "registrationAreaCode": 1,
                            "familyStatus": "состоит в зарегистрированном браке",
                            "familyStatusCode": 1,
                            "education": "профессиональное: среднее",
                            "employment": "была занята в экономике: квалифицированные рабочие"
                        },
                        "labodeli": {
                            "firstAppearance": 10,
                            "childbirthTook": "врач-акушер-гинеколог",
                            "childbirthPlace": "в стационаре",
                            "childbirthPlaceCode": 1,
                            "childbirthInfo": "многоплодные роды",
                            "childrenBorn": 2
                        },
                        "xmlLink": {
                            "objectId": 2840742,
                            "objectTypeId": 40,
                            "mnemonic": "41АB-123456.xml"
                        },
                        "signatureLink": {
                            "objectId": 2840742,
                            "objectTypeId": 41,
                            "mnemonic": "41АB-123456.sig"
                        },
                        "type": "MDCL_BRTH_CERT"
                    }
                ]''', MedicalBirthCertificate.class)
    }

    static def getMedicalBirthCertificates() {
        [
                getMedicalBirthCertificate()
        ] as List
    }

}