package ru.gosuslugi.pgu.fs.component.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.dto.lk.LkDataMessage;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticNotifierComponent extends AbstractComponent<String> {

    private final LkNotifierService lkNotifierService;
    private final static String fieldName = "fieldName";
    private final static String fieldValue = "fieldValue";
    private final static String fieldMnemonic = "fieldMnemonic";

    @Override
    public ComponentType getType() {
        return ComponentType.AnalyticNotifier;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        List<LkDataMessage> messages = getComponentLkNotifierMessages(component);
        if (!messages.isEmpty()) {
            lkNotifierService.sendMessages(scenarioDto.getOrderId(), messages);
        }
        return ComponentResponse.empty();
    }

    private List<LkDataMessage> getComponentLkNotifierMessages(FieldComponent component) {
        List<LkDataMessage> messages = new ArrayList<>();
        Map<String, String> arguments = component.getArguments();
        if (arguments != null && !arguments.isEmpty()) {
            for (int i = 1; i <= 30; i++) {
                String fieldNameArg = arguments.get(fieldName+i);
                String fieldValueArg = arguments.get(fieldValue+i);
                String fieldMnemonicArg = arguments.get(fieldMnemonic+i);
                if (StringUtils.hasText(fieldNameArg) && StringUtils.hasText(fieldValueArg) && StringUtils.hasText(fieldMnemonicArg)) {
                    messages.add(new LkDataMessage(fieldNameArg, fieldValueArg, fieldMnemonicArg));
                }
            }
        }
        return messages;
    }
}
