package ru.gosuslugi.pgu.fs.common.exception;

/**
 * Ошибка в сценарии - найдено несколько экранов для перехода
 */
public class MultipleScreensFoundException extends ScenarioException {
    public MultipleScreensFoundException(String s) {
        super(s);
    }
}
