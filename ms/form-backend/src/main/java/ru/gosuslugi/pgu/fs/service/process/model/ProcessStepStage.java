package ru.gosuslugi.pgu.fs.service.process.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Consumer;

/**
 * Шаг процесса
 */
@Data
@AllArgsConstructor
public class ProcessStepStage<T> implements ProcessStage<T> {

    private Consumer<T> function;

    @Override
    public ProcessStageType getType() {
        return ProcessStageType.STEP;
    }

    @Override
    public void execute(T process) {
        function.accept(process);
    }
}
