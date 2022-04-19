package ru.gosuslugi.pgu.components.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDto {
    @Singular
    private List<StateDto> states;

    /**
     * Значения которые будут сохранены в ScenarioDto после прохождения экрана
     */
    private Map<String, Object> storedValues;
}
