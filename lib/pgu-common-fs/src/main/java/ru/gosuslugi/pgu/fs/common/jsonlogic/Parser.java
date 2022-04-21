package ru.gosuslugi.pgu.fs.common.jsonlogic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex.DateToStringArgument;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService;
import ru.gosuslugi.pgu.fs.common.service.condition.ConditionCheckerHelper;
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex.ComplexType;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex.DateArgument;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicParseException;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.*;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive.BooleanArgument;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive.NumberArgument;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive.StringArgument;
import ru.gosuslugi.pgu.fs.common.variable.VariableType;

import java.util.*;


@Component
@RequiredArgsConstructor
public class Parser {
    private static final ObjectMapper PARSER = JsonProcessingUtil.getObjectMapper();
    private final ProtectedFieldService protectedFieldService;
    private final VariableRegistry variableRegistry;
    private final JsonProcessingService jsonProcessingService;
    private final ConditionCheckerHelper conditionCheckerHelper;

    public Node parse(String json, ScenarioDto scenarioDto) {
        try {
            return parse(PARSER.readTree(json), scenarioDto);
        } catch (JsonProcessingException e) {
            throw new JsonLogicParseException(e);
        }
    }

    private Node parse(JsonNode root, ScenarioDto scenarioDto) {
        if (root.isNull()) {
            return NullNode.NULL;
        }
        if (root.isValueNode()) {
            if (root.isTextual()) {
                return new StringArgument(getReferenceValue(root.textValue(), scenarioDto));
            }
            if (root.isNumber()) {
                return new NumberArgument(root.numberValue());
            }
            if (root.isBoolean()) {
                if (root.booleanValue()) {
                    return BooleanArgument.TRUE;
                }
                return BooleanArgument.FALSE;
            }
        }
        if (root.isArray()) {
            List<Node> nodes = new ArrayList<>(root.size());
            for (JsonNode jsonNode : root) {
                nodes.add(parse(jsonNode, scenarioDto));
            }
            return new ArrayNode(nodes);
        }
        Map<String, JsonNode> fields = new HashMap<>();
        root.fields().forEachRemaining(entry -> fields.put(entry.getKey(), entry.getValue()));
        Set<String> keys = fields.keySet();
        if (keys.contains("value")) {
            try {
                return parseArgument(fields, scenarioDto);
            } catch (JsonProcessingException e) {
                throw new JsonLogicParseException("Ошибка парсинга json");
            }
        }
        if (keys.size() != 1) {
            throw new JsonLogicParseException(String.format("Json объект должен иметь 1 ключ, найдено %d", keys.size()));
        }
        String key = keys.stream().findAny().get();
        Node argumentNode = parse(root.get(key), scenarioDto);
        ArrayNode arguments;
        if (argumentNode instanceof ArrayNode) {
            arguments = (ArrayNode) argumentNode;
        } else {
            arguments = new ArrayNode(Collections.singletonList(argumentNode));
        }

        return new OperationNode(key, arguments);


    }

    private Node parseArgument(Map<String, JsonNode> fields, ScenarioDto scenarioDto) throws JsonProcessingException {
        fields.put("value", new TextNode(getReferenceValue(fields.get("value").asText(), scenarioDto)));
        if (!fields.containsKey("type")) {
            return new StringArgument(fields.get("value").asText());
        }
        switch (ComplexType.valueOf(fields.get("type").asText())) {
            case Date:
                return PARSER.readValue(JsonProcessingUtil.toJson(fields), DateArgument.class);
            case DateToString:
                return PARSER.readValue(JsonProcessingUtil.toJson(fields), DateToStringArgument.class);
            case String:
                return PARSER.readValue(JsonProcessingUtil.toJson(fields), StringArgument.class);
        }

        return null;
    }

    private String getReferenceValue(String value, ScenarioDto scenarioDto) {
        Object refValue;
        if (value.startsWith("answer.")) {
            String answerRef = value.replace("answer.", "");
            DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()));
            DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue()));
            DocumentContext serviceInfoContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getServiceInfo()));
            refValue = conditionCheckerHelper.getFirstFromContexts(answerRef, List.of(currentValueContext, applicantAnswersContext, serviceInfoContext), Object.class);
            return Optional.ofNullable(refValue).map(JsonProcessingUtil::toJson).orElse("");
        } else if (value.startsWith("protected.")) {
            String protectedField = value.replace("protected.", "");
            refValue = protectedFieldService.getValue(protectedField);
            return Optional.ofNullable(refValue).map(JsonProcessingUtil::toJson).orElse("");
        } else if (value.startsWith("variable.")) {
            String variable = value.replace("variable.", "");
            refValue = variableRegistry.getVariable(VariableType.valueOf(variable)).getValue(scenarioDto);
            return Optional.ofNullable(refValue).map(JsonProcessingUtil::toJson).orElse("");
        }
        return value;
    }
}
