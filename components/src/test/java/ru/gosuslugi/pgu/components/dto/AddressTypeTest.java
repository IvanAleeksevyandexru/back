package ru.gosuslugi.pgu.components.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Тестирование AddressType enum
 */
public class AddressTypeTest {

    /**
     * {@link AddressType#fromString(java.lang.String)}
     */
    @Test
    public void smokeTestFromString() {
        assertNull(AddressType.fromString(null));
        assertNull(AddressType.fromString(""));
        assertNull(AddressType.fromString("Unknown"));
        for (AddressType addressType : AddressType.values()) {
            assertEquals(addressType, AddressType.fromString(addressType.name()));
        }
    }
}
