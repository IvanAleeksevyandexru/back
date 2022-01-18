package ru.gosuslugi.pgu.fs.component.address

import ru.atc.carcass.security.rest.model.EsiaAddress
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ServiceInfoDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.service.LkNotifierService
import ru.gosuslugi.pgu.fs.utils.ContextJsonUtilSpec
import spock.lang.Specification

class AddressInputComponentSpec extends Specification {

    UserPersonalData userPersonalDataMock = Mock(UserPersonalData)

    def 'Can get initial address value'() {
        given:
            def addressInputComponent = new AddressInputComponent(userPersonalDataMock)
            def fieldComponent = new FieldComponent(attrs: [addrType: 'permanentRegistry'])
            userPersonalDataMock.addresses >> [new EsiaAddress(
                    type: 'PRG', zipCode: '141070', fiasCode: '11111', addressStr: '129515, г. Москва, ул. Академика Королева, д. 1')]
        when:
            def result = addressInputComponent.getInitialValue(fieldComponent)
        then:
            result.response.fullAddress == '129515, г. Москва, ул. Академика Королева, д. 1'
            result.response.fiasCode == '11111'
            result.response.postalCode == '141070'
    }

    def 'Check skip validation with or without addresses'() {
        given:
        AddressInputComponent addressInputComponent = Spy(new AddressInputComponent(userPersonalDataMock))
        ComponentTestUtil.setAbstractComponentServices(addressInputComponent)
        addressInputComponent.lkNotifierService = Mock(LkNotifierService)
        def fieldComponent = new FieldComponent(id: "ai1", type: ComponentType.AddressInput, attrs: [hidden: hidden, addrType: 'permanentRegistry'], required: required)
        userPersonalDataMock.addresses >> initial
        def entry = expectedEntry

        when:
        def response = addressInputComponent.getInitialValue(fieldComponent)
        def scenarioDto = ComponentTestUtil.mockScenario(new HashMap<String, ApplicantAnswer>(), new HashMap<String, ApplicantAnswer>(), new ServiceInfoDto())
        fieldComponent.setSkipValidation(true)
        def result = addressInputComponent.validate(entry, scenarioDto, fieldComponent)
        then:
        response != null
        response.get() == expectedResponse.get()
        result.isEmpty()

        where:
        initial                           || expectedResponse                                || expectedEntry                                    || hidden  || required  || skip    || count
        Collections.emptyList()           || ComponentResponse.<FullAddress>empty()          || Map.entry("ai1", new ApplicantAnswer(value: "")) || true    || false     || true    || 0
        List.of(getActualEsiaAddress())   || ComponentResponse.of(getExpectedFullAddress())  || Map.entry("ai1", getExpectedApplicantAnswer())   || false   || false     || false   || 1
    }

    private FullAddress getExpectedFullAddress() {
        FullAddress fullAddress = new FullAddress();
        fullAddress.setFullAddress("край. Приморский, г. Владивосток, ул. Прапорщика Комарова")
        return fullAddress;
    }

    private EsiaAddress getActualEsiaAddress() {
        return new EsiaAddress( type: 'PRG', addressStr: 'край. Приморский, г. Владивосток, ул. Прапорщика Комарова');
    }

    private ApplicantAnswer getExpectedApplicantAnswer() {
        ApplicantAnswer answerExpected = new ApplicantAnswer(value: JsonFileUtil.getJsonFromFile(ContextJsonUtilSpec.class, "-1-answer_value_expected.json"))
        return answerExpected
    }
}
