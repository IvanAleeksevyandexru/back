package ru.gosuslugi.pgu.fs.pgu.dto.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTokenResponseDto {
    long expires;
    String token;
}
