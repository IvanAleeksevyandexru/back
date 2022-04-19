package ru.gosuslugi.pgu.fs.common.utils;

import java.util.regex.Pattern;

/** Обёртка для подстановок переменных в выражениях. */
public enum ReplacerWrapper {
    /**
     * Переменная отмечается в виде '$'
     */
    DOLLAR("$", "", Pattern.compile("\\$(.*?)(?:(?=\\s+|=|\\||&|-|\\+|\\*|/|>|<|!|\\(|\\))|$)")),
    /**
     * Переменная отмечается в виде '${}'
     */
    DOLLAR_CURLY_BRACES("${", "}", Pattern.compile("\\$\\{(.*?)}"));

    private final String prefix;
    private final String postfix;
    private final Pattern pattern;

    ReplacerWrapper(String prefix, String postfix, Pattern pattern) {
        this.prefix = prefix;
        this.postfix = postfix;
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String wrap(String str) {
        return prefix + str + postfix;
    }
}
