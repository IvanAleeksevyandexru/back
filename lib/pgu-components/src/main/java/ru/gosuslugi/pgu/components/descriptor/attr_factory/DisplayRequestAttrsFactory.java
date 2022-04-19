package ru.gosuslugi.pgu.components.descriptor.attr_factory;

import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Создает сущности из атрибутов, прописанных в экране
 */
@RequiredArgsConstructor
public class DisplayRequestAttrsFactory implements AttrsFactory {

    private final Map<String, Object> attrs;

    @Override
    public Map<String, Object> getRefsMap() {
        return Objects.nonNull(attrs) ? (Map<String, Object>) attrs.computeIfAbsent(REFS_ATTR, key -> new HashMap<>()) : new HashMap<>();
    }

    @Override
    public List<Placeholder> getPlaceholders() {
        var extraAttrsMap = Optional.ofNullable(attrs).orElse(new HashMap<>()).get("placeholders");
        if (extraAttrsMap instanceof Map) {
            return getPlaceholders((Map<String, String>) extraAttrsMap);
        }

        return Collections.emptyList();
    }
}
