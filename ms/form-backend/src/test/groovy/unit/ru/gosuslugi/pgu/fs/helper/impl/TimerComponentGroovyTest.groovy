package unit.ru.gosuslugi.pgu.fs.helper.impl

import io.micrometer.core.instrument.util.StringUtils
import ru.gosuslugi.pgu.dto.PguTimer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.time.TimerComponent
import ru.gosuslugi.pgu.fs.service.TimerService
import spock.lang.Specification

import java.time.ZonedDateTime

class TimerComponentGroovyTest extends Specification {

    private TimerService timerService
    private TimerComponent timerComponent
    private ScenarioDto scenarioDto
    private ServiceDescriptor serviceDescriptor

    def setup() {
        timerService = Mock(TimerService)
        timerComponent = Mock(TimerComponent)
        scenarioDto = Stub(ScenarioDto.class)
        serviceDescriptor = Stub(ServiceDescriptor)
    }

    def 'null parameter values are not allowed'() {

        given:
        TimerComponent timerComponent = null

        when:
        timerComponent.getType()

        then:
        thrown(NullPointerException)
    }

    def 'check component type'() {

        when:
        TimerComponent component = new TimerComponent(timerService);

        then:
        assert component != null
        assert component.getType() == ComponentType.Timer
    }

    def 'test if scenario dto is null'() {

        given:
        FieldComponent fieldComponent = getFieldComponent("someId")
        TimerComponent component = new TimerComponent(timerService);
        ComponentTestUtil.setAbstractComponentServices(component)

        when:
        component.process(fieldComponent, scenarioDto, serviceDescriptor)
        String value = fieldComponent.getValue()

        then:
        assert value != null
        assert StringUtils.isEmpty(value)

    }

    def 'test preset component value'() {

        given:
        ZonedDateTime startTime = ZonedDateTime.now()
        ZonedDateTime expirationTime = ZonedDateTime.now().plusMinutes(60)
        PguTimer timer = new PguTimer()
        timer.setStartTime(startTime.toString())
        timer.setExpirationTime(expirationTime.toString())
        TimerComponent component = new TimerComponent(timerService)
        ComponentTestUtil.setAbstractComponentServices(component)
        Map<String, Object> attrs = new HashMap<>()
        attrs.put("timerCode", "testCode")
        FieldComponent fieldComponent = getFieldComponent("someId", attrs)
        timerService.getTimer(0L, "testCode") >> timer

        when:
        component.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        verifyAll { timerService.getTimer(0L, "testCode") }
        assert !fieldComponent.getAttrs().isEmpty()
        assert fieldComponent.getAttrs().containsKey("startTime")
        assert startTime.toString() == fieldComponent.getAttrs().get("startTime")
        assert fieldComponent.getAttrs().containsKey("expirationTime")
        assert expirationTime.toString() == fieldComponent.getAttrs().get("expirationTime")
    }

    def 'test preset component value from ref'() {
        given:
        FieldComponent fieldComponent = getFieldComponent("someId")
        TimerComponent component = new TimerComponent(timerService)
        ComponentTestUtil.setAbstractComponentServices(component)

        when:
        component.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        verifyAll { 0 * timerService.getTimer("someId", null) }

    }

    FieldComponent getFieldComponent(String componentId, Map<String, Object> attributes) {
        FieldComponent component = new FieldComponent()
        component.setId(componentId)
        component.setAttrs(attributes)
        return component
    }

    FieldComponent getFieldComponent(String componentId) {
        Map<String, Object> attributes = new HashMap<>()
        return getFieldComponent(componentId, attributes)
    }

    PguTimer getTimer(String code, ZonedDateTime startTime, ZonedDateTime expirationTime) {
        PguTimer timer = new PguTimer()
        timer.setCode(code)
        timer.setStartTime(startTime.toString())
        timer.setExpirationTime(expirationTime.toString())
        return timer
    }

}