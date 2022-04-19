package ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex;

import lombok.Data;

@Data
public class StringArgument implements Complex<String> {
    private String value;

    @Override
    public ComplexType getComplexType() {
        return ComplexType.String;
    }
}
