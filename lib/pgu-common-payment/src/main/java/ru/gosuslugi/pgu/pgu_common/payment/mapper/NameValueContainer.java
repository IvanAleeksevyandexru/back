package ru.gosuslugi.pgu.pgu_common.payment.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NameValueContainer {
    private String name;
    private String value;
}
