package ru.gosuslugi.pgu.fs.descriptor.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalDescriptor;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ErrorModalDescriptorServiceImpl implements ErrorModalDescriptorService {

    private final List<ErrorModalDescriptor> errorModalDescriptors;
    Map<String, ErrorModalDescriptor> errorModalDescriptorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (ErrorModalDescriptor errorDescriptor: errorModalDescriptors) {
            errorModalDescriptorMap.put(errorDescriptor.getName(), errorDescriptor);
        }
    }

    @Override
    public ErrorModalWindow getErrorModal(ErrorModalView view) {
        ErrorModalDescriptor errorModalDescriptor = errorModalDescriptorMap.get(view.getMappingType().name());
        if (Objects.isNull(errorModalDescriptor)) {
            return null;
        }
        return errorModalDescriptor.getModals().get(view.name());
    }
}
