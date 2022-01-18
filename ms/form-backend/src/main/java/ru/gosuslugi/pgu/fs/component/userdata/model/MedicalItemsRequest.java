package ru.gosuslugi.pgu.fs.component.userdata.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MedicalItemsRequest {
    private String eserviceId;
    private String userSelectedRegion;
    private String refName;
    private String parentRefItemValue;
    private List<ParamsItem> params;
    private String treeFiltering;
    private String[] selectAttributes;
    private MedicalItemsFilter filter;
    private Integer pageSize;
    private Integer pageNum;

}
