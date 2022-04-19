package ru.gosuslugi.pgu.components.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Класс предназначен для передачи ошибок валидации компонентов на фронт с иконкой, заголовком и описанием.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDto {
    /**
     * Заголовок
     */
    private String label;

    /**
     * Значение
     */
    private String value;

    /**
     * Нужно ли форматировать числовое поле. Например значие '1000' преобразуется в '1 000'
     */
    private Boolean rank;

    /**
     * Обязательность заполнения поля
     */
    @JsonIgnore
    private boolean required;

    /**
     * Дополнительные атрибуты поля
     */
    @JsonIgnore
    private Map<String, Object> attrs;

    public FieldDto(String label, String value) {
        this.label = label;
        this.value = value;
    }
}
