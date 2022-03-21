package unit.ru.gosuslugi.pgu.fs.component.logic

import ru.gosuslugi.pgu.fs.common.exception.FormBaseException
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.component.logic.RestCallComponent
import ru.gosuslugi.pgu.fs.service.impl.RestCallServiceImpl
import spock.lang.Specification

class RestCallComponentSpec extends Specification {

    RestCallComponent component = new RestCallComponent(_ as String, new RestCallServiceImpl())

    def 'Can set method'() {
        when: 'method not present'
        component.getInitialValue(new FieldComponent(attrs: [url: 'url', path: 'path']))

        then:
        thrown(FormBaseException)

        when: 'method is empty'
        component.getInitialValue(new FieldComponent(attrs: [method: '', url: 'url', path: 'path']))

        then:
        thrown(FormBaseException)

        when: 'method not valid'
        component.getInitialValue(new FieldComponent(attrs: [method: '1', url: 'url', path: 'path']))

        then:
        thrown(FormBaseException)

        when:
        def result = component.getInitialValue(new FieldComponent(attrs: [method: 'get', url: 'url', path: 'path']))

        then:
        result.get().method == 'GET'
    }

    def 'Can set url'() {
        when: 'url not present'
        component.getInitialValue(new FieldComponent(attrs: [path: 'path']))

        then:
        thrown(FormBaseException)

        when: 'path not present'
        component.getInitialValue(new FieldComponent(attrs: [url: 'url']))

        then:
        thrown(FormBaseException)

        when:
        def result = component.getInitialValue(getSimpleFieldComponent())

        then:
        result.get().url == 'http://host/url_0/path/path_0?param1=pa1&param2=p2'
    }

    def 'Can set body'() {
        when:
        def result = component.getInitialValue(getSimpleFieldComponent())

        then:
        result.get().body == '{"arg1": "a1", "arg2": "a2"}'
    }

    def 'Can set headers'() {
        when:
        def result = component.getInitialValue(getSimpleFieldComponent())

        then:
        result.get().headers == [header2: 'h2', header1: 'ha1']
    }

    def 'Can set cookies'() {
        when:
        def result = component.getInitialValue(getSimpleFieldComponent())

        then:
        result.get().cookies == [cooki2: 'c2', cookie1: 'ca1']
    }

    def 'Can set timeout'() {
        when:
        def result = component.getInitialValue(getSimpleFieldComponent())

        then:
        result.get().timeout == 100500l

        when:
        def fieldComponent = getSimpleFieldComponent()
        fieldComponent.attrs['timeout'] = 'a1'
        result = component.getInitialValue(fieldComponent)

        then:
        result.get().timeout == null
    }

    static def getSimpleFieldComponent() {
        new FieldComponent(
                attrs: [
                        method         : 'get',
                        url            : 'http://host/${sub_url}',
                        path           : '/path/${sub_path}',
                        timeout        : '100500',
                        queryParameters: [param1: 'p${a1}', param2: 'p2'],
                        body           : '{"arg1": "${a1}", "arg2": "a2"}',
                        headers        : [header1: 'h${a1}', header2: 'h2'],
                        cookies        : [cookie1: 'c${a1}', cooki2: 'c2']],
                arguments: [a1: 'a1', sub_url: 'url_0', sub_path: 'path_0'])
    }
}
