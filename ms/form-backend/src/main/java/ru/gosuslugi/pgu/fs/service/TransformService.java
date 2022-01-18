package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;

/**
 * https://jira.egovdev.ru/browse/EPGUCORE-45771
 * При ответе от службы проверяем необходимость трансформации
 */
public interface TransformService {

    /**
     * @param order order
     * @param serviceId service id
     * @param draftHolderDto draft dto
     * @return result - если трансформация была и новый объект
     */
    TransformationResult checkAndTransform(Order order, String serviceId, DraftHolderDto draftHolderDto);

    /**
     * @param id status code
     * @param serviceId service id
     * @return true - if it is accepted
     */
    boolean isAcceptedCode(Long id, String serviceId);
}
