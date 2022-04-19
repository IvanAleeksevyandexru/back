package ru.gosuslugi.pgu.components.descriptor.types;

import lombok.Data;
import ru.gosuslugi.pgu.dto.descriptor.CycledAttrs;

/**
 * All supported actions
 */
@Data
public class Action {
    private String label;
    private String value;
    private String type;
    private String action;
    private CycledAttrs attrs;
    private String pathToArray;
}
