package ru.gosuslugi.pgu.components.descriptor.types;

import lombok.Data;

import java.util.Map;

@Data
public class StatesField {
    private Map<String, String> storedValues;
}
