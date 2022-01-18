package ru.gosuslugi.pgu.fs.service.process.impl;

import ru.gosuslugi.pgu.common.core.exception.PguException;
import ru.gosuslugi.pgu.fs.service.process.Process;
import ru.gosuslugi.pgu.fs.service.process.model.ProcessContinueIfStage;
import ru.gosuslugi.pgu.fs.service.process.model.ProcessExecuteIfStage;
import ru.gosuslugi.pgu.fs.service.process.model.ProcessStage;
import ru.gosuslugi.pgu.fs.service.process.model.ProcessStepStage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Абстрактный процесс
 * @param <T> Класс задающий методы для выполнения этапов процесса
 * @param <V> Результат выполения процесса
 */
public abstract class AbstractProcess<T, V> implements Process<T, V> {
    /** Результат выполения процесса */
    protected V response;
    /** Признак завершения процесса */
    protected Boolean isFinished = false;
    /** Шаги выполнения процесса */
    protected List<ProcessStage<T>> stages = new ArrayList<>();
    /** Callback при возникновении {@link PguException} */
    protected Consumer<V> onPguErrorCallback;

    protected void init() {
        response = null;
        isFinished = false;
        stages = new ArrayList<>();
    }

    /**
     * Шаг выполнения процесса
     * @param function Фунукция для выполнеия шага
     * @return Процесс
     */
    @Override
    public Process<T, V> execute(Consumer<T> function) {
        stages.add(new ProcessStepStage<>(function));
        return this;
    }

    @Override
    public AbstractProcess<T, V> execute(List<Consumer<T>> functions) {
        functions.forEach(it -> stages.add(new ProcessStepStage<>(it)));
        return this;
    }

    /**
     * Выполнить при выполнении условия
     * @param predicate     Условие
     * @param rightAction Выполнить при выполнении условия
     * @return              Процесс
     */
    @Override
    public Process<T, V> executeIf(Predicate<T> predicate, Consumer<T> rightAction) {
        return executeIf(predicate, rightAction, null);
    }

    @Override
    public Process<T, V> executeIf(Predicate<T> predicate, List<Consumer<T>> rightActions) {
        return executeIf(predicate, rightActions, null);
    }

    @Override
    public Process<T, V> executeIf(Predicate<T> predicate, Consumer<T> rightAction, Consumer<T> wrongActions) {
        stages.add(new ProcessExecuteIfStage<>(null, predicate, rightAction == null ? null : List.of(rightAction), wrongActions == null ? null : List.of(wrongActions)));
        return this;
    }

    @Override
    public Process<T, V> executeIf(Predicate<T> predicate, List<Consumer<T>> rightActions, List<Consumer<T>> wrongActions) {
        stages.add(new ProcessExecuteIfStage<>(null, predicate, rightActions, wrongActions));
        return this;
    }

    @Override
    public Process<T, V> executeIf(Boolean checkResult, Consumer<T> rightAction) {
        stages.add(new ProcessExecuteIfStage<>(checkResult, null, rightAction == null ? null : List.of(rightAction), null));
        return this;
    }

    /**
     * Завершить процесс при выполнении заданного условия
     * @param predicate Предикат
     * @return          Процесс
     */
    @Override
    public Process<T, V> completeIf(Predicate<T> predicate) {
        return completeIf(predicate, List.of());
    }

    @Override
    public Process<T, V> completeIf(Predicate<T> predicate, Consumer<T> modifier) {
        stages.add(new ProcessContinueIfStage<>(null, predicate, modifier == null ? null : List.of(modifier)));
        return this;
    }

    @Override
    public Process<T, V> completeIf(Predicate<T> predicate, List<Consumer<T>> modifiers) {
        stages.add(new ProcessContinueIfStage<>(null, predicate, modifiers));
        return this;
    }

    public void setFinished() {
        isFinished = true;
    }

    /**
     * Выполнить процесс
     * @return Результат выполения процесса
     */
    @Override
    public V start() {
        for (ProcessStage<T> stage : stages) {
            try {
                stage.execute(getProcess());
                if (this.isFinished) {
                    return response;
                }
            } catch (PguException e) {
                if (onPguErrorCallback != null) {
                    onPguErrorCallback.accept(response);
                }
                throw e;
            }
        }

        return response;
    }
}
