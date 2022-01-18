package ru.gosuslugi.pgu.fs.common.descriptor.impl

import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import spock.lang.Specification
import static ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor.CLARIFICATIONS_ATTR

/** Тест конвертации ServiceDescriptor для модульного описания модальных окон */
class DescriptorClarificationConverterTest extends Specification {
    private static final CLARIFICATION_OLD_NAME = "Karl Sebost`yanovich"
    private static final CLARIFICATION_NEW_NAME = "Edmund Aristrhovich"

    def "Convertation old and new"() {
        given:
        ServiceDescriptor descriptor = new ServiceDescriptor()
        descriptor.applicationFields = [getOldComponent(), getNewComponent()]
        descriptor.clarifications = getClarificationsProp()

        when:
        DescriptorClarificationConverter.convert(descriptor)

        then:
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_OLD_NAME) == "old"
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 1
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_NEW_NAME) == "new"
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 1
    }

    def "Convertation - double"() {
        given:
        ServiceDescriptor descriptor = new ServiceDescriptor()
        descriptor.applicationFields = [getOldComponent(), getNewComponent()]
        descriptor.clarifications = getClarificationsProp()

        when:
        DescriptorClarificationConverter.convert(descriptor)
        DescriptorClarificationConverter.convert(descriptor)

        then:
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_OLD_NAME) == "old"
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 1
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_NEW_NAME) == "new"
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 1
    }

    def "Convertation new - not found"() {
        given:
        ServiceDescriptor descriptor = new ServiceDescriptor()
        descriptor.applicationFields = [getOldComponent(), getNewComponent()]

        when:
        DescriptorClarificationConverter.convert(descriptor)

        then:
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_OLD_NAME) == "old"
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 1
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_NEW_NAME) == null
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 0
    }

    def "Convertation new - many objects"() {
        given:
        ServiceDescriptor descriptor = new ServiceDescriptor()
        descriptor.applicationFields = [getOldComponent(), getNewComponent()]
        descriptor.clarifications = getClarificationsProp()
        descriptor.clarifications.put(CLARIFICATION_NEW_NAME + " many", "descriptor.clarification")

        when:
        DescriptorClarificationConverter.convert(descriptor)

        then:
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_OLD_NAME) == "old"
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 1
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_NEW_NAME) == "new"
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 1
    }

    def "Convertation - clarifications is null"() {
        given:
        ServiceDescriptor descriptor = new ServiceDescriptor()
        descriptor.applicationFields = [getOldComponent(), getNewComponent()]

        when:
        DescriptorClarificationConverter.convert(descriptor)

        then:
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_OLD_NAME) == "old"
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 1
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).get(CLARIFICATION_NEW_NAME) == null
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>).values().size() == 0
    }

    def "Convertation - without clarification"() {
        given:
        ServiceDescriptor descriptor = new ServiceDescriptor()

        FieldComponent fieldComponent = new FieldComponent()
        fieldComponent.attrs = new HashMap<String, Object>()

        FieldComponent fieldComponent2 = new FieldComponent()
        fieldComponent2.attrs = new HashMap<String, Object>()

        descriptor.applicationFields = [fieldComponent, fieldComponent2]

        when:
        DescriptorClarificationConverter.convert(descriptor)

        then:
        (descriptor.applicationFields[0].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>) == null
        (descriptor.applicationFields[1].attrs.get(CLARIFICATIONS_ATTR) as HashMap<String, Object>) == null
    }

    /** Старый способ описания */
    private static FieldComponent getOldComponent() {
        HashMap clarifications = new HashMap<String, Object>()
        clarifications.put(CLARIFICATION_OLD_NAME, "old")

        HashMap attrs = new HashMap<String, Object>()
        attrs.put(CLARIFICATIONS_ATTR, clarifications)

        FieldComponent fieldComponent = new FieldComponent()
        fieldComponent.attrs = attrs
        return fieldComponent
    }

    /** Новый способ описания */
    private static FieldComponent getNewComponent() {
        ArrayList<String> clarifications = [CLARIFICATION_NEW_NAME, "not_found"]

        HashMap attrs = new HashMap<String, Object>()
        attrs.put(CLARIFICATIONS_ATTR, clarifications)

        FieldComponent fieldComponent = new FieldComponent()
        fieldComponent.attrs = attrs
        return fieldComponent
    }

    /** Раздел clarifications для нового способа описания */
    private static HashMap<String, Object> getClarificationsProp() {
        HashMap clarifications = new HashMap<String, Object>()
        clarifications.put(CLARIFICATION_NEW_NAME, "new")
        return clarifications.clone() as HashMap<String, Object>
    }
}
