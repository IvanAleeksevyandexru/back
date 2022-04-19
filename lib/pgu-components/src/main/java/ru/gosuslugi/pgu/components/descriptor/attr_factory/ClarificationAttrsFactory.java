package ru.gosuslugi.pgu.components.descriptor.attr_factory;

import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;

import java.util.List;
import java.util.Map;

/**
 * Фактория для вычесления референсов Clarification объектов
 */
@RequiredArgsConstructor
public class ClarificationAttrsFactory implements AttrsFactory {

    /**
     * Метод заглушка
     * @return null
     */
    @Override
    public Map<String, Object> getRefsMap() {
        return null;
    }

    /**
     * Метод заглушка
     * @return null
     */
    @Override
    public List<Placeholder> getPlaceholders() {
        return null;
    }
}
