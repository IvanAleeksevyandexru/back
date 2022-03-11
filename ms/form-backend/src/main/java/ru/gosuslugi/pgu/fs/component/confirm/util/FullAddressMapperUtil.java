package ru.gosuslugi.pgu.fs.component.confirm.util;

import lombok.experimental.UtilityClass;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddress;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressElement;

import java.util.Optional;

@UtilityClass
public class FullAddressMapperUtil {

    public static String getRegionCode(DadataAddress dadataAddress) {
        // See epgu2-form-frontend/projects/epgu-constructor/src/lib/shared/services/address-helper/address-helper.service.ts
        final var kladrCode = getKladrCode(dadataAddress);
        return kladrCode != null && kladrCode.length() >= 2 ? kladrCode.substring(0, 2) : null;
    }

    public static String getKladrCode(DadataAddress dadataAddress) {
        // See epgu2-form-frontend/projects/epgu-constructor/src/lib/shared/services/address-helper/address-helper.service.ts
        final var elements = dadataAddress != null ? dadataAddress.getElements() : null;
        return elements != null && !elements.isEmpty() ? elements.get(elements.size() - 1).getKladrCode() : null;
    }

    public static String getFiasCode(DadataAddress dadataAddress) {
        return dadataAddress != null ? dadataAddress.getFiasCode() : null;
    }

    public static String getAddressElementFiasCode(DadataAddress dadataAddress, int level) {
        return Optional.ofNullable(dadataAddress)
                .map(DadataAddress::getElements)
                .flatMap(elements -> elements.stream()
                        .filter(element -> element.getLevel().equals(level))
                        .findFirst())
                .map(DadataAddressElement::getFiasCode)
                .orElse(null);
    }
}
