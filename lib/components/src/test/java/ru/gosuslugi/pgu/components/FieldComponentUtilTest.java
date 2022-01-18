package ru.gosuslugi.pgu.components;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FieldComponentUtilTest {

    @Test
    public void testToStringMapWithValueNull() throws IOException {
        Map<Object, Object> map = new HashMap<>();
        map.put("value", null);
        map.put("value1", 1);
        Map<String, String> result = FieldComponentUtil.toStringMap(map);
        assertTrue(result.containsKey("value"));
        assertNull(result.get("value"));
        assertTrue(result.containsKey("value1"));
        assertEquals("1", result.get("value1"));
    }
}
