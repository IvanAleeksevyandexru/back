package ru.gosuslugi.pgu.pgu_common.payment.dto.pay;

import lombok.Data;
import ru.gosuslugi.pgu.pgu_common.payment.mapper.NameValueContainer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class PaymentRequisites {
    private NameValueContainer cbc;
    private NameValueContainer purpose;
    private NameValueContainer drawerStatus;
    private NameValueContainer paytReason;
    private NameValueContainer taxPeriod;
    private NameValueContainer docDate;
    private NameValueContainer taxPaytKind;
    private NameValueContainer docNumber;

    public List<NameValueContainer> getParams() {
        return List.of(cbc, purpose, drawerStatus, paytReason, taxPeriod, docDate, taxPaytKind, docNumber).stream()
                .filter(container -> Objects.nonNull(container.getValue()))
                .collect(Collectors.toList());
    }
}
