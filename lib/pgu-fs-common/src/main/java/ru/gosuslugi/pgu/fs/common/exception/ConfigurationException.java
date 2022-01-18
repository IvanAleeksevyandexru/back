package ru.gosuslugi.pgu.fs.common.exception;

/**
 * Ошибки в конфигурации
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }
}
