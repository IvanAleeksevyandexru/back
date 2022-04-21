package ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive;


import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.Node;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.NodeType;

public interface Primitive<T> extends Node {
    T getValue();
    PrimitiveType getPrimitiveType();

    @Override
    default NodeType getType() {
        return NodeType.PRIMITIVE_ARG;
    }

}
