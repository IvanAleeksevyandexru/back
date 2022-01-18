package ru.gosuslugi.pgu.fs.transformation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOperation;
import ru.gosuslugi.pgu.fs.common.exception.ConfigurationException;
import ru.gosuslugi.pgu.fs.transformation.Transformation;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.*;

@Slf4j
@Component
public class TransformationRegistry {

    private final ConcurrentHashMap<TransformationOperation, Transformation> registry = new ConcurrentHashMap<>();

    @Autowired
    public void setTransformations(List<Transformation> actions) {
        actions.forEach(this::register);
    }

    private void register(Transformation transformation) {
        TransformationOperation operation = transformation.getOperation();
        Transformation oldOperation = registry.putIfAbsent(operation, transformation);
        if(Objects.nonNull(oldOperation)){
            String errorMessage = String.format(
                    "Обработчик %s для операции %s уже зарегистрирован: %s",
                    transformation.getClass().getCanonicalName(),
                    operation,
                    oldOperation.getClass().getCanonicalName());
            error(log, () -> errorMessage);
            throw new ConfigurationException(errorMessage);
        }
    }

    public Transformation getTransformation(TransformationOperation operation){
        return registry.get(operation);
    }
}
