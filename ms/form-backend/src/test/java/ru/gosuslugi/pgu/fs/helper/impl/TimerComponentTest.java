package ru.gosuslugi.pgu.fs.helper.impl;

import io.micrometer.core.instrument.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.gosuslugi.pgu.dto.PguTimer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ServiceInfoDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil;
import ru.gosuslugi.pgu.fs.component.time.TimerComponent;
import ru.gosuslugi.pgu.fs.service.TimerService;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;


@RunWith(MockitoJUnitRunner.class)
public class TimerComponentTest {

    @Mock
    private TimerService timerService;

    @Test(expected = NullPointerException.class)
    public void constructorOfNull() {
        new TimerComponent( null);
    }

    @Test
    public void testComponentType() {
        TimerComponent component =
                new TimerComponent(timerService);
        Assert.assertNotNull(component);
        Assert.assertEquals(ComponentType.Timer, component.getType());
    }

    @Test
    public void testScenarioDtoNull() {
        FieldComponent fieldComponent = getFieldComponent("someId");
        TimerComponent component = new TimerComponent(timerService);
        ComponentTestUtil.setAbstractComponentServices(component);

        ScenarioDto scenarioDto = mock(ScenarioDto.class);
        Mockito.when(scenarioDto.getServiceInfo()).thenReturn(new ServiceInfoDto());
        component.process(fieldComponent, scenarioDto, mock(ServiceDescriptor.class));
        String value = fieldComponent.getValue();
        Assert.assertNotNull(value);
        Assert.assertTrue(StringUtils.isEmpty(value));
    }

    @Test
    public void testPreSetComponentValueTimerService() {
        ZonedDateTime startTime = ZonedDateTime.now();
        ZonedDateTime expirationTime = ZonedDateTime.now().plusMinutes(60);
        PguTimer timer = getTimer("testCode", startTime, expirationTime);
        Mockito.when(timerService.getTimer(Mockito.anyLong(), Mockito.anyString())).thenReturn(timer);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("timerCode", "testCode");
        FieldComponent fieldComponent = getFieldComponent("someId", attrs);
        TimerComponent component =
                new TimerComponent(timerService);
        ComponentTestUtil.setAbstractComponentServices(component);
        ScenarioDto scenarioDto = mock(ScenarioDto.class);
        Mockito.when(scenarioDto.getServiceInfo()).thenReturn(new ServiceInfoDto());

        component.process(fieldComponent, scenarioDto, mock(ServiceDescriptor.class));
        Mockito.verify(timerService, Mockito.times(1)).getTimer(Mockito.anyLong(), ArgumentMatchers.eq("testCode"));
        Assert.assertFalse(fieldComponent.getAttrs().isEmpty());
        Assert.assertTrue(fieldComponent.getAttrs().containsKey("startTime"));
        Assert.assertEquals(startTime.toString(), fieldComponent.getAttrs().get("startTime"));
        Assert.assertTrue(fieldComponent.getAttrs().containsKey("expirationTime"));
        Assert.assertEquals(expirationTime.toString(), fieldComponent.getAttrs().get("expirationTime"));
    }

    @Test
    public void testPreSetComponentValueTimerFromRef() {
        FieldComponent fieldComponent = getFieldComponent("someId");
        TimerComponent component = new TimerComponent(timerService);
        ComponentTestUtil.setAbstractComponentServices(component);

        ScenarioDto scenarioDto = mock(ScenarioDto.class);
        Mockito.when(scenarioDto.getServiceInfo()).thenReturn(new ServiceInfoDto());
        component.process(fieldComponent, scenarioDto, mock(ServiceDescriptor.class));
        Mockito.verify(timerService, Mockito.times(0)).getTimer(Mockito.anyLong(), Mockito.anyString());
    }

    FieldComponent getFieldComponent(String componentId, Map<String, Object> attributes) {
        FieldComponent component = new FieldComponent();
        component.setId(componentId);
        component.setAttrs(attributes);
        return component;
    }

    FieldComponent getFieldComponent(String componentId) {
        Map<String, Object> attributes = new HashMap<>();
        return getFieldComponent(componentId, attributes);
    }

    PguTimer getTimer(String code, ZonedDateTime startTime, ZonedDateTime expirationTime) {
        PguTimer timer = new PguTimer();
        timer.setCode(code);
        timer.setStartTime(startTime.toString());
        timer.setExpirationTime(expirationTime.toString());
        return timer;
    }
}
