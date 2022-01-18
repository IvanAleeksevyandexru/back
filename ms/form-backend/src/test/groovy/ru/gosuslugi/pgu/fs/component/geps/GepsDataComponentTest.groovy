package ru.gosuslugi.pgu.fs.component.geps

import ru.gosuslugi.pgu.core.lk.model.feed.Feed
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.BaseComponent
import ru.gosuslugi.pgu.fs.pgu.client.PguClient
import ru.gosuslugi.pgu.fs.pgu.dto.feed.Detail
import ru.gosuslugi.pgu.fs.pgu.dto.feed.FeedDto
import ru.gosuslugi.pgu.fs.pgu.dto.feed.Params
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService
import spock.lang.Specification

import static ru.gosuslugi.pgu.fs.common.component.ComponentResponse.of
import static ru.gosuslugi.pgu.fs.common.component.ComponentResponse.empty

class GepsDataComponentTest extends Specification {
    def "GetInitialValue - testing pgu integration"() {
        // expected values
        Long gepsId = 111L

        // given objects
        PguClient pguClient = Mock()
        NsiDictionaryService nsiDictionaryService = Mock();
        ScenarioDto scenarioDto = new ScenarioDto(gepsId: gepsId)
        BaseComponent<GepsData> gepsComponent = new GepsDataComponent(pguClient,nsiDictionaryService)

        when:"setting initial value"
        gepsComponent.getInitialValue(Mock(FieldComponent), scenarioDto)

        then:"pgu client should be called"
        1 * pguClient.findFeed(Feed.FeedType.GEPS, gepsId)

    }

    def "GetInitialValue - checking response values"() {
        given:
        BaseComponent<GepsData> component = new GepsDataComponent(pguClient, Mock(NsiDictionaryService))

        when:
        def actual = component.getInitialValue(Mock(FieldComponent), new ScenarioDto())

        then:
        expected == actual

        where:
        pguClient                                                                                                                | expected
        // no NPE
        pguStub((FeedDto)null)                                                                                                   | empty()
        pguStub(new FeedDto(detail: new Detail()))                                                                               | empty()
        pguStub((Params)null)                                                                                                    | empty()

        // blank values
        pguStub(new Params(orgINN: "   ", orgName: "name", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "    ", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber: "   ", mobileNetworkOperator: "op", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber: "999", mobileNetworkOperator: "  ", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber: "   ")) | empty()

        // empty values
        pguStub(new Params(orgINN:    "", orgName: "name", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName:     "", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber:    "", mobileNetworkOperator: "op", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber: "999", mobileNetworkOperator:   "", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber:    "")) | empty()

        // null values
        pguStub(new Params(orgINN:  null, orgName: "name", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName:   null, phoneNumber: "999", mobileNetworkOperator: "op", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber:  null, mobileNetworkOperator: "op", routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber: "999", mobileNetworkOperator: null, routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber:  null)) | empty()

        // random null/empty/blank values
        pguStub(new Params(orgINN:  null, orgName: "name", phoneNumber:  null, mobileNetworkOperator: "op", routeNumber:  null)) | empty()
        pguStub(new Params(orgINN: "666", orgName:   null, phoneNumber: "999", mobileNetworkOperator: null, routeNumber: "333")) | empty()
        pguStub(new Params(orgINN: "666", orgName: "    ", phoneNumber:  null, mobileNetworkOperator: "op", routeNumber:  null)) | empty()
        pguStub(new Params(orgINN: "   ", orgName: "name", phoneNumber:    "", mobileNetworkOperator: null, routeNumber: "333")) | empty()

        // correct value
        pguStub(new Params(orgINN: "666", orgName: "name", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber: "333")) | of(new GepsData(orgName: "name", orgINN: "666", phoneNumber: "999", mobileNetworkOperator: "op", routeNumber: "333", operatorId: null))
    }

    def pguStub(FeedDto dto) {
        PguClient client = Stub()
        client.findFeed(_, _) >> dto
        return client
    }

    def pguStub(Params params) {
        return pguStub(new FeedDto(detail: new Detail(params: params)))
    }

    def "PreloadComponent - putting component value into answers"() {
        // expected values
        String componentId = "gepsData"
        String componentValue = "testValue"

        // given objects
        FieldComponent fieldComponent = new FieldComponent(id: componentId, value: componentValue)
        ScenarioDto scenarioDto = new ScenarioDto(applicantAnswers: [:])
        BaseComponent<GepsData> gepsComponent = new GepsDataComponent(Mock(PguClient), Mock(NsiDictionaryService))

        when:"preloading component"
        gepsComponent.preloadComponent(fieldComponent, scenarioDto)

        then:"value should be put into the answers"
        scenarioDto.getApplicantAnswerByFieldId(componentId) != null
        scenarioDto.getApplicantAnswerByFieldId(componentId).getValue() == componentValue
    }
}
