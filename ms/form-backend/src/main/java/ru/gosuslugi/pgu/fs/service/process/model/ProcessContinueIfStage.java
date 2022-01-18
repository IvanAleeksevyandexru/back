package ru.gosuslugi.pgu.fs.service.process.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.fs.service.process.impl.AbstractProcess;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Проверка необходимости продолжения процесса
 */
@Data
@AllArgsConstructor
public class ProcessContinueIfStage<T> implements ProcessStage<T> {

    private Boolean checkResult;
    private Predicate<T> predicate;
    private List<Consumer<T>> modifiers;

    @Override
    public ProcessStageType getType() {
        return ProcessStageType.CONTINUE_IF;
    }

    @Override
    public void execute(T process) {
        boolean continueCheckResult = Objects.requireNonNullElseGet(checkResult, () -> predicate.test(process));
        if (continueCheckResult) {
            if (!CollectionUtils.isEmpty(modifiers)) {
                modifiers.forEach(it -> it.accept(process));
            }
            ((AbstractProcess<?, ?>)process).setFinished();
        }
    }
}
