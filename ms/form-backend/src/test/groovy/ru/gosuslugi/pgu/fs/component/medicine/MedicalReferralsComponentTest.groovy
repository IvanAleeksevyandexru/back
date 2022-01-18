package ru.gosuslugi.pgu.fs.component.medicine

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import spock.lang.Specification

class MedicalReferralsComponentTest extends Specification {

    private final static String MEDICAL_INFO_KEY = "medicalInfo"
    private final static String BUTTON_LABEL = "buttonLabel"
    private static MedicalReferralsComponent helper

    def setupSpec() {
        helper = new MedicalReferralsComponent()
    }

    def 'component type is MedicalReferrals'() {
        expect:
        helper.getType() == ComponentType.MedicalReferrals
    }

    def 'getInitialValue method test'() {
        given:
        def component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component.json"), FieldComponent)
        String initialValueString = helper.getInitialValue(component).get()

        when:
        Map<String, String> valueMap = JsonProcessingUtil.fromJson(initialValueString, Map)

        then:
        valueMap.containsKey(MEDICAL_INFO_KEY) && valueMap.get(MEDICAL_INFO_KEY) == component.getArgument(MEDICAL_INFO_KEY)
        valueMap.containsKey(BUTTON_LABEL) && valueMap.get(BUTTON_LABEL) == component.getArgument(BUTTON_LABEL)
        valueMap.size() == 2
    }
}
