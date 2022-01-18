package ru.gosuslugi.pgu.fs.common.jsonlogic.structure;

import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive.Primitive;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive.PrimitiveType;

public class NullNode implements Primitive<Object> {

    public static final NullNode NULL = new NullNode();

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.NULL;
    }
}
