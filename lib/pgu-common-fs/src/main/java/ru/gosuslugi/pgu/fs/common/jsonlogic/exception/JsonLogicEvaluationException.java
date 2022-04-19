package ru.gosuslugi.pgu.fs.common.jsonlogic.exception;

public class JsonLogicEvaluationException extends RuntimeException {
    public JsonLogicEvaluationException(String message) {
        super(message);
    }

    public JsonLogicEvaluationException(Exception e) {
        super(e);
    }
}
