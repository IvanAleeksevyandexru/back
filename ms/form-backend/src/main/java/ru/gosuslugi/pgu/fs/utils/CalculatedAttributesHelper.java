package ru.gosuslugi.pgu.fs.utils;

import com.jayway.jsonpath.DocumentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.components.descriptor.placeholder.PlaceholderContext;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.AttrValue;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.gosuslugi.pgu.fs.utils.AttributeValueTypes.CALC;
import static ru.gosuslugi.pgu.fs.utils.AttributeValueTypes.REF;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalculatedAttributesHelper {
    private final ServiceIdVariable serviceIdVariable;
    private final ParseAttrValuesHelper parseAttrValuesHelper;
    private ExpressionParser expressionParser;
    private EvaluationContext context;

    private final ComponentReferenceService componentReferenceService;

    // Наименование атрибута, по которому производится фильтрации
    private static final String CALCULATIONS_ATTRIBUTE_NAME = "attributeName";
    // Атрибут с описанием типа получения значения для подстановки в фильтр
    private static final String CALCULATIONS_VALUE_TYPE_NAME = "valueType";
    /*
     * Атрибут, содержащий значение в соответствие с типом значения фильтра
     * для получения окончательного значения для подстановки в фильтр
     */
    private static final String CALCULATIONS_FIELD_VALUE_NAME = "value";

    // Название атрибута для передачи значения в фильтр
    private static final String EXPRESSION_ATTRIBUTE_NAME = "expr";

    private static final String DEFAULT_EXPRESSION_VALUE = "'null'";

    private static final String CALCULATION_VARIABLE_START = "$";

    private static final String CALCULATION_REG_EXP = "["+CALCULATION_VARIABLE_START+"](.*?)([=\\s+|&-)!]|$)";

    //This expression restricts use of function, escape symbols for bypass
    private static final String EXPRESSION_VALIDATION_REG_EXP = "[0-9A-Za-zА-Яа-яёЁ.\\s&|!$'\"=()?:\\-;,_{}\\[\\]\\\\+<>]+";

    /**
     * attributeName для ServiceId
     */
    private static final String SERVICE_ID_DICTIONARY_FILTER_VALUE = "serviceId";

    private static final String NOT_CORRECT_CONDITION_PARSING = "Невозможно сформировать условие фильтрации. Описание фильтра некорректно";
    private static final String NOT_CORRECT_CONDITION_TYPYING = "Указан неверный тип вложенных условий для вычисляемых значений";


    @PostConstruct
    public void postConstruct() {
        expressionParser = new SpelExpressionParser();
        context = SimpleEvaluationContext.forReadOnlyDataBinding().withInstanceMethods().build();
    }

    /**
     * Рассчитывает все атрибуты для SpEL выражений в указанном узле
     *
     * @param calculationsAttributeName узел рассчета
     * @param component                 компонент расчета
     * @param scenarioDto               сценарий
     * @return мапа с результатами расчета
     */
    public Map<String, Object> getAllCalculatedValues(String calculationsAttributeName, FieldComponent component, ScenarioDto scenarioDto) {
        @SuppressWarnings("unchecked") var attributesForCalc = (List<Map<String, Object>>) component.getAttrs().get(calculationsAttributeName);
        return getAllCalculatedValues(scenarioDto, component, attributesForCalc);
    }

    /**
     * Рассчитывает все атрибуты для SpEL выражений в указанном узле
     *
     * @param attributesForCalc узел рассчета
     * @param component         компонент расчета
     * @param scenarioDto       сценарий
     * @return мапа с результатами расчета
     */
    public Map<String, Object> getAllCalculatedValues(List<Map<String, Object>> attributesForCalc, FieldComponent component, ScenarioDto scenarioDto) {
        return getAllCalculatedValues(scenarioDto, component, attributesForCalc);
    }

    /**
     * Расчитывает указанный аттрибут указанного компонента
     * @param item узел в видет мапы
     * @param component компонент рассчета
     * @param scenarioDto сценарий
     * @param exprAttrName тег вырожения
     * @return возвращает результат SpEL в виде строки
     */
    public String getCalculatedValue(Map<String, Object> item, FieldComponent component, ScenarioDto scenarioDto, String exprAttrName) {
        if (!item.containsKey(exprAttrName)) {
            log.warn("Expression is missing for calc typed field in {} component", component.getId());
            return null;
        }
        if ("orderId".equals(item.get(exprAttrName)) && Objects.nonNull(scenarioDto.getOrderId())) return scenarioDto.getOrderId().toString();
        if ("uuid_v4".equals(item.get(exprAttrName))) return UUID.randomUUID().toString();

        String presetValue = "";
        if (item.get(exprAttrName) instanceof ArrayList) {
            for (Object calcExpression : (ArrayList<Object>)item.get(exprAttrName)) {
                if (!(calcExpression instanceof String)) {
                    log.error("Inner expression should have string type");
                    throw new FormBaseException(NOT_CORRECT_CONDITION_TYPYING);
                }
                String innerCalValue = processCalculationExpression(calcExpression.toString(), component, scenarioDto);
                if (innerCalValue!=null && !innerCalValue.equals("") && !innerCalValue.equals("null")) {
                    presetValue = innerCalValue;
                    break;
                }
            }
        } else {
            presetValue = processCalculationExpression(item.get(exprAttrName).toString(), component, scenarioDto);
        }
        return presetValue;
    }

    public String processCalculationExpression(String expression, FieldComponent component, ScenarioDto scenarioDto) {
        String result = "";
        log.info("Starting calculation filter parameter for component with id {}", component.getId());

        if (!Pattern.matches(EXPRESSION_VALIDATION_REG_EXP, expression)) {
            log.error("Неверное описание expression = {}", expression);
            throw new FormBaseException(NOT_CORRECT_CONDITION_PARSING);
        }

        Pattern pattern = Pattern.compile(CALCULATION_REG_EXP);
        Matcher matcher = pattern.matcher(expression);
        Set<String> usedVariables = new HashSet<>();
        while (matcher.find()) {
            usedVariables.add(matcher.group(1));
        }
        log.debug("{} variables were found in filter parameter for component {}", usedVariables.size(), component.getId());

        for (String v : usedVariables) {
            String varValue = parseAttrValuesHelper.getStringValueOfRefAttributeByFullPath(v, scenarioDto);

            // replaceAll съедает \
            // spel конвертирует "" в "
            String spelVarValue = DEFAULT_EXPRESSION_VALUE;
            if (StringUtils.hasText(varValue)) {
                // числа вставляем как есть, без кавычек
                if (NumberUtils.isParsable(varValue) && !varValue.startsWith("0")) {
                    // org.springframework.expression.spel.standard.Tokenizer (метод lexNumericLiteral)
                    // автоматически пытается преобразовать целое число в int если нет l или L на конце и выбрасывает ошибку
                    spelVarValue = NumberUtils.isDigits(varValue) ? varValue + 'L' : varValue;
                } else {
                    spelVarValue = "'" + varValue.replace("\"\"", "\"\"\"\"") + "'";
                }
            }
            expression = expression.replace(CALCULATION_VARIABLE_START + v, spelVarValue);
        }

        if (expressionParser == null) {
            log.warn("Expression parser is not initialized");
            return result;
        }
        try {
            log.debug("Running filter calculation expression {}...", expression);
            Expression springExpression = expressionParser.parseExpression(expression);
            result = Optional.ofNullable(springExpression.getValue(context)).orElse("").toString();
            log.debug("Filter calculation expression result is {} for component with id {}", result, component.getId());
        } catch (ParseException | EvaluationException e) {
            log.error("Exception во время парсинга/обработки expression = {}",expression);
            log.error("Exception info",e);
            throw new FormBaseException(NOT_CORRECT_CONDITION_PARSING);
        }
        return result;
    }

    public String getCalculatedValue(AttrValue attrValue, FieldComponent component, ScenarioDto scenarioDto) {
        for (String calcExpression : attrValue.getValue()) {
            String innerCalValue = processCalculationExpression(calcExpression, component, scenarioDto);
            if (innerCalValue != null && !innerCalValue.equals("") && !innerCalValue.equals("null")) {
                return innerCalValue;
            }
        }

        return "";
    }

    private Map<String, Object> getAllCalculatedValues(ScenarioDto scenarioDto, FieldComponent fieldComponent, List<Map<String, Object>> attributesForCalc) {
        var result = new HashMap<String, Object>();
        var isAttributesEmpty = CollectionUtils.isEmpty(fieldComponent.getAttrs());

        if (isAttributesEmpty || Objects.isNull(attributesForCalc)) {
            return result;
        }

        PlaceholderContext placeholderContext = null;
        DocumentContext[] documentContexts = null;

        for (Map<String, Object> item : attributesForCalc) {

            AttributeValueTypes type = null;
            var valueType = item.get(CALCULATIONS_VALUE_TYPE_NAME);
            try {
                if (item.containsKey(CALCULATIONS_VALUE_TYPE_NAME) && valueType != null) {
                    type = AttributeValueTypes.valueOf(valueType.toString().toUpperCase());
                }
            } catch (IllegalArgumentException ex) {
                log.warn("Illegal filter value type was found: {}", valueType);
                continue;
            }

            // TODO вероятно, для глобальных переменных нужен отдельный тип или preset
            if (SERVICE_ID_DICTIONARY_FILTER_VALUE.equalsIgnoreCase((String) item.get(CALCULATIONS_FIELD_VALUE_NAME))) {
                result.put(SERVICE_ID_DICTIONARY_FILTER_VALUE, serviceIdVariable.getValue(scenarioDto));
                continue;
            }

            if (type == CALC) {
                var calculated = getCalculatedValue(item, fieldComponent, scenarioDto, EXPRESSION_ATTRIBUTE_NAME);
                if (calculated != null) {
                    //setting calculated value
                    result.put(item.get(CALCULATIONS_ATTRIBUTE_NAME).toString(), calculated);
                    //changing type for frontend to get value
                    item.put(CALCULATIONS_VALUE_TYPE_NAME, AttributeValueTypes.PRESET.toString().toLowerCase());
                    item.put(CALCULATIONS_FIELD_VALUE_NAME, item.get(CALCULATIONS_ATTRIBUTE_NAME).toString());
                }
            }

            if (type == REF) {

                if (placeholderContext == null) {
                    placeholderContext = componentReferenceService.buildPlaceholderContext(fieldComponent, scenarioDto);
                    documentContexts = componentReferenceService.getContexts(scenarioDto);
                }

                var value = (String) item.get(CALCULATIONS_FIELD_VALUE_NAME);
                var resolvedValue = componentReferenceService.getValueByContext(
                    value, Function.identity(), placeholderContext, documentContexts);

                item.put(CALCULATIONS_FIELD_VALUE_NAME, resolvedValue);

            }

        }
        return result;
    }
}
