package ru.gosuslugi.pgu.fs.component.address;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.fs.utils.ContextJsonUtil;
import ru.gosuslugi.pgu.fs.utils.FullAddressEnrichUtil;
import ru.gosuslugi.pgu.fs.utils.FullAddressFiasUtil;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public abstract class AbstractFullAddressComponent<PreSetModel> extends AbstractAddressComponent<PreSetModel> {

    @Override
    public FullAddress getFullAddress(Map.Entry<String, ApplicantAnswer> entry) {
        return getFullAddressContext(entry).read();
    }

    @Override
    public boolean addMetaInfo(Map.Entry<String, ApplicantAnswer> entry, DadataAddressResponse addressResponse) {
        ContextJsonUtil<FullAddress> contextJsonUtil = getFullAddressContext(entry);
        FullAddress read = contextJsonUtil.read();
        if (isNull(read)) {
            return false;
        }
        FullAddress fullAddress = FullAddressFiasUtil.addMetaInfo(read, addressResponse);
        if (isNull(fullAddress))  {
            return false;
        }
        contextJsonUtil.save(fullAddress);
        return true;
    }

    @Override
    public void enrichEntry(Map.Entry<String, ApplicantAnswer> entry) {
        ContextJsonUtil<FullAddress> contextJsonUtil = getFullAddressContext(entry);
        FullAddress read = contextJsonUtil.read();
        if (nonNull(read)) {
            contextJsonUtil.save(FullAddressEnrichUtil.enrich(read));
        }
    }

    private ContextJsonUtil<FullAddress> getFullAddressContext(Map.Entry<String, ApplicantAnswer> entry) {
        return new ContextJsonUtil<>(
            Optional.ofNullable(entry).map(Map.Entry::getValue).orElse(null),
            getFullAddressJsonPath(),
            FullAddress.class
        );
    }

    @Override
    public void addOktmoNameInfo(Map.Entry<String, ApplicantAnswer> entry, String oktmoName) {
        ContextJsonUtil<FullAddress> contextJsonUtil = getFullAddressContext(entry);
        FullAddress fullAddress = contextJsonUtil.read();
        if (isNull(fullAddress)) {
            logger.info("fullAddress is null");
            return;
        }
        fullAddress.setOktmoName(oktmoName);
        contextJsonUtil.save(fullAddress);
    }

    public abstract String getFullAddressJsonPath();

}
