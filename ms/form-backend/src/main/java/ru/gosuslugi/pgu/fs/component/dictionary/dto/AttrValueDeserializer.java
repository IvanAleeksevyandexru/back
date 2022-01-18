package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttrValueDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node.isArray()) {
            var result = new ArrayList<String>();
            node.forEach(child -> result.add(child.textValue()));

            return Collections.unmodifiableList(result);
        }

        return List.of(node.textValue());
    }
}
