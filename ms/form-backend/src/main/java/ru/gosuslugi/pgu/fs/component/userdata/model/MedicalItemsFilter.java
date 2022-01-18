package ru.gosuslugi.pgu.fs.component.userdata.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicalItemsFilter {
    private MedicalItemsUnion union;
}
