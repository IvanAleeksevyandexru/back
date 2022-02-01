package ru.gosuslugi.pgu.pgu_common.gibdd.service.impl


import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException
import ru.gosuslugi.pgu.common.core.exception.dto.ExternalError
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.*
import ru.gosuslugi.pgu.pgu_common.gibdd.mapper.VehicleFullInfoMapperImpl
import ru.gosuslugi.pgu.pgu_common.gibdd.mapper.VehicleInfoMapperImpl
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService
import spock.lang.Specification

class GibddDataServiceImplSpec extends Specification {

    private static final String SHOWCASE_VIN_SERVICE_NAME = "ShowcaseVIN"
    private static final String SHOWCASE_OWNER_SERVICE_NAME = "ShowcaseOwner"
    private static final String GIBDD_RECORD_STATUS_DICTIONARY_NAME = "GIBDD_RECORD_STATUS"
    private static final String GIBDD_OWNER_TYPE_DICTIONARY_NAME = "GIBDD_OWNER_TYPE"
    private static final String EKOKLASS_MVD_DICTIONARY_NAME = "ekoklass_MVD"

    GibddDataServiceImpl service
    NsiDictionaryService nsiDictionaryService

    def setup() {
        nsiDictionaryService = Mock(NsiDictionaryService)

        service = new GibddDataServiceImpl(nsiDictionaryService, new VehicleInfoMapperImpl(), new VehicleFullInfoMapperImpl())
    }

    def 'If vehicle service by VIN return error'() {
        given:
        GibddServiceResponse<VehicleInfo> result

        when:
        nsiDictionaryService.getDictionary(_ as String, _ as String, _ as NsiDictionaryFilterRequest) >>
                showcaseVINErrorResponse()
        result = service.getAsyncVehicleInfo(Stub(VehicleInfoRequest)).join()

        then:
        result.externalServiceCallResult == ExternalServiceCallResult.EXTERNAL_SERVER_ERROR
    }

    def 'If vehicle service by VIN return empty result'() {
        given:
        GibddServiceResponse<VehicleInfo> result

        when:
        nsiDictionaryService.getDictionary(_ as String, _ as String, _ as NsiDictionaryFilterRequest) >>
                showcaseVINNotFoundResponse()
        result = service.getAsyncVehicleInfo(Stub(VehicleInfoRequest)).join()

        then:
        result.externalServiceCallResult == ExternalServiceCallResult.NOT_FOUND_ERROR
    }

