package ru.gosuslugi.pgu.fs.helper;

import org.springframework.beans.factory.annotation.Autowired;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;

import java.util.Objects;

/**
 *
 * Этот абстрактный класс определяет общее поведение для гендерных экранов таких как:
 * {@link ScreenType#GQUESTION}, {@link ScreenType#GCOMPONENT}, {@link ScreenType#GCUSTOM}, 
 * {@link ScreenType#GINFO}, {@link ScreenType#GREPEATABLE},
 *
 */
public abstract class AbstractGenderScreenHelper extends AbstractScreenHelper implements GenderHelper {

    @Autowired
    protected UserPersonalData userPersonalData;

    /**
     *
     * @return не гендерный тип экрана соответствующий данному экрану.
     * Например ScreenType.COMPONENT для экрана ScreenType.GCOMPONENT.
     */
    protected abstract ScreenType getNewType();

    @Override
    public ScreenDescriptor processScreen(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto) {
        ScreenDescriptor resultScreenDescriptor = screenDescriptor.getCopy();
        resultScreenDescriptor.setType(getNewType());
        String gender = Objects.nonNull(userPersonalData.getUserId()) && Objects.nonNull(userPersonalData.getPerson()) ? userPersonalData.getPerson().getGender() : null;
        resultScreenDescriptor.setHeader(getHeader(gender, screenDescriptor));
        return resultScreenDescriptor;
    }
}
