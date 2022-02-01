package ru.gosuslugi.pgu.fs.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.descriptor.types.DocInputField;
import ru.gosuslugi.pgu.components.descriptor.types.StatesField;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class ConfirmPersonalUserDataComponentTest {

    private static final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();

    @Test
    public void loading() throws IOException {
        List<DocInputField> docs = getFields();
        Map<String, DocInputField> docInputs = docs.stream().collect(HashMap::new, (m, v)-> m.put(v.getLabel(), v), HashMap::putAll);
        assertNotNull(docInputs);
    }

    private List<DocInputField> getFields() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("ConfirmPersonalUserDataComponent.json");) {
            return objectMapper.readValue(
                    is,
                    new TypeReference<>() {}
            );
        }
    }

    private StatesField getData() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("States.json");) {
            return objectMapper.readValue(
                    is,
                    StatesField.class
            );
        }
    }

    @Test
    public void convertMap() throws IOException {
        StatesField field = getData();
        assertNotNull(field);
    }
}
