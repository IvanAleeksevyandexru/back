package ru.gosuslugi.pgu.kafkacli.props;

import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
public class IncludeExcludeProperties<T> {

    /**
     * Допустимые значения
     */
    Set<T> include = Collections.emptySet();

    /**
     * Исключаемые значения
     */
    Set<T> exclude = Collections.emptySet();

    public boolean includes(T value) {
        return (include.isEmpty() || include.contains(value))
            && (exclude.isEmpty() || !exclude.contains(value));
    }

}
