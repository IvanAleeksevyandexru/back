package unit.ru.gosuslugi.pgu.fs.service.impl.process

import ru.gosuslugi.pgu.fs.service.process.impl.AbstractProcess
import spock.lang.Specification

import java.util.function.Consumer
import java.util.function.Predicate

class AbstractProcessSpec extends Specification {

    TestProcess process

    def 'Execute process'() {
        given:
        def result

        when: 'процесс из одного действия'
        process = new TestProcess()
        result = process.of('request')
                .execute({process.setSuccessResponse()} as Consumer)
                .start()

        then:
        result == 'success'

        when: 'процесс с условием, условие выполняется'
        process = new TestProcess()
        result = process.of('request')
                .executeIf({it -> process.right} as Predicate, {process.setSuccessResponse()} as Consumer)
                .start()

        then:
        result == 'success'

        when: 'процесс с условием, условие не выполняется'
        process = new TestProcess()
        result = process.of('request')
                .executeIf({it -> process.wrong} as Predicate, {process.setSuccessResponse()} as Consumer, null)
                .start()

        then:
        result == ''

        when: 'процесс с условием завершения, условие выполняется'
        process = new TestProcess()
        result = process.of('request')
                .execute({process.setSuccessResponse()} as Consumer)
                .completeIf({it -> process.right} as Predicate)
                .execute({process.setErrorResponse()} as Consumer)
                .start()

        then:
        result  == 'success'

        when: 'процесс с условием завершения, условие не выполняется'
        process = new TestProcess()
        result = process.of('1')
                .execute({process.setSuccessResponse()} as Consumer)
                .completeIf({it -> process.wrong} as Predicate)
                .execute({process.setErrorResponse()} as Consumer)
                .start()

        then:
        result == 'error'
    }

    class TestProcess extends AbstractProcess<TestProcess, String> {

        String request

        TestProcess of(String request) {
            this.response = ''
            this.request = request
            return this
        }

        @Override
        TestProcess getProcess() {
            this
        }

        boolean isRight() {
            true
        }

        boolean isWrong() {
            false
        }

        void setSuccessResponse() {
            response = 'success'
        }

        void setErrorResponse() {
            response = 'error'
        }
    }

}