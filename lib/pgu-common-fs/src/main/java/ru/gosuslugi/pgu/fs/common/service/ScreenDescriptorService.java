package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;

public interface ScreenDescriptorService {

    boolean isSkippedScreen(ScenarioRequest req, ScreenDescriptor screenDescriptor);
}
