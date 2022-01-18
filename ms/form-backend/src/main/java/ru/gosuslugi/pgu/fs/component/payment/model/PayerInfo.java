package ru.gosuslugi.pgu.fs.component.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayerInfo {
    private String idType;
    private String idNum;
}
