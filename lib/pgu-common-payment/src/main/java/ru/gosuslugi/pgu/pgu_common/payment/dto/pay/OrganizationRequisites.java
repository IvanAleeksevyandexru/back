package ru.gosuslugi.pgu.pgu_common.payment.dto.pay;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gosuslugi.pgu.pgu_common.payment.mapper.NameValueContainer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
public class OrganizationRequisites {
    private NameValueContainer name;
    private NameValueContainer payeeINN;
    private NameValueContainer kpp;
    private NameValueContainer bic;
    private NameValueContainer bankName;
    private NameValueContainer personalAcc;
    private NameValueContainer oktmo;
    private NameValueContainer corr;

    public List<NameValueContainer> getParams() {
        return Stream.of(name, payeeINN, kpp, bic, bankName, personalAcc, oktmo, corr)
                .filter(container -> Objects.nonNull(container.getValue()))
                .collect(Collectors.toList());
    }
}
