package ru.gosuslugi.pgu.fs.common.service.functions.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.fs.common.service.functions.ContextFunctionService;
import ru.gosuslugi.pgu.fs.common.service.functions.ContextFunctions;

import java.util.List;

@Slf4j
@Service
public class ContextFunctionServiceImpl implements ContextFunctionService {

    @Override
    public String applyFunction(String functionName, List<String> functionArgs) {
        return ContextFunctions.valueOf(functionName).applyFunction(functionArgs);
    }
}
