package ru.gosuslugi.pgu.components.descriptor.types;

import lombok.Data;

@Data
public class FullAddressCheckboxes {
    private boolean streetCheckbox;
    private boolean houseCheckbox;
    private boolean apartmentCheckbox;
}
