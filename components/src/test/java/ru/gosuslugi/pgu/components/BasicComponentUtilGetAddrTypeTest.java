package ru.gosuslugi.pgu.components;

import org.junit.Test;
import ru.gosuslugi.pgu.components.dto.AddressType;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link BasicComponentUtil#getAddrType(ru.gosuslugi.pgu.dto.descriptor.FieldComponent)}
 */
public class BasicComponentUtilGetAddrTypeTest {

    @Test
    public void smokeTestGetAddrType() {

        FieldComponent component = new FieldComponent();
        assertNull(BasicComponentUtil.getAddrType(component));

        component.setAttrs(new LinkedHashMap<>());
        assertNull(BasicComponentUtil.getAddrType(component));

        component.getAttrs().put("key", "value");
        assertNull(BasicComponentUtil.getAddrType(component));

        component.getAttrs().put(BasicComponentUtil.PRESET_ADDR_TYPE, "value");
        assertNull(BasicComponentUtil.getAddrType(component));

        component.getAttrs().put(BasicComponentUtil.PRESET_ADDR_TYPE, AddressType.actualResidence.name());
        assertEquals(AddressType.actualResidence, BasicComponentUtil.getAddrType(component));

        component.getAttrs().put(BasicComponentUtil.PRESET_ADDR_TYPE, AddressType.permanentRegistry.name());
        assertEquals(AddressType.permanentRegistry, BasicComponentUtil.getAddrType(component));
    }
}
