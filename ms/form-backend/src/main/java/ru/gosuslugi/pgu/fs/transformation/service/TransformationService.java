package ru.gosuslugi.pgu.fs.transformation.service;

import ru.gosuslugi.pgu.dto.StatusInfo;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationBlock;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationRule;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;

import java.util.List;

public interface TransformationService {

    /**
     * Трансформация - атомарная: если хотябы одна из операций не проходим - делаем rollback
     *
     * @param transformationBlock правила трансформации для статуса
     * @param statusInfo                статус
     * @param origin                    опригиналный черновик
     * @return результат преобразования (проведена ли трансформация и преобразованный (или оригинальный) объект)
     */
    TransformationResult transform(
            TransformationBlock transformationBlock,
            StatusInfo statusInfo,
            Order order,
            DraftHolderDto origin
    );
}
