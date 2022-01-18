package ru.gosuslugi.pgu.fs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.gosuslugi.pgu.dto.action.ActionRequestDto;
import ru.gosuslugi.pgu.dto.action.ActionResponseDto;
import ru.gosuslugi.pgu.fs.action.ActionService;
import ru.gosuslugi.pgu.fs.action.ActionType;
import ru.gosuslugi.pgu.fs.utils.TracingHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "service/action", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ScenarioActionController {

    private static final String ACTION_NOT_FOUND = "Action not found";

    private final List<ActionService> actionServiceList;
    private final TracingHelper tracingHelper;

    @PostMapping("/{actionType}")
    public ActionResponseDto invokeAction(@PathVariable("actionType") String actionType, @RequestBody ActionRequestDto actionRequestDto){
        tracingHelper.addServiceCodeAndOrderId(actionRequestDto);
        Optional<ActionService> optionalActionService = this.getActionServiceByActionType(ActionType.valueOf(actionType));
        if(optionalActionService.isEmpty()){
            return handleActionServiceNotFound();
        }
        return optionalActionService.get().invoke(actionRequestDto);
    }

    private Optional<ActionService> getActionServiceByActionType(ActionType actionType){
        return actionServiceList.stream().filter(v-> v.getActionType().equals(actionType)).findFirst();
    }

    private ActionResponseDto handleActionServiceNotFound(){
        ActionResponseDto actionResponseDto = new ActionResponseDto();

        actionResponseDto.setResult(false);
        actionResponseDto.setErrorList(Collections.singletonList(ACTION_NOT_FOUND));
        return actionResponseDto;
    }
}
