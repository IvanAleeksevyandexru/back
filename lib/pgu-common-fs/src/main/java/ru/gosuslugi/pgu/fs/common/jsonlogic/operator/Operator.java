package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import ru.gosuslugi.pgu.fs.common.jsonlogic.Evaluator;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicException;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.ArrayNode;

public interface Operator {
    String key();

    Object evaluate(Evaluator evaluator, ArrayNode arguments) throws JsonLogicException;
}
