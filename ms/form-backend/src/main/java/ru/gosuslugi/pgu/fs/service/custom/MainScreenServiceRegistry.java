package ru.gosuslugi.pgu.fs.service.custom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.fs.common.exception.ConfigurationException;
import ru.gosuslugi.pgu.fs.service.MainScreenService;
import ru.gosuslugi.pgu.fs.service.impl.MainScreenServiceImpl;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainScreenServiceRegistry {

    private final ConcurrentHashMap<String, CustomScreenService> serviceRegistry = new ConcurrentHashMap<>();
    private final MainScreenServiceImpl mainScreenService;

    public void register(CustomScreenService service) {
        String targetId = service.getTargetId();
        CustomScreenService oldService = serviceRegistry.putIfAbsent(targetId, service);
        if (Objects.nonNull(oldService)) {
            String errorMessage = String.format(
                    "Сервис %s для услуги %s уже зарегистрирован: %s",
                    service.getClass().getCanonicalName(),
                    targetId,
                    oldService.getClass().getCanonicalName());
            error(log, () -> errorMessage);
            throw new ConfigurationException(errorMessage);
        }
    }

    public MainScreenService getService(String targetId) {
        if (Strings.isBlank(targetId)) {
            return mainScreenService;
        }
        return serviceRegistry.containsKey(targetId) ? serviceRegistry.get(targetId) : mainScreenService;
    }
}
