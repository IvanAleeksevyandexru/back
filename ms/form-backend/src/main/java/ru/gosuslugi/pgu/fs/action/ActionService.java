package ru.gosuslugi.pgu.fs.action;

import ru.gosuslugi.pgu.dto.action.ActionRequestDto;
import ru.gosuslugi.pgu.dto.action.ActionResponseDto;

public interface ActionService {

    ActionType getActionType();

    ActionResponseDto invoke(ActionRequestDto actionRequestDto);
}
