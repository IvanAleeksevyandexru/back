package ru.gosuslugi.pgu.fs.common.component;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ComponentResponse<T> {

    private final T response;

    private ComponentResponse(T response) {
        this.response = response;
    }

    public static <T> ComponentResponse<T> of(T value) {
        return new ComponentResponse<>(value);
    }

    public static <T> ComponentResponse<T> empty() {
        return new ComponentResponse<>(null);
    }

    public T get() {
        return response;
    }

    public Class<?> getResponseClass() {
        return response == null ? null : response.getClass();
    }
}