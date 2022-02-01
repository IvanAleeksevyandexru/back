package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.service.ChecksumService;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChecksumServiceImpl implements ChecksumService {

    private final UserPersonalData userPersonalData;

    @SneakyThrows
    @Override
    public String computeChecksum(Map<String, String> params) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        params.values().forEach(param -> md.update(param.getBytes()));
        md.update(userPersonalData.getUserId().toString().getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
    }
}
