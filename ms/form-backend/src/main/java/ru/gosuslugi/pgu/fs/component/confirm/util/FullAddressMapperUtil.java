package ru.gosuslugi.pgu.fs.component.confirm.util;

import com.google.common.collect.Lists;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddress;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressElement;

import java.util.List;
import java.util.Optional;

public class FullAddressMapperUtil {
    public static String getRegionCode(DadataAddress dadataAddress) {
        return Optional.ofNullable(getKladrCode(dadataAddress)).filter(str -> str.length() >= 2)
                .map(str -> str.substring(0, 2)).orElse(null);
    }

    public static String getKladrCode(DadataAddress dadataAddress) {
        return Optional.ofNullable(dadataAddress)
                .map(DadataAddress::getElements)
                .map(list -> Lists.reverse(list).stream().findFirst().orElse(null))
                .map(DadataAddressElement::getKladrCode)
                .orElse(null);
    }

    public static String getFiasCode(DadataAddress dadataAddress) {
        return Optional.ofNullable(dadataAddress)
                .map(DadataAddress::getFiasCode).orElse(null);
    }

    public static String getAddressElementFiasCode(DadataAddress dadataAddress, int level) {
        if (dadataAddress != null && !dadataAddress.getElements().isEmpty()) {
            List<DadataAddressElement> elements = dadataAddress.getElements();
            return elements.stream().filter(element -> element.getLevel().equals(level)).findFirst()
                    .map(DadataAddressElement::getFiasCode).orElse(null);
        }
        return null;
    }
}
