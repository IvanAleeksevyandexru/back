package ru.gosuslugi.pgu.fs.component.child

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.*
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.service.ListComponentItemUniquenessService
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.EsiaUsers
import ru.gosuslugi.pgu.fs.component.input.RepeatableFieldsComponent
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService
import ru.gosuslugi.pgu.fs.service.ParticipantService
import ru.atc.carcass.security.rest.model.person.Kids
import spock.lang.Specification

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.StringInput

@SuppressWarnings("GroovyAccessibility")
class ExcludeChildComponentTest extends Specification {

    private static ScenarioDto scenarioDto
    private static FieldComponent fieldComponent
    private static ServiceDescriptor serviceDescriptor
    private static ChildrenListComponent component

    private static Map<String, Object> kidsMap = [
            "kid100" : [
                    "id": "100",
                    "esiaData" : [
                            "lastName": "Бричкина",
                            "gender": "F",
                            "firstName": "Людмила",
                            "middleName": "Прокофьевна",
                            "id": "100",
                            "snils": "010-100-313 22"
                    ]
            ],
            "kid200" : [
                    "id": "200",
                    "esiaData" : [
                            "lastName": "Иванов",
                            "gender": "M",
                            "firstName": "Сергей",
                            "middleName": "Степанович",
                            "id": "200",
                            "snils": "010-200-313 33"
                    ]
            ],
            "kid300" : [
                    "id": "300",
                    "esiaData" : [
                            "lastName": "Иванов",
                            "gender": "M",
                            "firstName": "Егор",
                            "middleName": "Степанович",
                            "id": "300"
                    ]
            ],
            "kid400" : [
                    "id": "400",
                    "esiaData" : [
                            "lastName": "Иванов",
                            "gender": "M",
                            "firstName": "Иван",
                            "middleName": "Степанович",
                            "snils": "010-400-313 40"
                    ]
            ],
            "kid500" : [
                    "esiaData" : [
                            "lastName": "Павлова",
                            "gender": "F",
                            "firstName": "Геля",
                            "middleName": "Степановна"
                    ]
            ],
            "kid666" : [
                    "id": "666",
                    "esiaData" : [
                            "lastName": "Лютиков",
                            "gender": "M",
                            "firstName": "Иван",
                            "middleName": "Степанович",
                            "id": "666",
                            "snils": ""
                    ]
            ]

    ]

    private static Integer excludeBadObject = 10
    private static List excludeSingle = [ "f22" ]
    private static List excludePair = [ "f666" , "f22" ]

    def setupSpec() {

        scenarioDto = new ScenarioDto()
        scenarioDto.setCurrentValue(Map.of())

        fieldComponent = new FieldComponent(type: ComponentType.ChildrenList, attrs: [
                fields: [
                ]])
        fieldComponent.setId("child21")

        serviceDescriptor = new ServiceDescriptor(applicationFields: [
                new FieldComponent(id: 'cl24', type: StringInput, attrs: [components: ["bb", "cc"], fields: [["fieldName": "firstName"], ["fieldName": "lastName"], ["fieldName": "rfBirthCertificateSeries"], ["fieldName": "rfBirthCertificateIssueDate"]]]),
                new FieldComponent(id: 'bb', type: StringInput, attrs: [fields: [["fieldName": "firstName"], ["fieldName": "lastName"], ["fieldName": "rfBirthCertificateSeries"], ["fieldName": "rfBirthCertificateIssueDate"]]]),
                new FieldComponent(id: 'cc', type: StringInput, attrs: [fields: [["fieldName": "isNew"]]])
        ])

        UserPersonalData user = EsiaUsers.userWithChildren

        component = new ChildrenListComponent(
                user,
                Mock(RepeatableFieldsComponent),
                Mock(ParticipantService),
                Mock(ListComponentItemUniquenessService),
                Mock(DictionaryListPreprocessorService)
        )
        ComponentTestUtil.setAbstractComponentServices(component)

    }

