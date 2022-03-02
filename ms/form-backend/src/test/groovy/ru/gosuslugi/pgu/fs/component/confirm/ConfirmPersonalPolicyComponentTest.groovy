package ru.gosuslugi.pgu.fs.component.confirm

import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import spock.lang.Specification
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil

class ConfirmPersonalPolicyComponentTest extends Specification {

    private static final String COMPONENT_SUFFIX = "-component.json"
    private static final String WITH_ERRORS_COMPONENT_SUFFIX = "-withErrors-component.json"
    private static final String WITH_DEFAULT_HINT_COMPONENT_SUFFIX = "-withDefaultHint-component.json"

    private static final String ERRORS_ATTR = "errors"

    private static final String OMS_UNITED_NUMBER = "unitedNumber"
    private static final String OMS_ISSUE_PLACE = "issuePlace"
    private static final String OMS_ISSUED_BY = "issuedBy"

    UserPersonalData userPersonalData
    ConfirmPersonalPolicyComponent confirmPolicyComponent

    void setup() {
        userPersonalData = Mock(UserPersonalData)
        confirmPolicyComponent = new ConfirmPersonalPolicyComponent(userPersonalData)
    }

    def "Test getType return proper value"() {
        expect:
        confirmPolicyComponent.getType() == ComponentType.ConfirmPersonalPolicy
    }

    def "Test getInitialValue if there is the complete OMS document in the personal storage and errors are defined in attrs"() {
        given:
        def fieldComponent = readComponentFromFile(WITH_ERRORS_COMPONENT_SUFFIX)

        when:
        def initialValue = confirmPolicyComponent.getInitialValue(fieldComponent).get()

        then:
        1 * userPersonalData.getOmsDocument() >> Optional.of(generateOms())
        checkErrorsInAttrs(fieldComponent)
        initialValue.states.find()
        !initialValue.errors.find()
        initialValue.storedValues != null
        initialValue.storedValues.unitedNumber == OMS_UNITED_NUMBER
        initialValue.storedValues.issuePlace == OMS_ISSUE_PLACE
        initialValue.storedValues.issuedBy == OMS_ISSUED_BY
    }

    def "Test getInitialValue if there is the complete OMS document in the personal storage and errors are not defined in attrs"() {
        given:
        def fieldComponent = readComponentFromFile(COMPONENT_SUFFIX)

        when:
        def initialValue = confirmPolicyComponent.getInitialValue(fieldComponent).get()

        then:
        1 * userPersonalData.getOmsDocument() >> Optional.of(generateOms())
        !checkErrorsInAttrs(fieldComponent)
        initialValue.states.find()
        !initialValue.errors.find()
        initialValue.storedValues != null
        initialValue.storedValues.unitedNumber == OMS_UNITED_NUMBER
        initialValue.storedValues.issuePlace == OMS_ISSUE_PLACE
        initialValue.storedValues.issuedBy == OMS_ISSUED_BY
    }

    def "Test getInitialValue if there is the incomplete OMS document in the personal storage and errors are defined in attrs"() {
        given:
        def fieldComponent = readComponentFromFile(WITH_ERRORS_COMPONENT_SUFFIX)

        when:
        def initialValue = confirmPolicyComponent.getInitialValue(fieldComponent).get()

        then:
        1 * userPersonalData.getOmsDocument() >> Optional.of(generateOmsWithoutUnitedNumber())
        checkErrorsInAttrs(fieldComponent)
        initialValue.states.find()
        initialValue.errors.find()
        initialValue.storedValues != null
        initialValue.storedValues.unitedNumber == null
        initialValue.storedValues.issuePlace == OMS_ISSUE_PLACE
        initialValue.storedValues.issuedBy == OMS_ISSUED_BY
    }

    def "Test getInitialValue if there is the incomplete OMS document in the personal storage and errors are not defined in attrs"() {
        given:
        def fieldComponent = readComponentFromFile(COMPONENT_SUFFIX)

        when:
        def initialValue = confirmPolicyComponent.getInitialValue(fieldComponent).get()

        then:
        1 * userPersonalData.getOmsDocument() >> Optional.of(generateOmsWithoutUnitedNumber())
        !checkErrorsInAttrs(fieldComponent)
        initialValue.states.find()
        !initialValue.errors.find()
        initialValue.storedValues != null
        initialValue.storedValues.unitedNumber == null
        initialValue.storedValues.issuePlace == OMS_ISSUE_PLACE
        initialValue.storedValues.issuedBy == OMS_ISSUED_BY
    }

    def "Test getInitialValue if there is no OMS document in the personal storage and errors are defined in attrs"() {
        given:
        def fieldComponent = readComponentFromFile(WITH_ERRORS_COMPONENT_SUFFIX)

        when:
        def initialValue = confirmPolicyComponent.getInitialValue(fieldComponent).get()

        then:
        1 * userPersonalData.getOmsDocument() >> Optional.empty()
        checkErrorsInAttrs(fieldComponent)
        !initialValue.states.find()
        !initialValue.errors.find()
        initialValue.storedValues != null
        initialValue.storedValues.unitedNumber == null
        initialValue.storedValues.issuePlace == null
        initialValue.storedValues.issuedBy == null
    }

    def "Test getInitialValue if there is no OMS document in the personal storage and errors are not defined in attrs"() {
        given:
        def fieldComponent = readComponentFromFile(COMPONENT_SUFFIX)

        when:
        def initialValue = confirmPolicyComponent.getInitialValue(fieldComponent).get()

        then:
        1 * userPersonalData.getOmsDocument() >> Optional.empty()
        !checkErrorsInAttrs(fieldComponent)
        !initialValue.states.find()
        !initialValue.errors.find()
        initialValue.storedValues != null
        initialValue.storedValues.unitedNumber == null
        initialValue.storedValues.issuePlace == null
        initialValue.storedValues.issuedBy == null
    }

    def "Test getInitialValue fill error with defaultHint if OmsDocument isn`t exists" () {
        given:
        def fieldComponent = readComponentFromFile(WITH_DEFAULT_HINT_COMPONENT_SUFFIX)

        when:
        def initialValue = confirmPolicyComponent.getInitialValue(fieldComponent).get()

        then:
        1 * userPersonalData.getOmsDocument() >> Optional.empty()
        fieldComponent.errors.find()
        fieldComponent.errors.size() == 1
        !initialValue.states.find()
        initialValue.storedValues != null
        initialValue.storedValues.unitedNumber == null
        initialValue.storedValues.issuePlace == null
        initialValue.storedValues.issuedBy == null
    }

    def generateOmsWithoutUnitedNumber() {
        PersonDoc document = new PersonDoc()
        document.setIssuePlace(OMS_ISSUE_PLACE)
        document.setIssuedBy(OMS_ISSUED_BY)

        return document
    }

    def generateOms() {
        PersonDoc document = new PersonDoc()
        document.setUnitedNumber(OMS_UNITED_NUMBER)
        document.setIssuePlace(OMS_ISSUE_PLACE)
        document.setIssuedBy(OMS_ISSUED_BY)

        return document
    }

    def checkErrorsInAttrs(FieldComponent fieldComponent) {
        def attrs = fieldComponent.getAttrs()
        return attrs.find() && attrs.get(ERRORS_ATTR).find()
    }

    def readComponentFromFile(prefix) {
        return JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), prefix),
                FieldComponent
        )
    }
}
