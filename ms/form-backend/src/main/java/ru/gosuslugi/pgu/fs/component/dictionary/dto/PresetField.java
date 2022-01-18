package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresetField {
    private String fieldName;
    private String label;
    private String type;
}
