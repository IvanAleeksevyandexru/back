package ru.gosuslugi.pgu.fs.common.service.functions;

import java.util.List;

public interface ContextFunctionService {
    String applyFunction(String functionName, List<String> args);
}
