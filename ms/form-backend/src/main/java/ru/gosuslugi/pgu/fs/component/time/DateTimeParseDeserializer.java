package ru.gosuslugi.pgu.fs.component.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class DateTimeParseDeserializer extends StdDeserializer<LocalDateTime> {
    public DateTimeParseDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return p.getValueAsString().isEmpty() ? null : LocalDateTime.parse(p.getValueAsString());
    }
}
