package ru.gosuslugi.pgu.fs.component.logic

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.service.BackRestCallService
import ru.gosuslugi.pgu.fs.service.impl.BackRestCallServiceImpl
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore
class BackRestCallRosreestrComponentSpec extends Specification {

    @Shared
    RestTemplate restTemplate
    MockRestServiceServer mockServer
    @Shared
    RestCallComponent restCallComponent
    BackRestCallComponent component
    @Shared
    BackRestCallService restCallService

    def setupSpec() {
        restTemplate = new RestTemplate()
        restCallService = new BackRestCallServiceImpl(restTemplate, Stub(RestTemplateBuilder))
        restCallComponent = new RestCallComponent('http://url_to_service')
        ComponentTestUtil.setAbstractComponentServices(restCallComponent)
    }

    def setup() {
        component = new BackRestCallComponent(restCallComponent, restCallService)
        ComponentTestUtil.setAbstractComponentServices(component)
        mockServer = MockRestServiceServer.createServer(restTemplate)
    }

    def 'select by CadNumber'() {
        when:
        def initialValue = component.getInitialValue(getCadNumberFieldComponent())
        then:
        initialValue.get() != ''
    }

    def 'select by PropOldNNumb'() {
        when:
        def initialValue = component.getInitialValue(getPropOldNNumbFieldComponent())
        then:
        initialValue.get() != ''
    }

    def static getCadNumberFieldComponent() {
        [
                id       : 'brc1',
                type     : 'BackRestCall',
                attrs    : [
                        method         : 'POST',
                        url            : 'http://pgu-uat-feddsnlb.test.gosuslugi.ru',
                        path           : '/internal/api/einfahrt',
                        formData: ['sql': '''SELECT 
                                            PF.TypeCode,
                                            PF.PropertyTypeValue,
                                            PF.CadNumber,
                                            PF.RegistrationDate,
                                            PF.PropOldNTypeValue,
                                            PF.PropOldNNumb,
                                            PF.PropOldNAssDate,
                                            PF.FIAS,
                                            PF.KLADRCode,
                                            PF.Oktmo,
                                            PF.PostalCode,
                                            PF.RegionCode,
                                            PF.RegionValue,
                                            PF.DistrictType,
                                            PF.DistrictName,
                                            PF.CityType,
                                            PF.CityName,
                                            PF.UrbanDistricType,
                                            PF.UrbanDistricName,
                                            PF.SovietVillagType,
                                            PF.NameSovietVill,
                                            PF.LocalityType,
                                            PF.LocalityName,
                                            PF.StreetType,
                                            PF.StreetName,
                                            PF.Level1Type,
                                            PF.Level1Name,
                                            PF.Level2Type,
                                            PF.Level2Name,
                                            PF.Level3Type,
                                            PF.Level3Name,
                                            PF.ApartmentType,
                                            PF.ApartmentName,
                                            PF.Other,
                                            PF.LandAreaTypeCode,
                                            PF.LandAreaTypeValue,
                                            PF.LandAreaValue,
                                            PF.PropAreaValue,
                                            PF.PropArea,
                                            PF.PropBltUpArea,
                                            PF.PropExt,
                                            PF.PropDpth,
                                            PF.PropOccDpth,
                                            PF.PropVol,
                                            PF.PropHght,
                                            PF.FloorsCount,
                                            PF.PropYearBlt,
                                            PF.PropYearCom,
                                            PF.BuildMatCode,
                                            PF.BuildMatValue,
                                            PF.RoomPurposeCode,
                                            PF.RoomPurposeValue,
                                            PF.FloorNumber,
                                            PF.ParentCadNumber,
                                            PF.ParentRoomCadNumber,
                                            PF.ParentLandCadNumber,
                                            PF.PropCostValue,
                                            PF.PropCostRegDate,
                                            PF.PropCostAppDate,
                                            RI.RegDate,
                                            RI.RightNumber,
                                            RI.RightType,
                                            RI.RightCode,
                                            RSI.RegDate,
                                            RSI.RightRestricRegNum,
                                            RSI.RightRestRegNumCod,
                                            RSI.RightRestRegNumVal,
                                            CRCN.ChildRoomCadNumber,
                                            CCSC.ChildCarSpaceCadNumber
                                            FROM
                                            egrn.PropertyInfo AS PF 
                                            LEFT JOIN egrn.RightInfo AS RI ON PF.cadnumber = RI.cadnumber   
                                            LEFT JOIN egrn.RestrictionInfo AS RSI ON PF.cadnumber = RSI.cadnumber   
                                            LEFT JOIN egrn.ChildRoomCadNumbers AS CRCN ON PF.CadNumber= CRCN.RecordNumber   
                                            LEFT JOIN egrn.ChildCarSpaceCadNumbers AS CCSC ON PF.CadNumber = CCSC.RecordNumber  
                                            WHERE PF.CadNumber = '${CadNumberArg}'  
                                            LIMIT 10''',
                                   'priority': 'NORMAL'],
                        headers        : ['Content-Type': 'application/x-www-form-urlencoded', 'Accept-Version': '1']
                ] as LinkedHashMap,
                arguments: [CadNumberArg: '59:09:0024014:31']
        ] as FieldComponent
    }

