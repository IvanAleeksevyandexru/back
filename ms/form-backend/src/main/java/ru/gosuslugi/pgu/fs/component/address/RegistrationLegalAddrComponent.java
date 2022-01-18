package ru.gosuslugi.pgu.fs.component.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationLegalAddrComponent extends AbstractFullAddressComponent<String> {

    private static final String ADDRESS_TYPE_ATTR_KEY = "addressType";
    private static final String LEGAL_ADDRESS_TYPE = "legalAddress";
    private static final String FACT_ADDRESS_TYPE = "factAddress";
    private static final String OLG_TYPE = "OLG"; // legalAddress
    private static final String OPS_TYPE = "OPS"; // factAddress


    private final UserOrgData userOrgData;

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component) {
        if (Objects.isNull(userOrgData.getOrg())) {
            throw new FormBaseWorkflowException("Войдите как организация в личном кабинете");
        }
        Optional<EsiaAddress> esiaAddressOptional = getAddress(component);
        if (esiaAddressOptional.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            FullAddress address = new FullAddress();
            EsiaAddress esiaAddress = esiaAddressOptional.get();
            address.setFullAddress(esiaAddress.getAddressStr());
            result.put("regAddr", address);
            return ComponentResponse.of(JsonProcessingUtil.toJson(result));
        }

        return ComponentResponse.empty();
    }

    private Optional<EsiaAddress> getAddress(FieldComponent component) {
        String addrType = nonNull(component.getAttrs()) && component.getAttrs().containsKey(ADDRESS_TYPE_ATTR_KEY) ? component.getAttrs().get(ADDRESS_TYPE_ATTR_KEY).toString() : null;

        if (nonNull(addrType) && FACT_ADDRESS_TYPE.equals(addrType)) {
            return userOrgData.getAddresses().stream()
                    .filter(a -> a.getType().equals(OPS_TYPE))
                    .findFirst();
        }
        if (isNull(addrType) || LEGAL_ADDRESS_TYPE.equals(addrType)) {
            return userOrgData.getAddresses().stream()
                    .filter(a -> a.getType().equals(OLG_TYPE))
                    .findFirst();
        }
        return Optional.empty();
    }

    @Override
    public String getFullAddressJsonPath() {
        // regAddr after root
        return "$['regAddr']";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.RegistrationLegalAddr;
    }
}
