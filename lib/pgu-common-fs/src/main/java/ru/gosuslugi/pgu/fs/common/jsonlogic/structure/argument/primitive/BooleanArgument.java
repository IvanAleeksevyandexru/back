package ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BooleanArgument implements Primitive<Boolean> {

    public static final BooleanArgument TRUE = new BooleanArgument(true);
    public static final BooleanArgument FALSE = new BooleanArgument(false);

    private final boolean value;

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.BOOLEAN;
    }
}
