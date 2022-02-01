package ru.gosuslugi.pgu.fs.component.payment

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import spock.lang.Specification

import static ru.gosuslugi.pgu.components.ComponentAttributes.BILL_ID_ATTR
import static ru.gosuslugi.pgu.components.ComponentAttributes.BILL_NUMBER_ATTR

class InvoiceScrComponentTest extends Specification {

    private static final String COMPONENT_SUFFIX = "-component.json"

    InvoiceScrComponent component = new InvoiceScrComponent()

    def "Processed properly"() {
        given:
        def fieldComponent = JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), COMPONENT_SUFFIX),
                FieldComponent
        )

        when:
        component.preProcess(fieldComponent)

        then:
        fieldComponent.getAttrs().containsKey(BILL_ID_ATTR)
        fieldComponent.getAttrs().containsKey(BILL_NUMBER_ATTR)
    }
}
