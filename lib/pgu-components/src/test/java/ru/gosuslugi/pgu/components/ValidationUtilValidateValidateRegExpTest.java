package ru.gosuslugi.pgu.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;

public class ValidationUtilValidateValidateRegExpTest {


    private static final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();

    @Test
    public void validateRequiredNoRegExp() throws IOException {
        FieldComponent component = new FieldComponent();
        component.setRequired(true);

        Map.Entry<String, String> result = ValidationUtil.validateRegExp("name", "", component);
        assertNull(result);
    }

    @Test
    public void validateRequiredNull() throws IOException {
        FieldComponent component = getFieldComponentFromFile();

        Map.Entry<String, String> result = ValidationUtil.validateRegExp("name", null, component);
        assertNull(result);
    }

    @Test
    public void validateRequiredBlank() throws IOException {
        FieldComponent component = getFieldComponentFromFile();

        Map.Entry<String, String> result = ValidationUtil.validateRegExp("name", "", component);
        assertNotNull(result);
        assertEquals("name", result.getKey());
        assertEquals("Поле не может быть пустым, Поле может содержать только русские буквы", result.getValue());
    }

    @Test
    public void validateRequiredNoRussian() throws IOException {
        FieldComponent component = getFieldComponentFromFile();

        Map.Entry<String, String> result = ValidationUtil.validateRegExp("name", "russian chars", component);
        assertNotNull(result);
        assertEquals("name", result.getKey());
        assertEquals("Поле может содержать только русские буквы", result.getValue());
    }

    @Test
    public void validateRequiredWithSpace() throws IOException {
        FieldComponent component = getFieldComponentFromFile();

        Map.Entry<String, String> result = ValidationUtil.validateRegExp("name", "русские буквы", component);
        assertNotNull(result);
        assertEquals("name", result.getKey());
        assertEquals("Поле может содержать только русские буквы", result.getValue());
    }

    @Test
    public void validateRequiredRussian() throws IOException {
        FieldComponent component = getFieldComponentFromFile();

        Map.Entry<String, String> result = ValidationUtil.validateRegExp("name", "РусскиеБуквы", component);
        assertNull(result);

    }

    private FieldComponent getFieldComponentFromFile() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("ValidationUtilValidateValidateRegExpTest.json");) {
            return objectMapper.readValue(
                    is,
                    FieldComponent.class
            );
        }
    }
}
