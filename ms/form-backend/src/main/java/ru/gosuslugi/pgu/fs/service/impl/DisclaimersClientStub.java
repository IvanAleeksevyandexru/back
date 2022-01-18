package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.gosuslugi.pgu.dto.PortalDisclaimer;
import ru.gosuslugi.pgu.fs.service.DisclaimersClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DisclaimersClientStub implements DisclaimersClient {

    @Override
    public List<PortalDisclaimer> getDisclaimers(String passCode, String targetCode) {
        return Collections.emptyList();
    }
}
