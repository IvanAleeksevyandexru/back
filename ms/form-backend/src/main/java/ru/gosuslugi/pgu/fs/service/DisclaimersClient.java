package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.PortalDisclaimer;

import java.util.List;

public interface DisclaimersClient {
    List<PortalDisclaimer> getDisclaimers(String passCode, String targetCode);
}
