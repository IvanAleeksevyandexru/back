package ru.gosuslugi.pgu.fs.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PguBookingObjectDto {

    String name;
    String newValue;

    @EqualsAndHashCode.Exclude
    OffsetDateTime date = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    String title;
}
