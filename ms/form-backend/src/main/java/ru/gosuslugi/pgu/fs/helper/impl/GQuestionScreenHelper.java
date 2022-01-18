package ru.gosuslugi.pgu.fs.helper.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;
import ru.gosuslugi.pgu.fs.helper.AbstractGenderScreenHelper;

@Service
@RequiredArgsConstructor
public class GQuestionScreenHelper extends AbstractGenderScreenHelper {

    @Override
    public ScreenType getType() {
        return ScreenType.GQUESTION;
    }

    @Override
    protected ScreenType getNewType() {
        return ScreenType.QUESTION;
    }

}
