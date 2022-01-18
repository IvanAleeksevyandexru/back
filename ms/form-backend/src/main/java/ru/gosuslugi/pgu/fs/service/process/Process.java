package ru.gosuslugi.pgu.fs.service.process;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Процесс
 * @param <T> Класс описания этапов процесса
 * @param <V> Результат выполения процесса
 */
public interface Process<T, V> {

    Process<T, V> execute(Consumer<T> function);

    Process<T, V> execute(List<Consumer<T>> functions);

    Process<T, V> executeIf(Boolean checkResult, Consumer<T> rightAction);

    Process<T, V> executeIf(Predicate<T> predicate, Consumer<T> rightAction);

    Process<T, V> executeIf(Predicate<T> predicate, List<Consumer<T>> rightActions);

    Process<T, V> executeIf(Predicate<T> predicate, Consumer<T> rightAction, Consumer<T> wrongAction);

    Process<T, V> executeIf(Predicate<T> predicate, List<Consumer<T>> rightActions, List<Consumer<T>> wrongActions);

    Process<T, V> completeIf(Predicate<T> predicate);

    Process<T, V> completeIf(Predicate<T> predicate, Consumer<T> modifier);

    Process<T, V> completeIf(Predicate<T> predicate, List<Consumer<T>> modifiers);

    /**
     * Выполнить процесс
     * @return Результат выполения процесса
     */
    V start();

    T getProcess();
}