    CycledApplicantAnswerItem buildAnswerItem(String kidKey) {
        Map<String, Object> map = (Map<String, Object>)kidsMap.get(kidKey)
        if (map != null) {
            CycledApplicantAnswerItem item = new CycledApplicantAnswerItem()
            item.setId((String)map.get("id"))
            item.setEsiaData((Map<String, Object>)map.get("esiaData"))
            return item
        }
        return null
    }
    Kids buildKid(String kidKey) {
        Map<String, Object> map = (Map<String, Object>)kidsMap.get(kidKey)
        if (map != null) {
            Kids kid = new Kids()
            Map<String, Object> esiaData = (Map<String, Object>)map.get("esiaData")
            kid.setId((String)esiaData.get("id"))
            kid.setSnils((String)esiaData.get("snils"))
            kid.setFirstName((String)esiaData.get("firstName"))
            kid.setLastName((String)esiaData.get("lastName"))
            kid.setMiddleName((String)esiaData.get("middleName"))
            kid.setGender((String)esiaData.get("gender"))
            return kid
        }
        return null
    }
    List<Kids> buildKids(List<String> keys) {
        List<Kids> kids = new ArrayList()
        for (String key : keys) {
            kids.add(buildKid(key))
        }
        return kids
    }
    String kidToString(Kids kid) {
        StringBuilder sb = new StringBuilder()
        sb.append("Kid{")
        sb.append("id="+kid.getId())
        sb.append(", snils="+kid.getSnils())
        sb.append(", firstName="+kid.getFirstName())
        sb.append(", lastName="+kid.getLastName())
        sb.append(", middleName="+kid.getMiddleName())
        sb.append(", gender="+kid.getGender())
        sb.append("}")
        return sb.toString()
    }
    String kidsToString(List<Kids> kids) {
        StringBuilder sb = new StringBuilder()
        for (Kids kid : kids) {
            sb.append(" "+kidToString(kid)+" ")
        }
        return sb.toString()
    }
    void kidsPrintln(List<Kids> kids) {
        for (Kids kid : kids) {
            println(kidToString(kid))
        }
    }

    void cleanAnswers() {
        scenarioDto.getCycledApplicantAnswers().setAnswers(new LinkedList<>())
    }

    def setAnswer(String answerId, String kidKey) {
        CycledApplicantAnswer answer = new CycledApplicantAnswer(answerId)
        CycledApplicantAnswerItem item = buildAnswerItem(kidKey)
        answer.addItemIfAbsent(answerId, item)
        scenarioDto.getCycledApplicantAnswers().getAnswers().add(answer)
    }

    def 'Get an exclude component list from ClassCast object'() {
        given:
        fieldComponent.getAttrs().put("excludeChildComponents", excludeBadObject)
        when:
        def result = component.getExcludedDataEsiaList(fieldComponent, scenarioDto)
        then:
        assert result.isEmpty()
    }

    def 'Exclude kid with id'() {
        given:
        fieldComponent.getAttrs().put("excludeChildComponents", excludeSingle)
        cleanAnswers()
        setAnswer("f22", "kid100")
        List<String> keys = [ "kid200", "kid300", "kid100", "kid400", "kid500" ]
        List<Kids> kids = buildKids(keys)

        when:
        def result = component.excludeFromKids(fieldComponent, scenarioDto, kids)

        then:
        assert result.size() == 4 && !kidsToString(result).contains("id=100")
    }

    def 'Exclude kid without id and snils'() {
        given:
        fieldComponent.getAttrs().put("excludeChildComponents", excludeSingle)
        cleanAnswers()
        setAnswer("f22", "kid500")
        List<String> keys = [ "kid200", "kid300", "kid100", "kid400", "kid500" ]
        List<Kids> kids = buildKids(keys)

        when:
        def result = component.excludeFromKids(fieldComponent, scenarioDto, kids)

        then:
        assert result.size() == 4 && !kidsToString(result).contains("firstName=Геля")
    }

    def 'Exclude kid when there is another answer' () {
        given:
        fieldComponent.getAttrs().put("excludeChildComponents", excludePair)
        cleanAnswers()
        setAnswer("f22", "kid300")
        setAnswer("f666", "kid666")
        List<String> keys = [ "kid200", "kid300", "kid100", "kid400", "kid500" ]
        List<Kids> kids = buildKids(keys)

        when:
        def result = component.excludeFromKids(fieldComponent, scenarioDto, kids)

        then:
        assert result.size() == 4 && !kidsToString(result).contains("id=300")
    }

    def 'Exclude two kids' () {
        given:
        fieldComponent.getAttrs().put("excludeChildComponents", excludePair)
        cleanAnswers()
        setAnswer("f22", "kid100")
        setAnswer("f666", "kid666")
        List<String> keys = [ "kid200", "kid300", "kid100", "kid400", "kid500", "kid666" ]
        List<Kids> kids = buildKids(keys)

        when:
        def result = component.excludeFromKids(fieldComponent, scenarioDto, kids)

        then:
        assert result.size() == 4 && !kidsToString(result).contains("id=100") && !kidsToString(result).contains("id=666")
    }

    def 'Exclude two kids when excludeChildComponents != [answer0.id, answer1.id]' () {
        given:
        fieldComponent.getAttrs().put("excludeChildComponents", excludePair)
        cleanAnswers()
        setAnswer("f1000", "kid100")
        setAnswer("f2000", "kid666")
        List<String> keys = [ "kid200", "kid300", "kid100", "kid400", "kid500", "kid666" ]
        List<Kids> kids = buildKids(keys)

        when:
        def result = component.excludeFromKids(fieldComponent, scenarioDto, kids)

        then:
        assert result.size() == 6
    }

}
