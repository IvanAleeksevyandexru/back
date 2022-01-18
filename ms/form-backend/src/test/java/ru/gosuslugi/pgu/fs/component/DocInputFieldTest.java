package ru.gosuslugi.pgu.fs.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.descriptor.types.DocInputField;
import ru.gosuslugi.pgu.fs.component.input.DocInputComponent;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DocInputFieldTest {

    private static final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();

    @Test
    public void loading() throws IOException {
        Map<String, DocInputField> docInputs = getDocInputField();
        assertNotNull(docInputs);
        assertFalse(docInputs.isEmpty());
        docInputs.values().forEach(docInputField -> assertNotNull(docInputField.getAttrs()));
    }

    @Test
    public void validateOk() throws IOException {

        String value = "{\"series\": \"122\"}";
        Map<String, DocInputField> fields = getDocInputField();

        Map<String, String> incorrectAnswers = DocInputComponent.validateFieldsByRegExp(new HashMap<>(), value, fields);
        assertTrue(incorrectAnswers.isEmpty());
    }

    @Test
    public void validateNull() throws IOException {

        String value = "{}";
        Map<String, DocInputField> fields = getDocInputField();

        Map<String, String> incorrectAnswers = DocInputComponent.validateFieldsByRegExp(new HashMap<>(), value, fields);
        assertTrue(incorrectAnswers.isEmpty());
    }

    @Test
    public void validateNonDigit() throws IOException {

        String value = "{\"series\": \"122 \"}";
        Map<String, DocInputField> fields = getDocInputField();

        Map<String, String> incorrectAnswers = DocInputComponent.validateFieldsByRegExp(new HashMap<>(), value, fields);
        assertEquals(incorrectAnswers.size(), 1);
        assertEquals(incorrectAnswers.get("series"), "Поле может содержать только цифры");
    }

    @Test
    public void validateEmpty() throws IOException {

        String value = "{\"series\": \"\"}";
        Map<String, DocInputField> fields = getDocInputField();

        Map<String, String> incorrectAnswers = DocInputComponent.validateFieldsByRegExp(new HashMap<>(), value, fields);
        assertEquals(incorrectAnswers.size(), 1);
        assertEquals(incorrectAnswers.get("series"), "Поле не может быть пустым");
    }

    @Test
    public void validateAlreadyExist() throws IOException {

        String value = "{\"series\": \"\"}";
        Map<String, DocInputField> fields = getDocInputField();

        HashMap<String, String> incorrectAnswers1 = new HashMap<>();
        incorrectAnswers1.put("series", "Existed error");
        Map<String, String> incorrectAnswers = DocInputComponent.validateFieldsByRegExp(incorrectAnswers1, value, fields);
        assertEquals(incorrectAnswers.size(), 1);
        assertEquals(incorrectAnswers.get("series"), "Existed error");
    }

    @Test
    public void validateNoRegExp() throws IOException {

        String value = "{\"series\": \"\"}";
        Map<String, DocInputField> fields = getDocInputFieldNoRegExp();

        HashMap<String, String> incorrectAnswers1 = new HashMap<>();
        Map<String, String> incorrectAnswers = DocInputComponent.validateFieldsByRegExp(incorrectAnswers1, value, fields);
        assertEquals(incorrectAnswers.size(), 0);
    }

    private Map<String, DocInputField> getDocInputField() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("DocInputFieldTest_with_RegExp.json");) {
            return objectMapper.readValue(
                is,
                    new TypeReference<>() {}
            );
        }
    }
    private Map<String, DocInputField> getDocInputFieldNoRegExp() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("DocInputFieldTest_without_RegExp.json");) {
            return objectMapper.readValue(
                    is,
                    new TypeReference<>() {}
            );
        }
    }
}
