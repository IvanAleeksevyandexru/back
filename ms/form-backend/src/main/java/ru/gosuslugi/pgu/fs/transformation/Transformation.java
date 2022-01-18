package ru.gosuslugi.pgu.fs.transformation;

import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOperation;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;

import java.util.Map;

public interface Transformation {

    /**
     * @return код операции
     */
    TransformationOperation getOperation();

    /**
     * Трансформирует черновик
     * @param current черновик
     * @param current черновик
     * @param spec конфигурация преобразования
     * @return результат преобразования (проведена ли трансформация и преобразованный (или оригинальный) объект)
     */
    TransformationResult transform(DraftHolderDto current, Order order, Map<String, Object> spec);
}
