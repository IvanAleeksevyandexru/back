package ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NumberArgument implements Primitive<Double> {

    private final Number value;

    @Override
    public Double getValue() {
        return value.doubleValue();
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.NUMBER;
    }
}
