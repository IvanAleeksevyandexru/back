package ru.gosuslugi.pgu.fs.pgu.dto.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Detail {

    @JsonProperty("addParams")
    private Params params;

}
