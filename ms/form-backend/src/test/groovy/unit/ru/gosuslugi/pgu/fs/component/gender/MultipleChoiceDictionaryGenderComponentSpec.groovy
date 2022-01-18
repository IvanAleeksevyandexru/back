package unit.ru.gosuslugi.pgu.fs.component.gender

import ru.gosuslugi.pgu.fs.component.gender.MultipleChoiceDictionaryGenderComponent
import spock.lang.Specification

class MultipleChoiceDictionaryGenderComponentSpec extends Specification {

    MultipleChoiceDictionaryGenderComponent component

    def setup() {
        component = new MultipleChoiceDictionaryGenderComponent()
    }

    def 'Can process component attributes'() {
        given:
        Map attrs = [dictionaryType: ['maleDictionary', 'femaleDictionary']]
        def result

        when:
        result = component.processAttrs(gender, attrs)

        then:
        dictionary == result['dictionaryType']

        where:
        gender | dictionary
        'M'    | 'maleDictionary'
        'F'    | 'femaleDictionary'
    }
}