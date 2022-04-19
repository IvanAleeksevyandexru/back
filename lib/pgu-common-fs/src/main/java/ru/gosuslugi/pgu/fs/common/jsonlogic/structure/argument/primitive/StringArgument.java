package ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringArgument implements Primitive<String> {

    private final String value;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.STRING;
    }
}
