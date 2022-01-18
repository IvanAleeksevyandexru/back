package ru.gosuslugi.pgu.fs.common.exception;

/**
 * Ошибка сценария - не найдено экранов для перехода
 */
public class NoScreensFoundException extends ScenarioException {
    public NoScreensFoundException(String s) {
        super(s);
    }
}
