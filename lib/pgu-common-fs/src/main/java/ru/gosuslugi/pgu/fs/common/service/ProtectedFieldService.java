package ru.gosuslugi.pgu.fs.common.service;

import java.util.Map;

public interface ProtectedFieldService {

    Object getValue(String name);
    Map<String, Object> getAllValues();
}
