package ru.gosuslugi.pgu.fs.component.input

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.gender.LookupGenderComponent
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponse
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponseAttribute
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponseItem
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService
import ru.gosuslugi.pgu.fs.service.MedicineService
import ru.gosuslugi.pgu.fs.service.RegionInfoService
import spock.lang.Specification

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.GLookup

class LookupGenderComponentTest extends Specification {

    ScenarioDto scenarioDto
    FieldComponent fieldComponent
    Map.Entry<String, ApplicantAnswer> entry
    Map<String, String> incorrectAnswers
    JsonProcessingService jsonProcessingService
    DictionaryFilterService dictionaryFilterService
    RegionInfoService regionInfoService
    MedicineService medicineService
    LookupGenderComponent lookupGenderComponent

    def setup() {
        dictionaryFilterService = Stub(DictionaryFilterService)
        regionInfoService = Stub(RegionInfoService)
        medicineService = Stub(MedicineService)
        jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
        lookupGenderComponent = new LookupGenderComponent(dictionaryFilterService, regionInfoService, medicineService)
        fieldComponent = new FieldComponent(id: '1', type: GLookup, attrs: [dictionaryType: ['man', 'woman']])
        scenarioDto = new ScenarioDto()
        entry = AnswerUtil.createAnswerEntry("value", "{\"originalItem\": {\"attributeValues\": {}}}")
    }

    def "Test getType returns correct value"() {
        expect:
        lookupGenderComponent.getType() == GLookup
    }

    def "Test getInitialValue returns non null value"() {
        given:
        FieldComponent fieldComponent = new FieldComponent(type: GLookup)

        expect:
        lookupGenderComponent.getInitialValue(fieldComponent) != null
        lookupGenderComponent.getInitialValue(fieldComponent, new ScenarioDto()) != null
    }

    def "Test getInitialValue returns empty response if dictionaryFilter attr not exists"() {
        given:
        FieldComponent fieldComponent = new FieldComponent(type: GLookup)
        dictionaryFilterService.getInitialValue(_, _) >> { [] as HashMap<String, Object> }
        ComponentTestUtil.setAbstractComponentServices(lookupGenderComponent)

        expect:
        lookupGenderComponent.getInitialValue(fieldComponent, new ScenarioDto()) != null
        lookupGenderComponent.getInitialValue(fieldComponent, new ScenarioDto()) == ComponentResponse.empty()
    }

    def "Test getInitialValue returns correct preset values"() {
        given:
        def preset = [key: 'value'] as HashMap<String, Object>
        dictionaryFilterService.getInitialValue(_, _) >> preset
        ComponentTestUtil.setAbstractComponentServices(lookupGenderComponent)

        expect:
        lookupGenderComponent.getInitialValue(fieldComponent, scenarioDto) != null
        lookupGenderComponent.getInitialValue(fieldComponent, scenarioDto) ==
                ComponentResponse.of(jsonProcessingService.toJson(preset))
    }

    def "Test validateAfterSubmit call DictionaryFilterService validation"() {
        given:
        def filterService = Mock(DictionaryFilterService)
        def lookupGComponent = new LookupGenderComponent(filterService, regionInfoService, medicineService)

        when:
        lookupGComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        1 * filterService.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent, _)

        when:
        lookupGComponent.validateAfterSubmit(incorrectAnswers, AnswerUtil.createAnswerEntry("key", null), scenarioDto, fieldComponent)

        then:
        0 * filterService.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent, _)
    }

    def "Test validateAfterSubmit set entry value if medicalInfo exists"() {
        given:
        medicineService.getSmevResponse(_, _, _) >> createMedicalDictionaryResponse()
        ComponentTestUtil.setAbstractComponentServices(lookupGenderComponent)

        when:
        regionInfoService.getIntegerSmevVersion(_, _) >> smevVersion
        lookupGenderComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, medicalFieldComponent)
        Map<String, Object> medicalInfo = ((Map<String, Object>) jsonProcessingService
                .fromJson(entry.getValue().getValue(), LinkedHashMap.class)).get("medicalInfo")

        then:
        entry != null
        medicalInfo != null
        medicalInfo.get("smevVersion") == smevVersion
        medicalInfo.get("sessionId") != null
        medicalInfo.get("eserviceId") == eserviceId
        medicalInfo.get("medicalData") != null

        where:
        smevVersion || medicalFieldComponent                   || eserviceId
        2           || createFieldComponentWithMedicalInfoV2() || "10000000002"
        3           || createFieldComponentWithMedicalInfoV3() || "10000000003"
    }

    def "Test component process use different dictionaries depending on gender"() {
        given:
        Map<String, Object> attrs = lookupGenderComponent.processAttrs(gender, fieldComponent.getAttrs())

        expect:
        attrs.get(DICTIONARY_NAME_ATTR) == dictionaryType

        where:
        gender || dictionaryType
        "M"    || "man"
        "W"    || "woman"
    }

    def static createMedicalDictionaryResponse() {
        new MedDictionaryResponse(error: [errorDetail: [errorCode: 0, errorMessage: '']],
                items: [
                        new MedDictionaryResponseItem(attributes: [
                                new MedDictionaryResponseAttribute(name: 'id', value: '1'),
                                new MedDictionaryResponseAttribute(name: 'name', value: 'Фёдоров Фёдор')]
                        )])
    }

    def static createFieldComponentWithMedicalInfoV2() {
        new FieldComponent(
                id: '1',
                type: 'GLookup',
                linkedValues: [],
                attrs: [externalIntegration: 'medicalInfo', ESERVICEID_V2: '10000000002', smevVersion: '2']
        )
    }

    def static createFieldComponentWithMedicalInfoV3() {
        new FieldComponent(
                id: '1',
                type: 'GLookup',
                linkedValues: [],
                attrs: [externalIntegration: 'medicalInfo', ESERVICEID_V3: '10000000003', smevVersion: '3']
        )
    }
}
