package ru.gosuslugi.pgu.fs.component.esep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Информация о подписании заявления ЭЦП (компонент EsepSign)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsepSignComponentDto {
    /** Ссылка на форму подписания */
    private String url;
    /** Заявление уже было подписано */
    private Boolean alreadySigned = false;
}
