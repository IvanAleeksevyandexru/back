package ru.gosuslugi.pgu.fs.component.confirm

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.PersonWithAge
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.common.esia.search.exception.MultiplePersonFoundException
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService
import ru.gosuslugi.pgu.components.BasicComponentUtil
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException
import ru.gosuslugi.pgu.fs.component.confirm.internal.PassportDoc
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import spock.lang.Specification

class ConfirmAnotherUserDataComponentTest extends Specification {
    private static final EXTERNAL_DATA_SUFFIX = "-externalData.json"

    PersonSearchService personSearch
    ErrorModalDescriptorService errorModalDescriptorService
    ConfirmAnotherUserDataComponent confirmAnotherUserDataComponent

    void setup() {
        personSearch = Mock(PersonSearchService)
        errorModalDescriptorService = Mock(ErrorModalDescriptorService)
        confirmAnotherUserDataComponent = new ConfirmAnotherUserDataComponent(personSearch, errorModalDescriptorService)
    }

    def "Test exception on multiple person found"() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.ConfirmAnotherUserData)
        def externalData = readExternalDataFromFile(EXTERNAL_DATA_SUFFIX)

        when:
        personSearch.searchOneTrusted(_ as String, _ as String) >> { throw new MultiplePersonFoundException() }
        confirmAnotherUserDataComponent.getCycledInitialValue(fieldComponent, externalData)

        then:
        thrown(ErrorModalException)
    }

    def "Test empty initial value"() {
        given:
        def fieldComponent = new FieldComponent(type: ComponentType.ConfirmAnotherUserData)

        when:
        def initialValue = confirmAnotherUserDataComponent.getCycledInitialValue(fieldComponent, new HashMap<String, Object>())

        then:
        initialValue == ComponentResponse.empty()
    }

    def "Test correct another user data"() {
        given:
        FieldComponent fieldComponent = new FieldComponent(type: ComponentType.ConfirmAnotherUserData)
        fieldComponent.attrs = new HashMap<>()
        Map externalData = readExternalDataFromFile(EXTERNAL_DATA_SUFFIX)
        PassportDoc passport = new PassportDoc(externalData)
        UserPersonalData anotherUserData = new UserPersonalData()
        anotherUserData.setPerson(new PersonWithAge())
        anotherUserData.setDocs(List.of(passport))
        ComponentResponse componentResponse = ComponentResponse.of(
                ConfirmPersonalUserDataComponent.prepareForm(anotherUserData, BasicComponentUtil.getPreSetFields(fieldComponent))
        )

        when:
        personSearch.searchOneTrusted(_ as String, _ as String) >> new PersonWithAge()
        ComponentResponse initialValue = confirmAnotherUserDataComponent.getCycledInitialValue(fieldComponent, externalData)

        then:
        initialValue == componentResponse

    }

    def readExternalDataFromFile(suffix) {
        return JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), suffix),
                Map
        )
    }
}
