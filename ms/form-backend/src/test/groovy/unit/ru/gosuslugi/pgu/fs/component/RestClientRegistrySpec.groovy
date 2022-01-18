package unit.ru.gosuslugi.pgu.fs.component

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.fs.component.RestClientRegistry
import spock.lang.Specification

class RestClientRegistrySpec extends Specification {
    RestClientRegistry registry
    RestTemplate restTemplate

    def setup() {
        restTemplate = new RestTemplate()
        registry = new RestClientRegistry(restTemplate, Stub(RestTemplateBuilder))
        registry.getRestTemplate(220000)
        registry.getRestTemplate(220000)
        registry.getRestTemplate(100500)
        registry.getRestTemplate(100500)
    }

    @SuppressWarnings("GroovyAccessibility")
    def test() {
        expect:
        registry.getRestTemplate() == restTemplate
        registry.getRestTemplate(-1) == restTemplate
        RestClientRegistry.registry.size() == 3
    }
}
