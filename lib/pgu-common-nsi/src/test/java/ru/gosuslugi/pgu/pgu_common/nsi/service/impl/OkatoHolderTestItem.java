package ru.gosuslugi.pgu.pgu_common.nsi.service.impl;

import ru.gosuslugi.pgu.common.core.service.OkatoHolder;

public class OkatoHolderTestItem implements OkatoHolder {

    private final String okato;

    public OkatoHolderTestItem(String okato) {
        this.okato = okato;
    }

    @Override
    public String getOkato() {
        return okato;
    }
}
