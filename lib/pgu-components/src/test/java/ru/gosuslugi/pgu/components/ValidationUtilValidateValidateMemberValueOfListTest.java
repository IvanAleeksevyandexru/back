package ru.gosuslugi.pgu.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;

public class ValidationUtilValidateValidateMemberValueOfListTest {


    private static final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();


    @Test
    public void validateRequiredNoMembersNull() throws IOException {
        FieldComponent component = new FieldComponent();

        Map.Entry<String, String> result = ValidationUtil.validateMemberValueOfList("name", null, component, "error");
        assertNull(result);
    }

    @Test
    public void validateRequiredNoMembers() throws IOException {
        FieldComponent component = new FieldComponent();

        Map.Entry<String, String> result = ValidationUtil.validateMemberValueOfList("name", "", component, "error");
        assertNull(result);
    }

    @Test
    public void validateRequiredNull() throws IOException {
        FieldComponent component = getFieldComponentFromFile();

        Map.Entry<String, String> result = ValidationUtil.validateMemberValueOfList("name", null, component, "error");
        assertNull(result);
    }

    @Test
    public void validateRequiredNoMember() throws IOException {
        FieldComponent component = getFieldComponentFromFile();

        Map.Entry<String, String> result = ValidationUtil.validateMemberValueOfList("name", "1870", component, "error");
        assertNotNull(result);
        assertEquals("name", result.getKey());
        assertEquals("error",  result.getValue());
    }

    @Test
    public void validateRequiredMember() throws IOException {
        FieldComponent component = getFieldComponentFromFile();

        Map.Entry<String, String> result = ValidationUtil.validateMemberValueOfList("name", "2020", component, "error");
        assertNull(result);
    }

    private FieldComponent getFieldComponentFromFile() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("ValidationUtilValidateValidateMemberValueOfListTest.json");) {
            return objectMapper.readValue(
                    is,
                    FieldComponent.class
            );
        }
    }
}
