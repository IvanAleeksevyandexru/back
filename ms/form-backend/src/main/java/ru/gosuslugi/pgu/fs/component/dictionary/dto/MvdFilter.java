package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MvdFilter {
    //Дополнительное опциональное условие действия фильтра
    private String extraConditionExpr;
    private List<String> fiasList;
    private String value;

    public static MvdFilter EMPTY_FILTER = new MvdFilter(
            "",
            List.of(),
            ""
    );
}
