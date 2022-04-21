package ru.gosuslugi.pgu.fs.common.jsonlogic.exception;

public class JsonLogicParseException extends RuntimeException {
    public JsonLogicParseException(Throwable cause) {
        super(cause);
    }

    public JsonLogicParseException(String message) {
        super(message);
    }
}
