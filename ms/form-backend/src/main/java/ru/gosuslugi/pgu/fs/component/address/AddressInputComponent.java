package ru.gosuslugi.pgu.fs.component.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.components.dto.AddressType;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressInputComponent extends AbstractFullAddressComponent<FullAddress> {

    private final UserPersonalData userPersonalData;

    @Override
    public String getFullAddressJsonPath() {
        // root
        return "$";
    }

    @Override
    public ComponentResponse<FullAddress> getInitialValue(FieldComponent component) {
        AddressType addressType = BasicComponentUtil.getAddrType(component);
        if (Objects.nonNull(addressType)) {
            Optional<EsiaAddress> esiaAddressOptional = userPersonalData.getAddresses().stream()
                    .filter(a -> a.getType().equals(addressType.getEsiaAddressType().getCode()))
                    .findFirst();

            if (esiaAddressOptional.isPresent()) {
                EsiaAddress esiaAddress = esiaAddressOptional.get();
                FullAddress fullAddress = new FullAddress();
                fullAddress.setFullAddress(esiaAddress.getAddressStr());
                fullAddress.setPostalCode(esiaAddress.getZipCode());
                fullAddress.setFiasCode(esiaAddress.getFiasCode());
                return ComponentResponse.of(fullAddress);
            }
        }

        return ComponentResponse.empty();
    }

    @Override
    public ComponentType getType() {
        return ComponentType.AddressInput;
    }
}
