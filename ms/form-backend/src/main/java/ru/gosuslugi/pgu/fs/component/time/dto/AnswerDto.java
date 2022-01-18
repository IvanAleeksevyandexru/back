package ru.gosuslugi.pgu.fs.component.time.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import ru.gosuslugi.pgu.fs.component.time.DateParseDeserializer;
import ru.gosuslugi.pgu.fs.component.time.DateTimeParseDeserializer;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AnswerDto {
    @JsonDeserialize(using = DateTimeParseDeserializer.class)
    private LocalDateTime endDateTime;
    @JsonDeserialize(using = DateTimeParseDeserializer.class)
    private LocalDateTime startDateTime;
    @JsonDeserialize(using = DateParseDeserializer.class)
    private LocalDate endDate;
    @JsonDeserialize(using = DateParseDeserializer.class)
    private LocalDate startDate;
}
