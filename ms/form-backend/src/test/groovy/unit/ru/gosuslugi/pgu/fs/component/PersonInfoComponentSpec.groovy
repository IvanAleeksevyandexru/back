package unit.ru.gosuslugi.pgu.fs.component

import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.BaseComponent
import ru.gosuslugi.pgu.fs.component.personinfo.PersonAgeService
import ru.gosuslugi.pgu.fs.component.personinfo.PersonInfoComponent
import ru.gosuslugi.pgu.fs.component.personinfo.model.AgeType
import ru.gosuslugi.pgu.fs.component.personinfo.model.PersonAge
import ru.gosuslugi.pgu.fs.component.personinfo.model.PersonInfoDto
import spock.lang.Specification

class PersonInfoComponentSpec extends Specification{

    PersonAgeService personAgeService
    AgeType ageType

    def setup() {
        personAgeService = Mock(PersonAgeService)
        ageType = AgeType.YOUNG
    }

    def 'Can get cycledInitial value'() {
        given:
        BaseComponent<PersonInfoDto> personInfoComponent = new PersonInfoComponent(personAgeService)
        PersonAge personAge = new PersonAge(7L,6L)
        personAgeService.createPersonAge(_ as String) >> personAge
        def fieldComponent = [type: ComponentType.PersonInfo, attrs: [
                fields: [
                        [fieldName: "birthDate"],
                        [fieldName: "firstName"],
                        [fieldName: "gender"]
                ]]] as FieldComponent

        Map<String, Object> externalData = new HashMap<>()
        externalData.put('birthDate', '2015-03-03T00:00:00.000Z')
        externalData.put('firstName', 'Алена')
        externalData.put('gender', 'F')

        def personInfoDto = new PersonInfoDto()
        personInfoDto.setName('Алена')
        personInfoDto.setAgeText('7 лет')
        personInfoDto.setAgeType(ageType)
        personInfoDto.setGender('F')
        def expected = personInfoDto

        when:
        def actual = personInfoComponent.getCycledInitialValue(fieldComponent, externalData)

        then:
        expected.gender == actual.get().gender
        expected.ageText == actual.get().ageText
        expected.ageType == actual.get().ageType
        expected.name == actual.get().name
    }
}
