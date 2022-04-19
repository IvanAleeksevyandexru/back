package ru.gosuslugi.pgu.pgu_common.gibdd.service;

import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.GibddServiceResponse;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.OwnerVehiclesRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleFullInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleInfoRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * https://jira.egovdev.ru/browse/EPGUCORE-90200 - расширение для 1.4+
 */
public interface GibddDataService {

    VehicleFullInfo getVehicleFullInfo(VehicleInfoRequest request);

    List<VehicleFullInfo> getOwnerVehiclesInfo(OwnerVehiclesRequest request);

    FederalNotaryInfo getFederalNotaryInfo(FederalNotaryRequest request);

    CompletableFuture<GibddServiceResponse<VehicleInfo>> getAsyncVehicleInfo(VehicleInfoRequest request);

    CompletableFuture<GibddServiceResponse<FederalNotaryInfo>> getAsyncFederalNotaryInfo(FederalNotaryRequest request);

    // Не асинхронный метод для последовательного вызова при поиске по ГРЗ
    VehicleInfo getVehicleInfo(VehicleInfoRequest request);
}
