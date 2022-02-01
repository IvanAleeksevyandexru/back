package ru.gosuslugi.pgu.fs.service;

import java.util.Map;

public interface ChecksumService {
    String computeChecksum(Map<String,String> params);
}
