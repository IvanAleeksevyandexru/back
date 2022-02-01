package ru.gosuslugi.pgu.fs.action.impl;

import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.action.ActionRequestDto;
import ru.gosuslugi.pgu.dto.action.ActionResponseDto;
import ru.gosuslugi.pgu.fs.action.ActionService;
import ru.gosuslugi.pgu.fs.action.ActionType;

@Component
public class ResendSmsAction implements ActionService {

    @Override
    public ActionType getActionType() {
        return ActionType.resendSmsCode;
    }

    @Override
    public ActionResponseDto invoke(ActionRequestDto actionRequestDto) {
        return new ActionResponseDto();
    }
}
