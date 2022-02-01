package ru.gosuslugi.pgu.fs.component.confirm.util;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddress;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressElement;

import java.util.Optional;

@UtilityClass
public class FullAddressMapperUtil {

    public static String getRegionCode(DadataAddress dadataAddress) {
        // See epgu2-form-frontend/projects/epgu-constructor/src/lib/shared/services/address-helper/address-helper.service.ts
        return Optional.ofNullable(getKladrCode(dadataAddress))
                .filter(str -> str.length() >= 2)
                .map(str -> str.substring(0, 2))
                .orElse(null);
    }

    public static String getKladrCode(DadataAddress dadataAddress) {
        // See epgu2-form-frontend/projects/epgu-constructor/src/lib/shared/services/address-helper/address-helper.service.ts
        return Optional.ofNullable(getFiasCode(dadataAddress))
                .flatMap(fiasCode -> Optional.ofNullable(dadataAddress)
                        .map(DadataAddress::getElements)
                        .flatMap(elements -> elements.stream()
                                .filter(element -> fiasCode.equals(element.getFiasCode()))
                                .findFirst())
                        .map(DadataAddressElement::getKladrCode))
                .orElseGet(() -> Optional.ofNullable(dadataAddress)
                        .map(DadataAddress::getElements)
                        .flatMap(list -> Lists.reverse(list).stream().findFirst())
                        .map(DadataAddressElement::getKladrCode)
                        .orElse(null));
    }

    public static String getFiasCode(DadataAddress dadataAddress) {
        return Optional.ofNullable(dadataAddress)
                .map(DadataAddress::getFiasCode)
                .orElse(null);
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
