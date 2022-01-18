package ru.gosuslugi.pgu.fs.common.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.AttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.DisplayRequestAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.placeholder.Reference;
import ru.gosuslugi.pgu.components.descriptor.placeholder.ReferenceAttrs;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.CycledAttrs;
import ru.gosuslugi.pgu.dto.descriptor.Expression;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.FunctionDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;
import ru.gosuslugi.pgu.dto.util.DraftUtil;
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogic;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService;
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService;
import ru.gosuslugi.pgu.fs.common.service.functions.ContextFunctionService;
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry;
import ru.gosuslugi.pgu.fs.common.variable.VariableType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static ru.gosuslugi.pgu.components.descriptor.placeholder.Reference.FIELD_ID_INDEX_IN_PATH;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedValuesServiceImpl implements LinkedValuesService {

    public static final String ONE_DOT_REGEXP = "\\.";
    public static final int LV_VERSION_2 = 2;
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9_.?@'()=\\[\\]\\s]+)}");

    private final JsonProcessingService jsonProcessingService;
    private final JsonLogic jsonLogic;
    private final ContextFunctionService contextFunctionService;
    private final VariableRegistry variableRegistry;
    private final ProtectedFieldService protectedFieldService;
    private final DefinitionResolver definitionResolver;

    @Override
    public void fillLinkedValues(FieldComponent fieldComponent, ScenarioDto scenarioDto, DocumentContext... externalContexts) {
        FieldComponentAttrsFactory componentAttrsFactory = new FieldComponentAttrsFactory(fieldComponent);
        if (fieldComponent.getLinkedValues() == null) return;
        for (LinkedValue linkedValue: fieldComponent.getLinkedValues()) {
            if (LV_VERSION_2 == linkedValue.getVersion()) {
                val value = definitionResolver.compute(linkedValue, scenarioDto);
                fieldComponent.addArgument(linkedValue.getArgument(), String.valueOf(value));
            } else {
                val value = getValue(linkedValue, scenarioDto, componentAttrsFactory, externalContexts);
                if (value != null) fieldComponent.addArgument(linkedValue.getArgument(), value);
            }
        }
    }

    @Override
    public void fillLinkedValues(DisplayRequest displayRequest, ScenarioDto scenarioDto) {
        DisplayRequestAttrsFactory componentAttrsFactory = new DisplayRequestAttrsFactory(displayRequest.getAttrs());
        if (displayRequest.getLinkedValues() == null) return;
        for (LinkedValue linkedValue: displayRequest.getLinkedValues()) {
            val value = getValue(linkedValue, scenarioDto, componentAttrsFactory);
            if (value != null) displayRequest.addArgument(linkedValue.getArgument(), value);
        }
    }

    /**
     * Возвращает значение конкретного linkedValue
     *
     * @param linkedValue  - выражение
     * @param scenarioDto  - сценарий
     * @param attrsFactory - фактория для обработки реф ссылок
     * @return вычисленное значение
     */
    public String getValue(LinkedValue linkedValue, ScenarioDto scenarioDto, AttrsFactory attrsFactory , DocumentContext... externalContexts) {
        Reference reference = attrsFactory.getReference(linkedValue);
        linkedValue.setReference(reference);

        FunctionDescriptor functionDescriptor = linkedValue.getFunction();
        if (nonNull(functionDescriptor)) {
            String value = calculateValueByFunction(functionDescriptor, scenarioDto);
            return convertValue(linkedValue, value);
        }

        if (nonNull(linkedValue.getJsonLogic())) {
            String value = String.valueOf(jsonLogic.calculate(linkedValue.getJsonLogic(), scenarioDto));
            return convertValue(linkedValue, value);
        }

        if (isActiveConditionsFail(linkedValue, scenarioDto)) {
            return null;
        }

        if (StringUtils.hasText(linkedValue.getJsonPath())) {
            resolvePlaceholders(scenarioDto, linkedValue);
            String value = getValueByJsonPath(scenarioDto, linkedValue);
            value = convertValue(linkedValue, value);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }

        if (linkedValue.isJsonSource()) {
            String value = getValueFromJson(scenarioDto, linkedValue);
            value = convertValue(linkedValue, value);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        ApplicantAnswer applicantAnswer = getApplicationAnswer(scenarioDto, linkedValue, linkedValue.getReference().getPath(), externalContexts);
        if (applicantAnswer != null) {
            String convertedArgumentValue = convertValue(linkedValue, applicantAnswer.getValue());
            return linkedValue.evaluateValue(convertedArgumentValue);
        }

        VariableType variableType = VariableType.getInstance(Objects.toString(linkedValue.getSource()));
        if (nonNull(variableType)) {
            return variableRegistry.getVariable(variableType).getValue(scenarioDto);
        }

        Object protectedField = protectedFieldService.getValue(Objects.toString(linkedValue.getSource()));
        if (nonNull(protectedField)) {
            return String.valueOf(protectedField);
        }

        return convertValue(linkedValue, linkedValue.getDefaultValue());
    }

    private String calculateValueByFunction(FunctionDescriptor functionDescriptor, ScenarioDto scenarioDto) {
        String functionName = functionDescriptor.getName();
        List<String> args = resolveLinksToValues(functionDescriptor.getArgs(), scenarioDto);

        return contextFunctionService.applyFunction(functionName, args);
    }

    private List<String> resolveLinksToValues(List<String> args, ScenarioDto scenarioDto) {
        var convertedArgs = new ArrayList<>(args);

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (isArgLinkToApplicantAnswer(arg)) {
                String linkToValue = arg.substring(1);
                String valueByAnswerLink = DraftUtil.getValueByLink(scenarioDto, linkToValue);

                convertedArgs.set(i, valueByAnswerLink);
            }
        }

        return convertedArgs;
    }

    private boolean isArgLinkToApplicantAnswer(String arg) {
        return arg.startsWith("$") && arg.contains(".");
    }

    private String getValueFromJson(ScenarioDto scenarioDto, LinkedValue componentValue) {
        String[] fields = componentValue.getReference().getPath().split(ONE_DOT_REGEXP);
        if (fields.length == 0) return null;
        ApplicantAnswer answer = getApplicationAnswer(scenarioDto, componentValue, fields[FIELD_ID_INDEX_IN_PATH]);
        if (isNull(answer)) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String answerValue = answer.getValue();
            JsonParser parser = mapper.createParser(answerValue);
            JsonNode actualObj = mapper.readTree(parser);
            JsonNode currentPath = actualObj.findValue(fields[1]);
            if (isNull(currentPath)) return componentValue.getDefaultValue();
            for (int i = 2; i < fields.length; i++) {
                currentPath = currentPath.path(fields[i]);
            }
            if (isNull(currentPath)) return componentValue.getDefaultValue();
            if (currentPath instanceof NumericNode) {
                return componentValue.evaluateValue(currentPath.toString());
            }
            String jsonPathValue = currentPath.toString();
            jsonPathValue = jsonPathValue.startsWith("{") || jsonPathValue.startsWith("[") ? jsonPathValue : jsonPathValue.substring(1, jsonPathValue.length() - 1);
            return componentValue.evaluateValue(jsonPathValue);
        } catch (IOException e) {
            throw new JsonParsingException(e.getMessage(), e.getCause());
        }
    }

    private String getValueByJsonPath(ScenarioDto scenarioDto, LinkedValue linkedValue) {
        String[] splitPath = linkedValue.getJsonPath().split(ONE_DOT_REGEXP);
        if (splitPath.length == 0) {
            return linkedValue.getDefaultValue();
        }
        String appAnswerComponentId = splitPath[0];
        String jsonPath = appAnswerComponentId;
        if (splitPath.length > 1) {
            jsonPath = linkedValue.getJsonPath().substring(appAnswerComponentId.length() + 1);
        }

        ApplicantAnswer applicantAnswer = getApplicationAnswer(scenarioDto, linkedValue, appAnswerComponentId);
        if (isNull(applicantAnswer)) {
            return linkedValue.getDefaultValue();
        }
        String answerValue = applicantAnswer.getValue();
        String result;
        try {
            result = Objects.toString(JsonPath.read(answerValue, "$." + jsonPath));
        } catch (PathNotFoundException ex) {
            return linkedValue.getDefaultValue();
        }
        return result;
    }

    private void resolvePlaceholders(ScenarioDto scenarioDto, LinkedValue linkedValue) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(linkedValue.getJsonPath());
        List<PlaceholderHolder> holders = new ArrayList<>();

        while (matcher.find()) {
            holders.add(PlaceholderHolder.of(matcher.group(0), matcher.group(1)));
        }

        if (holders.isEmpty()) return;

        String result = null;
        LinkedValue tempLinkedValue = new LinkedValue();
        for (PlaceholderHolder placeholder : holders) {
            tempLinkedValue.setJsonPath(placeholder.getValue());
            result = Objects.requireNonNullElse(result, linkedValue.getJsonPath())
                    .replace(placeholder.getWrapped(), getValueByJsonPath(scenarioDto, tempLinkedValue));
        }
        linkedValue.setJsonPath(result);
    }

    /**
     * Ищет нужный ответ для подстановки значения в {@code source} аргумента. Если заданы цикличные настройки,
     * ищет среди цикличных ответов
     *
     * @param scenarioDto DTO
     * @param linkedValue linked value
     * @param field поле
     * @param externalContexts внешние контексты
     * @return найденный ответ
     */
    private ApplicantAnswer getApplicationAnswer(ScenarioDto scenarioDto, LinkedValue linkedValue, String field, DocumentContext... externalContexts) {
        CycledAttrs cycledAttrs = linkedValue.getAttrs();

        // Если цикличный ответ
        if(cycledAttrs != null && !StringUtils.isEmpty(cycledAttrs.getCycledAnswerId()) && !StringUtils.isEmpty(cycledAttrs.getCycledAnswerIndex())) {
            List<DocumentContext>  documentContexts = new ArrayList<>();
            Stream.of(externalContexts).forEach(documentContexts::add);
            documentContexts.add(JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue())));
            documentContexts.add(JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers())));

            Reference reference = new Reference();
            reference.setPath(cycledAttrs.getCycledAnswerIndex());
            String indexValue = reference.getNext(documentContexts.toArray(new DocumentContext[] {}));

            var cycledApplicantAnswersOptional = Optional.ofNullable(scenarioDto.getCycledApplicantAnswers());
            var answerItemOptional = cycledApplicantAnswersOptional
                    .map(answers -> answers.getAnswerOrDefault(cycledAttrs.getCycledAnswerId(), null))
                    .map(answer -> answer.getItemOrDefault(indexValue, null));

            if (answerItemOptional.isPresent()) {
                return answerItemOptional.get().getItemAnswers().get(field);
            }
        }

        return DraftUtil.getApplicantAnswer(scenarioDto, field);
    }

    private boolean isActiveConditionsFail(LinkedValue linkedValue, ScenarioDto scenarioDto) {
        if (!linkedValue.hasActiveConditions()) {
            return false;
        }
        final Map<String, ApplicantAnswer> applicantAnswers = scenarioDto.getApplicantAnswers();
        for (Expression expression : linkedValue.getActiveOnly()) {
            if (!applicantAnswers.containsKey(expression.getField())) {
                return true;
            }
            ApplicantAnswer answer = applicantAnswers.get(expression.getField());
            if (!expression.getWhen().equals(answer.getValue())) {
                return true;
            }
        }
        return false;
    }

    private String convertValue(LinkedValue linkedValue, String value) {
        Placeholder reference = linkedValue.getReference();
        if (reference instanceof Reference) {
            ReferenceAttrs referenceAttrs = ((Reference) reference).getAttrs();
            if (referenceAttrs != null) {
                return (String) referenceAttrs.getConverterType().getConverter().convert(value, referenceAttrs.getAttrs());
            }
        }
        return value;
    }
}
