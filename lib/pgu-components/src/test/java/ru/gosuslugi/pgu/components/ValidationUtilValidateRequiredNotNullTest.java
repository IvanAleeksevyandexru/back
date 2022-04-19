package ru.gosuslugi.pgu.components;

import org.junit.Test;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class ValidationUtilValidateRequiredNotNullTest {

    @Test
    public void validateRequiredNullInvalid() throws IOException {
        FieldComponent component = new FieldComponent();
        component.setRequired(true);

        Map.Entry<String, String> result = ValidationUtil.validateRequiredNotNull("name", null, component, "error");
        assertNotNull(result);
        assertEquals("name", result.getKey());
        assertEquals("error",  result.getValue());
    }

    @Test
    public void validateRequiredNullValid() throws IOException {
        FieldComponent component = new FieldComponent();
        component.setRequired(false);

        Map.Entry<String, String> result = ValidationUtil.validateRequiredNotNull("name", null, component, "error");
        assertNull(result);
    }

    @Test
    public void validateRequiredNotNullValid() throws IOException {
        FieldComponent component = new FieldComponent();
        component.setRequired(true);

        Map.Entry<String, String> result = ValidationUtil.validateRequiredNotNull("name", "value", component, "error");
        assertNull(result);
    }
}
