package ru.gosuslugi.pgu.fs.common.exception.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.gosuslugi.pgu.common.core.exception.dto.ModalComponentButton;
import ru.gosuslugi.pgu.common.core.exception.dto.ModalWindowButton;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorModalWindow {
    private ErrorContent content;
    private boolean showCloseButton = false;
    private boolean showCrossButton = true;
    private String closeHandlerCase;
    private List<ModalWindowButton> actionButtons;
    private List<ModalComponentButton> buttons;
    private Boolean isShortModal = true;
}
