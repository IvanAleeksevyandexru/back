package ru.gosuslugi.pgu.components.descriptor.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SpelConverter implements Converter {

    private final static String EXPRESSION_ATTR = "expression";
    private final static String REF_VALUE_TYPE = "refValueType";
    private final static String RESULT_TYPE_ATTR = "resultType";
    private final static String RESULT_CLASS_ATTR = "resultClass";

    @Override
    public Object convert(Object value, Map<String, Object> attrs) {
        if (attrs.containsKey(EXPRESSION_ATTR)) {
            String expression = (String) attrs.get(EXPRESSION_ATTR);
            if (StringUtils.isEmpty(expression)) {
                return value;
            }

            Optional<Class<?>> refValueClass = getRefValueClassByType((String) attrs.get(REF_VALUE_TYPE));
            if (refValueClass.isPresent()) {
                value = JsonProcessingUtil.fromJson((String)value, refValueClass.get());
            }

            Class<?> resultClass;
            if (attrs.containsKey(RESULT_CLASS_ATTR)) {
                resultClass = (Class<?>) attrs.get(RESULT_CLASS_ATTR);
            } else {
                resultClass = getResultClassByType((String) attrs.get(RESULT_TYPE_ATTR));
            }

            SpelExpressionParser expressionParser = new SpelExpressionParser();
            return expressionParser.parseExpression(expression).getValue(value, resultClass);
        }

        return value;
    }

    private Optional<Class<?>> getRefValueClassByType(String type) {
        return Optional.ofNullable(type).map(t -> ResultType.valueOf(t).className);
    }

    private Class<?> getResultClassByType(String type) {
        if (type == null) {
            return String.class;
        }

        return ResultType.valueOf(type).className;
    }

    private enum ResultType {
        String(String.class),
        Integer(Integer.class),
        Boolean(Boolean.class),
        List(List.class),
        Map(Map.class);

        Class<?> className;

        ResultType(Class<?> className) {
            this.className = className;
        }
    }
}
