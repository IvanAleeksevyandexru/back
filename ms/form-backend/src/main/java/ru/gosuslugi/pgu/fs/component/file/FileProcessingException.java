package ru.gosuslugi.pgu.fs.component.file;

import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

public class FileProcessingException extends FormBaseException {

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
