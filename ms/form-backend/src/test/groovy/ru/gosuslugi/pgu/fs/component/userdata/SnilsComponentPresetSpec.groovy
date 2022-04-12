package ru.gosuslugi.pgu.fs.component.userdata

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.draft.model.DraftHolderDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import ru.gosuslugi.pgu.fs.service.impl.EmpowermentServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.InitialValueFromImpl
import ru.gosuslugi.pgu.fs.service.impl.ProtectedFieldServiceImpl
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import spock.lang.Specification
import spock.mock.DetachedMockFactory

@ContextConfiguration(
        classes = [
                JsonProcessingServiceImpl,
                VariableRegistry,
                UserPersonalData,
                UserOrgData,
                ProtectedFieldServiceImpl,
                ParseAttrValuesHelper,
                CalculatedAttributesHelper,
                InitialValueFromImpl
        ]
)
@Import(Configuration)
@TestPropertySource(properties = "data.cache.enabled=false")
class SnilsComponentPresetSpec extends Specification {

    @MockBean
    ComponentReferenceService componentReferenceService

    @Autowired
    InitialValueFromService initialValueFromService

    SnilsComponent snilsComponent

    def setup() {
        snilsComponent = new SnilsComponent(null, null, null, null, initialValueFromService, null)
    }

    def "test initial value"() {
        given:
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), componentFileSuffix), FieldComponent)

        when:
        ComponentResponse<String> initialValue = snilsComponent.getInitialValue(
                component,
                JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), '-draft.json'), DraftHolderDto).getBody(),
                JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), '-serviceDescriptor.json'), ServiceDescriptor)
        )
        then:
        initialValue.get() != null
        expectedValue == initialValue.get()

        where:
        componentFileSuffix | expectedValue
        // test reference
        '-ref.json'         | '057-551-458 77'
        // test calcs
        '-calc-yes.json'    | '196247, г. Санкт-Петербург, пр-кт. Ленинский, д. 147, к. 5, кв. 55'
        '-calc-no.json'     | 'Профессиональное среднее'
    }

    @TestConfiguration
    static class Configuration {
        def detachedMockFactory = new DetachedMockFactory();

        @Bean
        ObjectMapper getObjectMapper() {
            return new ObjectMapper()
        }

        @Bean
        ServiceIdVariable serviceIdVariable() {
            return detachedMockFactory.Mock(ServiceIdVariable)
        }

        @Bean
        ProtectedFieldService protectedFieldService() {
            return detachedMockFactory.Mock(ProtectedFieldService)
        }

        @Bean
        UserPersonalData userPersonalData() {
            return detachedMockFactory.Mock(UserPersonalData)
        }

        @Bean
        UserOrgData userOrgData() {
            return detachedMockFactory.Mock(UserOrgData)
        }

        @Bean
        EmpowermentService empowermentService() {
            return detachedMockFactory.Mock(EmpowermentServiceImpl)
        }
    }
}
