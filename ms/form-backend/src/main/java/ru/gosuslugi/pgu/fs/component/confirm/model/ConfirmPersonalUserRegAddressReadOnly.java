package ru.gosuslugi.pgu.fs.component.confirm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmPersonalUserRegAddressReadOnly {
    private FullAddress regAddr;
    private String regDate;
    private String error;
}
