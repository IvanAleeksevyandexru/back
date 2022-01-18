package ru.gosuslugi.pgu.fs.component;

import lombok.*;
import ru.gosuslugi.pgu.components.dto.ErrorDto;
import ru.gosuslugi.pgu.components.dto.StateDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDto<T> {

    @Singular
    private List<StateDto> states;

    /**
     * Значения которые будут сохранены в ScenarioDto после прохождения экрана
     */
    private T storedValues;

    /** Информация об ошибке */
    @Singular
    private List<ErrorDto> errors;
}