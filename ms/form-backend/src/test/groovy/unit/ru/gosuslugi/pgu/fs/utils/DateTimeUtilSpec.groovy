package unit.ru.gosuslugi.pgu.fs.utils

import ru.gosuslugi.pgu.fs.utils.DateTimeUtil
import spock.lang.Specification

import java.time.LocalDate

class DateTimeUtilSpec extends Specification {

    def 'testParseDate'() {
        when:
        def value = DateTimeUtil.parseDate(date, format)

        then:
        value == expectedValue

        where:
        date         | format              | expectedValue
        '2021-06-01' | 'yyyy-MM-dd'        | LocalDate.of(2021, 06, 01)
        '01.06.2021' | 'dd.MM.yyyy'        | LocalDate.of(2021, 06, 01)
        '01-06-2021' | 'dd-MM-yyyy'        | LocalDate.of(2021, 06, 01)
        '2021-06-01' | 'yyyy-MM-dd zzzzzz' | null
        '2021-06-01' | '%%%%%%%%%%'        | null
        '2021/06/01' | 'yyyy-MM-dd'        | null
    }

}