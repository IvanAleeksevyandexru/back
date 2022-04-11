package ru.gosuslugi.pgu.components.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Тестирование AddressType enum
 */
public class AddressTypeTest {

    /**
     * {@link AddressType#formString(java.lang.String)}
     */
    @Test
    public void smokeTestFromString() {
        assertNull(AddressType.formString(null));
        assertNull(AddressType.formString(""));
        assertNull(AddressType.formString("Unknown"));
        for (AddressType addressType : AddressType.values()) {
            assertEquals(addressType, AddressType.formString(addressType.name()));
        }
    }
}
