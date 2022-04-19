package ru.gosuslugi.pgu.fs.common.service.functions;

import ru.gosuslugi.pgu.fs.common.service.functions.impl.PeriodDescriptionUtil;

import java.util.List;
import java.util.function.Function;

public enum ContextFunctions {

    PERIOD_DESCRIPTION(PeriodDescriptionUtil::createPeriodDescription);

    private final Function<List<String>, String> function;

    ContextFunctions(Function<List<String>, String> function) {
        this.function = function;
    }

    public String applyFunction(List<String> args) {
        return function.apply(args);
    }
}
