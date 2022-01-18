package ru.gosuslugi.pgu.fs.action.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.action.ActionRequestDto;
import ru.gosuslugi.pgu.dto.action.ActionResponseDto;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.action.ActionService;
import ru.gosuslugi.pgu.fs.action.ActionType;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreateUrlAction implements ActionService {

    private static final String SCREEN_ID_ATTR = "screenId";
    private final MainDescriptorService mainDescriptorService;

    @Override
    public ActionType getActionType() {
        return ActionType.createUrl;
    }

    @Override
    public ActionResponseDto invoke(ActionRequestDto actionRequestDto) {
        var screenId = actionRequestDto.getAdditionalParams().get(SCREEN_ID_ATTR);
        var scenario = actionRequestDto.getScenarioDto();
        if(Objects.isNull(scenario.getServiceCode())){
            throw new FormBaseException("Не указан serviceId");
        }

        var sd = mainDescriptorService.getServiceDescriptor(scenario.getServiceCode());
        if(Objects.isNull(sd)){
            throw new FormBaseException("Не найден service descriptor");
        }
        if(!sd.getExternalScreenIds().contains(screenId)){
            throw new FormBaseException("данный экран не находится в списке разрешенных");
        }

        if(Objects.isNull(screenId)){
            throw new FormBaseException("Нет указания экрана");
        }

        var url = this.prepareUrl(scenario,sd,String.valueOf(screenId));
        return this.prepareResponse(url);
    }

    private String prepareUrl(ScenarioDto scenario, ServiceDescriptor sd, String screenId){
        var screen = sd
                .getScreens()
                .stream()
                .filter(e-> e.getId().equals(screenId))
                .findFirst()
                .orElseThrow( () -> new FormBaseException("Не найден экран"));

        var answers = screen.getComponentIds().stream().map(componentId -> {
            var val = scenario.getApplicantAnswers().getOrDefault(componentId, scenario.getCurrentValue().get(componentId));
            return Map.entry(componentId,val.getValue());
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var builder = UriComponentsBuilder.newInstance();
        builder.queryParam("external")
               .queryParam(SCREEN_ID_ATTR,screenId)
        ;
        answers.forEach(builder::queryParam);
        return builder.buildAndExpand(screenId).toString();
    }

    private ActionResponseDto prepareResponse(String url){
        var response = new ActionResponseDto();
        Map<String, Object> responseParams = new HashMap<>();
        responseParams.put("value", url);
        response.setResponseData(responseParams);
        return response;
    }
}
