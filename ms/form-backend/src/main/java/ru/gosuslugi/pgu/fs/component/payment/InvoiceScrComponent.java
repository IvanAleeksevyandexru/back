package ru.gosuslugi.pgu.fs.component.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;

import static ru.gosuslugi.pgu.components.ComponentAttributes.BILL_ID_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.BILL_NUMBER_ATTR;

@Component
@RequiredArgsConstructor
public class InvoiceScrComponent extends AbstractComponent<String> {

    @Override
    public ComponentType getType() {
        return ComponentType.InvoiceScr;
    }

    @Override
    protected void preProcess(FieldComponent component) {
        component.getAttrs().put(BILL_ID_ATTR, component.getArgument(BILL_ID_ATTR));
        component.getAttrs().put(BILL_NUMBER_ATTR, component.getArgument(BILL_NUMBER_ATTR));
    }
}