package ru.gosuslugi.pgu.components.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Информация об ошибке
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {

    public enum TYPE {
        ERROR("error","red-line"),
        WARN("warn","yellow-line");

        final String type;
        final String icon;

        TYPE(String type, String icon) {
            this.type = type;
            this.icon = icon;
        }
    }

    private String icon;
    private String type;
    private String title;
    private String desc;
    private List<String> fields;

    public ErrorDto(ErrorDto.TYPE type, String title, String desc, List<String> fields) {
        this.icon = type.icon;
        this.type = type.type;
        this.title = title;
        this.desc = desc;
        this.fields = fields;
    }

    public ErrorDto(String title, String desc) {
        this(TYPE.ERROR, title, desc, null);
    }
}
