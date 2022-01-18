package ru.gosuslugi.pgu.fs.helper.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;
import ru.gosuslugi.pgu.fs.helper.AbstractGenderScreenHelper;

@Service
@RequiredArgsConstructor
public class GCustomScreenHelper extends AbstractGenderScreenHelper {

    @Override
    public ScreenType getType() {
        return ScreenType.GCUSTOM;
    }

    @Override
    protected ScreenType getNewType() {
        return ScreenType.CUSTOM;
    }

}
