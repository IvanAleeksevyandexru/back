package ru.gosuslugi.pgu.fs.common.helper;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;
import ru.gosuslugi.pgu.fs.common.exception.ConfigurationException;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.*;


@Slf4j
@Component
public class HelperScreenRegistry {

    private ConcurrentHashMap<ScreenType, ScreenHelper> screenRegistry = new ConcurrentHashMap<>();

    public void register(ScreenHelper helper) {
        ScreenType screenType = helper.getType();
        ScreenHelper oldHelper = screenRegistry.putIfAbsent(screenType, helper);
        if(Objects.nonNull(oldHelper)){
            String errorMessage = String.format(
                    "Helper %s для экрана %s уже зарегистрирован: %s",
                    helper.getClass().getCanonicalName(),
                    screenType,
                    oldHelper.getClass().getCanonicalName());
            error(log, () -> errorMessage);
            throw new ConfigurationException(errorMessage);
        }
        screenRegistry.put(screenType, helper);
    }

    public ScreenHelper getHelper(ScreenType screenType){
        return screenRegistry.get(screenType);
    }
}
