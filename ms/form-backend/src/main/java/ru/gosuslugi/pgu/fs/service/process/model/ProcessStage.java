package ru.gosuslugi.pgu.fs.service.process.model;

/**
 * Этап процесса
 */
public interface ProcessStage<T> {

    ProcessStageType getType();

    void execute(T process);
}
