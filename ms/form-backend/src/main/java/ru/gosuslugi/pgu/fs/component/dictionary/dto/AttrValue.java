package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gosuslugi.pgu.fs.utils.AttributeValueTypes;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttrValue {

    @JsonAlias("valueType")
    private AttributeValueTypes type;

    @JsonAlias("expr")
    @JsonDeserialize(using = AttrValueDeserializer.class)
    private List<String> value;

    /** Если type==REF то значение является списком с одним элементом.
     * Имеет смысл толькоя для значений типа REF! */
    public String asStringValue() {
        return value.get(0);
    }
}

