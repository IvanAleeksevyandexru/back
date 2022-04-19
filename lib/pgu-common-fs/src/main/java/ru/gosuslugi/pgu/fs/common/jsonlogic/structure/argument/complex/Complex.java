package ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex;

import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.Node;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.NodeType;

public interface Complex<T> extends Node {
    T getValue();
    ComplexType getComplexType();

    @Override
    default NodeType getType() {
        return NodeType.COMPLEX_ARG;
    }

}
