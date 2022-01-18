package ru.gosuslugi.pgu.fs.service.process.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Проверка условия
 */
@Data
@AllArgsConstructor
public class ProcessExecuteIfStage<T> implements ProcessStage<T> {

    private Boolean checkResult;
    private Predicate<T> predicate;
    private List<Consumer<T>> rightActions;
    private List<Consumer<T>> wrongActions;

    @Override
    public ProcessStageType getType() {
        return ProcessStageType.EXECUTE_IF;
    }

    @Override
    public void execute(T process) {
        boolean executeCheckResult = Objects.requireNonNullElseGet(checkResult, () -> predicate.test(process));
        List<Consumer<T>> actions = executeCheckResult ? rightActions : wrongActions;
        if (!CollectionUtils.isEmpty(actions)) {
            actions.forEach(it -> it.accept(process));
        }
    }
}