package ru.gosuslugi.pgu.fs.common.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class PlaceholderHolder {

    private final String wrapped;
    private final String value;
}