    def static getPropOldNNumbFieldComponent() {
        [
                id       : 'brc2',
                type     : 'BackRestCall',
                attrs    : [
                        method         : 'POST',
                        url            : 'http://pgu-uat-feddsnlb.test.gosuslugi.ru',
                        path           : '/internal/api/einfahrt',
                        formData: ['sql': '''SELECT 
                                            PF.TypeCode,
                                            PF.PropertyTypeValue,
                                            PF.CadNumber,
                                            PF.RegistrationDate,
                                            PF.PropOldNTypeValue,
                                            PF.PropOldNNumb,
                                            PF.PropOldNAssDate,
                                            PF.FIAS,
                                            PF.KLADRCode,
                                            PF.Oktmo,
                                            PF.PostalCode,
                                            PF.RegionCode,
                                            PF.RegionValue,
                                            PF.DistrictType,
                                            PF.DistrictName,
                                            PF.CityType,
                                            PF.CityName,
                                            PF.UrbanDistricType,
                                            PF.UrbanDistricName,
                                            PF.SovietVillagType,
                                            PF.NameSovietVill,
                                            PF.LocalityType,
                                            PF.LocalityName,
                                            PF.StreetType,
                                            PF.StreetName,
                                            PF.Level1Type,
                                            PF.Level1Name,
                                            PF.Level2Type,
                                            PF.Level2Name,
                                            PF.Level3Type,
                                            PF.Level3Name,
                                            PF.ApartmentType,
                                            PF.ApartmentName,
                                            PF.Other,
                                            PF.LandAreaTypeCode,
                                            PF.LandAreaTypeValue,
                                            PF.LandAreaValue,
                                            PF.PropAreaValue,
                                            PF.PropArea,
                                            PF.PropBltUpArea,
                                            PF.PropExt,
                                            PF.PropDpth,
                                            PF.PropOccDpth,
                                            PF.PropVol,
                                            PF.PropHght,
                                            PF.FloorsCount,
                                            PF.PropYearBlt,
                                            PF.PropYearCom,
                                            PF.BuildMatCode,
                                            PF.BuildMatValue,
                                            PF.RoomPurposeCode,
                                            PF.RoomPurposeValue,
                                            PF.FloorNumber,
                                            PF.ParentCadNumber,
                                            PF.ParentRoomCadNumber,
                                            PF.ParentLandCadNumber,
                                            PF.PropCostValue,
                                            PF.PropCostRegDate,
                                            PF.PropCostAppDate,
                                            RI.RegDate,
                                            RI.RightNumber,
                                            RI.RightType,
                                            RI.RightCode,
                                            RSI.RegDate,
                                            RSI.RightRestricRegNum,
                                            RSI.RightRestRegNumCod,
                                            RSI.RightRestRegNumVal,
                                            CRCN.ChildRoomCadNumber,
                                            CCSC.ChildCarSpaceCadNumber
                                            FROM
                                            egrn.PropertyInfo AS PF 
                                            LEFT JOIN egrn.RightInfo AS RI ON PF.cadnumber = RI.cadnumber   
                                            LEFT JOIN egrn.RestrictionInfo AS RSI ON PF.cadnumber = RSI.cadnumber   
                                            LEFT JOIN egrn.ChildRoomCadNumbers AS CRCN ON PF.CadNumber= CRCN.RecordNumber   
                                            LEFT JOIN egrn.ChildCarSpaceCadNumbers AS CCSC ON PF.CadNumber = CCSC.RecordNumber  
                                            WHERE PF.PropOldNNumb = '${PropOldNNumbArg}'  
                                            LIMIT 10''',
                                   'priority': 'NORMAL'],
                        headers        : ['Content-Type': 'application/x-www-form-urlencoded', 'Accept-Version': '1']
                ] as LinkedHashMap,
                arguments: [PropOldNNumbArg: '59-59-17/211/2014-663']
        ] as FieldComponent
    }
}
