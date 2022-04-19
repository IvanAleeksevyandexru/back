package ru.gosuslugi.pgu.fs.common.service.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.DisplayRequestAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.placeholder.PlaceholderContext;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.types.SubHeader;
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService;
import ru.gosuslugi.pgu.fs.common.variable.OrderIdVariable;

import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisplayReferenceServiceImpl implements DisplayReferenceService {

    private final JsonProcessingService jsonProcessingService;
    private final LinkedValuesService linkedValuesService;
    private final OrderIdVariable orderIdVariable;

    /**
     * Формирует поля экрана для вывода в интерфейсе
     * @param displayRequest    Экран
     * @param scenarioDto       Сценарий
     */
    public void processDisplayRefs(DisplayRequest displayRequest, ScenarioDto scenarioDto) {
        if (CollectionUtils.isEmpty(displayRequest.getAttrs()) && CollectionUtils.isEmpty(displayRequest.getLinkedValues())) {
            return;
        }

        linkedValuesService.fillLinkedValues(displayRequest, scenarioDto);

        DisplayRequestAttrsFactory factory = new DisplayRequestAttrsFactory(displayRequest.getAttrs());
        PlaceholderContext context = buildPlaceholderContext(factory, Map.of());

        String orderIdKey = orderIdVariable.getType().name();
        String orderId = orderIdVariable.getValue(scenarioDto);

        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue()));
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()));
        DocumentContext argumentsContext = JsonPath.parse(jsonProcessingService.toJson(displayRequest.getArguments()));
        DocumentContext variablesContext = JsonPath.parse(Map.of(orderIdKey, orderId));
        DocumentContext serviceInfoContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getServiceInfo()));

        DocumentContext[] extContexts = {
                currentValueContext,
                applicantAnswersContext,
                argumentsContext,
                variablesContext,
                serviceInfoContext
        };

        String headerValue = getValueByContext(displayRequest.getHeader(), Function.identity(), context, extContexts);
        displayRequest.setHeader(headerValue);

        if (displayRequest.getSubHeader() != null) {
            SubHeader newSubHeader = new SubHeader();
            String subHeaderValue = getValueByContext(displayRequest.getSubHeader().getText(), Function.identity(), context, extContexts);
            newSubHeader.setText(subHeaderValue);
            newSubHeader.setClarifications(displayRequest.getSubHeader().getClarifications());
            displayRequest.setSubHeader(newSubHeader);
        }
    }
}
