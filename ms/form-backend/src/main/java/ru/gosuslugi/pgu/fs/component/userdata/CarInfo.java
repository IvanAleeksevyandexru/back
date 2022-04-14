package ru.gosuslugi.pgu.fs.component.userdata;

import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleInfoRequest;

/**
 * https://jira.egovdev.ru/browse/EPGUCORE-90200 - расширение для 1.4+
 */
public interface CarInfo {

    default VehicleInfoRequest buildVehicleInfoRequest(Person person, String typeId, String vin, String govRegNumber, String sts, String tx) {
        String preparedTypeId = typeId;
        if ("null".equals(preparedTypeId)) {
            preparedTypeId = null;
        }
        return VehicleInfoRequest
                .builder()
                .lastName(person.getLastName())
                .firstName(person.getFirstName())
                .middleName(person.getMiddleName())
                .tx(tx)
                .typeId(preparedTypeId)
                .vin(vin)
                .sts(sts)
                .govRegNumber(govRegNumber)
                .build();
    }

    default FederalNotaryRequest buildFederalNotaryRequest(String orderId, String vin, String tx) {
        return FederalNotaryRequest
                .builder()
                .orderId(orderId)
                .vin(vin)
                .tx(tx)
                .build();
    }
}
