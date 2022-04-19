package ru.gosuslugi.pgu.fs.common.jsonlogic.structure;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OperationNode implements Node {

    private final String operator;
    private final ArrayNode arguments;

    @Override
    public NodeType getType() {
        return NodeType.OPERATION;
    }

    public String getOperator() {
        return operator;
    }

    public ArrayNode getArguments() {
        return arguments;
    }
}
