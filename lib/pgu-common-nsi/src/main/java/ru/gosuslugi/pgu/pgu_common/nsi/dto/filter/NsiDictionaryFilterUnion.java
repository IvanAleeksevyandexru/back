package ru.gosuslugi.pgu.pgu_common.nsi.dto.filter;

import lombok.Data;

import java.util.List;

@Data
public class NsiDictionaryFilterUnion {

    private NsiDictionaryUnionType unionKind;
    private List<NsiDictionaryFilter> subs;
}