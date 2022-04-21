package ru.gosuslugi.pgu.fs.common.service;

import com.jayway.jsonpath.DocumentContext;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.AttrsFactory;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;
import ru.gosuslugi.pgu.components.descriptor.placeholder.PlaceholderContext;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.PlaceholderType;
import ru.gosuslugi.pgu.components.descriptor.placeholder.Reference;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ReferenceService {

    Pattern placeholderPattern = Pattern.compile("\\$\\{(.*?)}");

    /**
     * Получает значение по контексту плайсхолдера
     *
     * @param value            значение котрое нужно обработать согласно контексту
     * @param context          контекст
     * @param documentContexts контексты для поиска значений рефов
     * @return получаемое значение
     */
    default String getValueByContext(String value, Function<String, String> converter, PlaceholderContext context, DocumentContext... documentContexts) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }

        final Matcher m = placeholderPattern.matcher(value);
        while (m.find()) {
            String key = m.group(1);
            String replaceValue;
            if (context.getPlaceholderMap().containsKey(key)) {
                Placeholder placeholder = context.getPlaceholderMap().get(key);
                replaceValue = placeholder.getNext(documentContexts);
                if (PlaceholderType.simple.equals(placeholder.getType())) {
                    replaceValue = getValueByContext(replaceValue, Function.identity(), context, documentContexts);
                }
            } else { // Пробуем проверить реф напрямую
                Reference ref = new Reference();
                ref.setPath(key);
                replaceValue = ref.getNext(documentContexts);
                if (key.equals(replaceValue)) {
                    continue;
                }
            }

            String expression = m.group();
            value = value.replace(expression, converter.apply(replaceValue));
        }

        return value;
    }

    /**
     * Создается контекст плайсхолдеров
     *
     * @param attrsFactory фабрика атрибутов компонента
     * @return контекст
     */
    default PlaceholderContext buildPlaceholderContext(AttrsFactory attrsFactory, Map<String, Object> additionalAttrs) {
        Map<String, Placeholder> paramsMap = new HashMap<>();
        attrsFactory.getReferences(additionalAttrs).forEach(it -> paramsMap.put(it.getKey(), it));
        attrsFactory.getPlaceholders().forEach(it -> paramsMap.put(it.getKey(), it));
        return PlaceholderContext.builder().placeholderMap(paramsMap).build();
    }
}