    def 'If vehicle service by VIN return info'() {
        given:
        VehicleInfo result

        when:
        nsiDictionaryService.getDictionary(GIBDD_OWNER_TYPE_DICTIONARY_NAME, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '1', title: 'Юридическое лицо'),
                                          new NsiDictionaryItem(value: '2', title: 'Физическое лицо')])
        nsiDictionaryService.getDictionary(GIBDD_RECORD_STATUS_DICTIONARY_NAME, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '1', title: 'На учёте')])
        nsiDictionaryService.getDictionary(EKOKLASS_MVD_DICTIONARY_NAME, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '4', title: 'Четвёртый')])
        nsiDictionaryService.getDictionary(_ as String, SHOWCASE_VIN_SERVICE_NAME, _ as NsiDictionaryFilterRequest) >>
                showcaseVINSuccessResponse()
        result = service.getAsyncVehicleInfo(Stub(VehicleInfoRequest)).join().data

        then:
        result as VehicleInfo
        result.status == 'На учёте'
        result.statusIntValue == '1'
        !result.legals
        result.restrictionsFlag
        !result.searchingTransportFlag
        result.enginePowerVt == '188.3'
        result.stsSeriesNumber == '222'
        result.ptsNum == '100'
        result.ptsType == 'ПТС'
        result.vehicleType == 'Легковой автомобил'
        result.ecologyClass == '4'
        result.ecologyClassDesc == 'Четвёртый'
        result.lastRegActionName == 'RegActionName2'
        result.restrictions.size() == 2
        result.restrictions[0].restrictionDate == '30.04.2018'
        result.restrictions[0].gibddDepart == 'department-1'
        result.restrictions[0].status == 'снято'
        result.restrictions[0].statusIntValue == '0'

        result.ownerPeriods.size() == 4
        result.ownerPeriods[0].ownerType == null
        result.ownerPeriods[2].ownerType == 'Юридическое лицо'
        // периоды владения должны быть отсортированы
        result.ownerPeriods[0].dateStart == null
        result.ownerPeriods[0].dateEnd == '01.03.2016'
        result.ownerPeriods[1].dateStart == '01.03.2016'
        result.ownerPeriods[1].dateEnd == '30.04.2016'
        result.ownerPeriods[2].dateStart == '30.04.2016'
        result.ownerPeriods[2].dateEnd == '30.04.2018'
        result.ownerPeriods[3].dateStart == '30.04.2018'
        result.ownerPeriods[3].dateEnd == null
    }

    def 'If vehicle service by VIN (full info) return result'() {
        given:
        VehicleFullInfo result

        when:
        nsiDictionaryService.getDictionary(GIBDD_OWNER_TYPE_DICTIONARY_NAME, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '1', title: 'Юридическое лицо'),
                                          new NsiDictionaryItem(value: '2', title: 'Физическое лицо')])
        nsiDictionaryService.getDictionary(GIBDD_RECORD_STATUS_DICTIONARY_NAME, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '1', title: 'На учёте')])
        nsiDictionaryService.getDictionary(EKOKLASS_MVD_DICTIONARY_NAME, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '4', title: 'Четвёртый')])
        nsiDictionaryService.getDictionary(_ as String, SHOWCASE_VIN_SERVICE_NAME, _ as NsiDictionaryFilterRequest) >>
                showcaseVINSuccessResponse()
        result = service.getVehicleFullInfo(Stub(VehicleInfoRequest))

        then:
        result as VehicleInfo
        result.vehicleTypeTAM == 'typeTAM'
        result.wheelLocation == '1'
        result.wheelLocationDesc == 'Левостороннее'
        result.transmissionType == '2'
        result.transmissionTypeDesc == 'Автоматическая'
        result.driveUnitType == '1'
        result.driveUnitTypeDesc == 'Передний'
        result.searchingSpec.operationDate == '30.04.2018'
        result.searchingSpec.techOperation == 'TechOperation1'
    }

    def 'Can get owner vehicles'() {
        given:
        List<VehicleInfo> result

        when:
        nsiDictionaryService.getDictionary(GIBDD_OWNER_TYPE_DICTIONARY_NAME, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '1', title: 'Юридическое лицо')])
        nsiDictionaryService.getDictionary(GIBDD_RECORD_STATUS_DICTIONARY_NAME, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '1', title: 'На учёте')])
        nsiDictionaryService.getDictionary(EKOKLASS_MVD_DICTIONARY_NAME, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '4', title: 'Четвёртый')])
        nsiDictionaryService.getDictionary(_ as String, SHOWCASE_OWNER_SERVICE_NAME, _ as NsiDictionaryFilterRequest) >>
                showcaseOwnerResponse()
        result = service.getOwnerVehiclesInfo(Stub(OwnerVehiclesRequest))

        then:
        result as List
        result.size() == 2
        def item = result[1]
        item.recordStatus == '1'
        item.status == 'На учёте'
        item.statusIntValue == '1'
        item.legals
        item.vin == '100'
        item.category == 'Category1'
        item.vehicleType == 'Легковой универсал'
        item.engineModel == 'G4NA-5R'
        item.engineNum == 'G4NALH310736'
        item.ecologyClass == '4'
        item.ecologyClassDesc == 'Четвёртый'
        item.wheelLocation == '1'
        item.wheelLocationDesc == 'Левостороннее'
        item.transmissionType == '2'
        item.transmissionTypeDesc == 'Автоматическая'
        item.driveUnitType == '1'
        item.driveUnitTypeDesc == 'Передний'
        item.ownerPeriods.size() == 2
        item.ownerPeriods[0].ownerType == 'Юридическое лицо'
        item.ownerPeriods[0].dateStart == '30.04.2016'
        item.restrictions.size() == 1
        item.restrictions[0].restrictionDate == '30.04.2018'
        item.regActions.size() == 1
        item.regActions[0].regActionName == 'RegActionName1'
        item.searchingSpec.operationDate == '30.04.2018'
        item.searchingSpec.techOperation == 'TechOperation1'
        item.owner.birthPlace == 'ТУЛЬСКАЯ ОБЛАСТЬ'
        item.owner.documentNumSer == '7729165816'
        item.owner.documentNumSerFormatted == '7729 165816'
    }

    def 'Can get federal notary info if federal notary return "OK"'() {
        given:
        FederalNotaryInfo result

        when:
        nsiDictionaryService.getDictionary(_ as String, _ as String, _ as NsiDictionaryFilterRequest) >> {
            new NsiDictionary(error: new ExternalError(code: 0), items: [new NsiDictionaryItem(value: 'OK')])
        }
        result = service.getFederalNotaryInfo(Stub(FederalNotaryRequest))

        then:
        result.isPledged
    }

    def 'Can get federal notary info if service return "NO_DATA"'() {
        given:
        FederalNotaryInfo result

        when:
        nsiDictionaryService.getDictionary(_ as String, _ as String, _ as NsiDictionaryFilterRequest) >> {
            new NsiDictionary(error: new ExternalError(code: 0), items: [new NsiDictionaryItem(value: 'NO_DATA')])
        }
        result = service.getFederalNotaryInfo(Stub(FederalNotaryRequest))

        then:
        !result.isPledged
    }

    def 'Can get federal notary info if service return incorrect value'() {
        when:
        nsiDictionaryService.getDictionary(_ as String, _ as String, _ as NsiDictionaryFilterRequest) >> {
            new NsiDictionary(error: new ExternalError(code: 0), items: [new NsiDictionaryItem(value: 'INCORRECT')])
        }
        service.getFederalNotaryInfo(Stub(FederalNotaryRequest))

        then:
        thrown(ExternalServiceException)
    }

    def 'Can get federal notary info if service return error'() {
        when:
        nsiDictionaryService.getDictionary(_ as String, _ as String, _ as NsiDictionaryFilterRequest) >> {
            new NsiDictionary(error: new ExternalError(code: 500), items: [new NsiDictionaryItem(value: 'OK')])
        }
        service.getFederalNotaryInfo(Stub(FederalNotaryRequest))

        then:
        thrown(ExternalServiceException)
    }

    def 'Can get federal notary info if service return empty value'() {
        when:
        nsiDictionaryService.getDictionary(_ as String, _ as String, _ as NsiDictionaryFilterRequest) >> {
            new NsiDictionary(error: new ExternalError(code: 0), items: [])
        }
        service.getFederalNotaryInfo(Stub(FederalNotaryRequest))

        then:
        thrown(ExternalServiceException)
    }

    static def showcaseVINErrorResponse() {
        new NsiDictionary(error: new ExternalError(code: 3, message: 'Internal Error'))
    }

    static def showcaseVINNotFoundResponse() {
        new NsiDictionary(error: new ExternalError(code: 0, message: '1 - Транспортные средства с указанными входными параметрами не найдены.'))
    }


    static def showcaseVINSuccessResponse() {
        new NsiDictionary(
                error: new ExternalError(code: 0, message: 'operation completed'),
                items: [
                        new NsiDictionaryItem(title: null, attributeValues: [
                                RecordStatus: '1', RestrictionsFlag: 'true', Category: 'Category1', EnginePowerkVt: '188.3',
                                VenicleType: 'Легковой автомобил', GovRegNumber: 'A 500 AA', EcologyClass: '4',
                                WheelLocation: '1', TransmissionType: '2', DriveUnitType: '1', VehicleTypeTAM: 'typeTAM']),
                        new NsiDictionaryItem(value: 'RegistrationDoc', title: 'RegistrationDoc', attributeValues: [RegDocSeriesNumber: '222']),
                        new NsiDictionaryItem(value: 'PTS', title: 'PTS', attributeValues: [PTSType: 'ПТС', PTSNum: '100']),
                        new NsiDictionaryItem(value: 'RegAction_0', title: 'RegAction', attributeValues: [RegActionName: 'RegActionName1', RegDate: '2020.12.10 10:00:00']),
                        new NsiDictionaryItem(value: 'RegAction_1', title: 'RegAction', attributeValues: [RegActionName: 'RegActionName2', RegDate: '2020.12.15 10:00:00']),
                        new NsiDictionaryItem(value: 'Owner_0', title: 'Owner', attributeValues: [OwnerType: '1', DateStart: '2016-04-30 00:00:00', DateEnd: '2018-04-30 00:00:00']),
                        new NsiDictionaryItem(value: 'Owner_1', title: 'Owner', attributeValues: [DateEnd: '2016-03-01 00:00:00']),
                        new NsiDictionaryItem(value: 'Owner_2', title: 'Owner', attributeValues: [DateStart: '2018-04-30 00:00:00']),
                        new NsiDictionaryItem(value: 'Owner_3', title: 'Owner', attributeValues: [DateStart: '2016-03-01 00:00:00', DateEnd: '2016-04-30 00:00:00']),
                        new NsiDictionaryItem(value: 'SearchingSpec', title: 'SearchingSpec', attributeValues: [OperationDate: '2018-04-30 00:00:00', TechOperation: 'TechOperation1']),
                        new NsiDictionaryItem(value: 'RestrInfoElement_0', title: 'RestrInfoElement', attributeValues: [RestrictionType: 'RestrictionType1', Status: '0', RestrictionDate: '2018-04-30 00:00:00', GIBDDDepart: 'department-1']),
                        new NsiDictionaryItem(value: 'RestrInfoElement_1', title: 'RestrInfoElement', attributeValues: [RestrictionType: 'RestrictionType2', Status: '1', RestrictionDate: '2018-04-30 00:00:00'])
                ])
    }

    static def showcaseOwnerResponse() {
        new NsiDictionary(items: [
                new NsiDictionaryItem(value: 'ResponseByOwnerData_0', title: 'main', attributeValues: [
                        VIN: '100', Category: 'Category1', RecordStatus: '1', MarkName: 'Kia', VenicleType: 'Легковой универсал', EngineModel: 'G4NA-5R', EngineNum: 'G4NALH310736', EcologyClass: '4',
                        WheelLocation: '1', TransmissionType: '2', DriveUnitType: '1']),
                new NsiDictionaryItem(value: 'ResponseByOwnerData_0', title: 'RegistrationDoc', attributeValues: [RegDocumentType: 'СТС', RegDocSeriesNumber: '7729165816', RegDocDocumentDate: '2014-08-25']),
                new NsiDictionaryItem(value: 'ResponseByOwnerData_0', title: 'PTS', attributeValues: [PTSType: "ПТС", PTSIssueAgency: 'ООО ХОНДА МОТОР', PTSNum: '77УК036740', PTSRegDate: '2008-01-01']),
                new NsiDictionaryItem(value: 'ResponseByOwnerData_0', title: 'Owner_0', attributeValues: [OwnerType: '1', DateStart: '2016-04-30 00:00:00', DateEnd: '2018-04-30 00:00:00']),
                new NsiDictionaryItem(value: 'ResponseByOwnerData_0', title: 'Owner_1', attributeValues: [DateStart: '2016-04-30 00:00:00', DateEnd: '2018-04-30 00:00:00']),
                new NsiDictionaryItem(value: 'ResponseByOwnerData_0', title: 'RestrInfoElement_0', attributeValues: [RestrictionType: 'RestrictionType1', Status: '0', RestrictionDate: '2018-04-30 00:00:00', GIBDDDepart: 'department-1']),
                new NsiDictionaryItem(value: 'ResponseByOwnerData_0', title: 'RegAction_0', attributeValues: [RegActionName: 'RegActionName1', RegDate: '2020.12.10 10:00:00']),
                new NsiDictionaryItem(value: 'ResponseByOwnerData_0', title: 'SearchingSpec', attributeValues: [OperationDate: '2018-04-30 00:00:00', TechOperation: 'TechOperation1']),
                new NsiDictionaryItem(value: 'ResponseByOwnerData_0', title: 'Owners', attributeValues: [DocumentNumSer: '7729165816', DocumentDate: '2014-08-25', BirthPlace: 'ТУЛЬСКАЯ ОБЛАСТЬ']),

                new NsiDictionaryItem(value: 'ResponseByOwnerData_1', title: 'main',
                        attributeValues: [VIN: '200', Category: 'Category2', RecordStatus: '1', MarkName: 'BMW'])
        ])
    }
}