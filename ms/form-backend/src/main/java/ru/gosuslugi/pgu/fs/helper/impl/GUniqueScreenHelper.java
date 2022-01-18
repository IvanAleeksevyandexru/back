package ru.gosuslugi.pgu.fs.helper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;
import ru.gosuslugi.pgu.fs.helper.AbstractGenderScreenHelper;

@Service
@RequiredArgsConstructor
public class GUniqueScreenHelper extends AbstractGenderScreenHelper {

    @Override
    public ScreenType getType() {
        return ScreenType.GUNIQUE;
    }

    @Override
    protected ScreenType getNewType() {
        return ScreenType.UNIQUE;
    }

}
