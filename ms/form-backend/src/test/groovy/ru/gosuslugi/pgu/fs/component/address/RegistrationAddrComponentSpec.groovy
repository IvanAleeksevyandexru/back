package ru.gosuslugi.pgu.fs.component.address

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.service.LkNotifierService
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataError
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService
import spock.lang.Specification
import spock.mock.DetachedMockFactory

@ContextConfiguration(
        classes = [
                JsonProcessingServiceImpl,
                VariableRegistry,
                RegistrationAddrComponent
        ]
)
@Import(Configuration)
@TestPropertySource(properties = "data.cache.enabled=false")
class RegistrationAddrComponentSpec extends Specification {

    static String FULL_ADDRESS = "115533, г. Москва, пр-кт. Андропова, д. 5, кв. 25"
    static String APPLICANT_ANSWER
    static String FAILED_APPLICANT_ANSWER
    static FieldComponent COMPONENT
    static ScenarioDto SCENARIO_DTO = new ScenarioDto()

    @Autowired
    RegistrationAddrComponent registrationAddrComponent

    @Autowired
    NsiDadataService nsiDadataService

    Map.Entry<String, ApplicantAnswer> entry = Mock(Map.Entry.class) {
        getValue() >> Mock(ApplicantAnswer.class) {
            getValue() >> APPLICANT_ANSWER
            getKey() >> 'pd44'
        }
    }

    Map.Entry<String, ApplicantAnswer> failedEntry = Mock(Map.Entry.class) {
        getValue() >> Mock(ApplicantAnswer.class) {
            getValue() >> FAILED_APPLICANT_ANSWER
            getKey() >> 'pd44'
        }
    }

    def setupSpec() {
        APPLICANT_ANSWER = JsonFileUtil.getJsonFromFile(this.getClass(), "-answer.json")
        FAILED_APPLICANT_ANSWER = JsonFileUtil.getJsonFromFile(this.getClass(), "-failed-answer.json")
        COMPONENT = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component.json"), FieldComponent)
    }

    def "testGetFullAddress"() {
        when:
        FullAddress address = registrationAddrComponent.getFullAddress(entry)
        then:
        ComponentType.RegistrationAddr == registrationAddrComponent.getType()
        address != null
        FULL_ADDRESS == address.getFullAddress()
    }

    def "testValidationPassed"() {
        given:
        DadataAddressResponse address = Mock(DadataAddressResponse) {
            getError() >> Mock(DadataError) {
                getCode() >> errorCode
                getMessage() >> errorMessage
            }
            getPostalCode() >> postalCode // There is no validation for a digital record.
            getDadataQc() >> qc
            getDadataQcComplete() >> qcComplete
        }
        nsiDadataService.getAddress(FULL_ADDRESS) >> address

        when:
        Map<String, String> errors = registrationAddrComponent.validate(entry, SCENARIO_DTO, COMPONENT)
        then:
        errors != null
        errors.isEmpty()

        where:
        errorCode | errorMessage          | postalCode           | qc | qcComplete
        0         | "operation completed" | "My postal code 100" | 0  | 0
        0         | "operation completed" | "My postal code 100" | 3  | 8
        0         | "operation completed" | "My postal code 100" | 3  | 9
        0         | "operation completed" | "My postal code 100" | 0  | 0
    }

    def "testValidationFailed"() {
        given:
        DadataAddressResponse address = Mock(DadataAddressResponse) {
            getError() >> Mock(DadataError) {
                getCode() >> 0
                getMessage() >> "operation completed"
            }
            getPostalCode() >> "My postal code 100" // There is no validation for a digital record.
            getDadataQc() >> 0
            getDadataQcComplete() >> 0
        }
        nsiDadataService.getAddress(FULL_ADDRESS) >> address

        when:
        Map<String, String> errors = registrationAddrComponent.validate(failedEntry, SCENARIO_DTO, COMPONENT)
        then:
        errors != null
        !errors.isEmpty()
        errors.containsValue("Адрес не задан")
    }

    @TestConfiguration
    static class Configuration {
        def detachedMockFactory = new DetachedMockFactory()

        @Bean
        ObjectMapper getObjectMapper() {
            return new ObjectMapper()
        }

        @Bean
        ServiceIdVariable serviceIdVariable() {
            return detachedMockFactory.Mock(ServiceIdVariable)
        }

        @Bean
        NsiDadataService nsiDadataService() {
            return detachedMockFactory.Mock(NsiDadataService)
        }

        @Bean
        ComponentReferenceService componentReferenceService() {
            return detachedMockFactory.Mock(ComponentReferenceService)
        }

        @Bean
        LinkedValuesService linkedValuesService() {
            return detachedMockFactory.Mock(LinkedValuesService)
        }

        @Bean
        NsiDictionaryService nsiDictionaryService() {
            return detachedMockFactory.Mock(NsiDictionaryService)
        }

        @Bean
        LkNotifierService lkNotifierService() {
            return detachedMockFactory.Mock(LkNotifierService)
        }
    }
}
