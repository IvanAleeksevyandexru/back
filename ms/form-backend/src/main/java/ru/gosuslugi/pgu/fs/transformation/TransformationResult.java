package ru.gosuslugi.pgu.fs.transformation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;

@AllArgsConstructor
@Getter
public class TransformationResult {
    private final boolean transformed;
    private final DraftHolderDto draftHolderDto;
    private final Order order;
}
