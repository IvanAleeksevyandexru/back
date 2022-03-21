package ru.gosuslugi.pgu.fs.component.logic

import org.springframework.http.HttpMethod
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.logic.model.RestCallDto
import ru.gosuslugi.pgu.fs.service.impl.RestCallServiceImpl
import spock.lang.Specification

class RestCallComponentSpec extends Specification {

    def 'test RestCall component with arument in body'() {
        given:
        def component = new RestCallComponent(_ as String, new RestCallServiceImpl())
        ComponentTestUtil.setAbstractComponentServices(component)

        when:
        RestCallDto result = component.getInitialValue(getFieldComponent()).get();

        then:
        component.getType() == ComponentType.RestCall
        result.getMethod() == HttpMethod.POST.toString()
        result.getUrl() == 'https://gosuslugi.ru/search/xml?user=mister-x&key=24748491&filter=strict'
        result.getBody() == '{\"arg\":hello\"}'
        result.getTimeout() == 500
        result.getHeaders().size() == 2
        result.getHeaders().get('Content-Type') == 'application/json'
        result.getHeaders().get('Accept') == 'application/json, text/plain, */*'
        result.getCookies().size() == 2
        result.getCookies().get('u') == '1000298933'
        result.getCookies().get('acc_t') == 'eyJ2ZXIiOjEsInR5cCI6IkpXVCIs'
    }

    def 'test default url'() {
        given:
        def component = new RestCallComponent('http://url_to_pgu', new RestCallServiceImpl())

        when:
        RestCallDto result = component.getInitialValue(getSimpleFieldComponent()).get();

        then:
        result.getUrl() == 'http://url_to_pgu/'
    }

    def static getFieldComponent() {
        [
                id       : 'rc1',
                type     : 'RestCall',
                attrs    : [
                        method         : 'POST',
                        url            : 'https://gosuslugi.ru',
                        path           : '/search/xml',
                        body           : '{\"arg\":${queryArg}\"}',
                        headers        : ['Content-Type': 'application/json', 'Accept': 'application/json, text/plain, */*'],
                        cookies        : ['u': '1000298933', 'acc_t': 'eyJ2ZXIiOjEsInR5cCI6IkpXVCIs'],
                        timeout        : 500,
                        queryParameters: ['user': 'mister-x', 'key': '24748491', 'filter': 'strict']
                ] as LinkedHashMap,
                arguments: [queryArg: 'hello']
        ] as FieldComponent
    }

    def static getSimpleFieldComponent() {
        [attrs: [method: 'POST', path: '/']] as FieldComponent
    }
}
