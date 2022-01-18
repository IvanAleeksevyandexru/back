package ru.gosuslugi.pgu.fs.component.userdata.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MedicalItemsUnion {
    private String unionKind;
    private List<MedicalSimpleFilter> subs;
}
