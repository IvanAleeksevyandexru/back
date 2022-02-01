package ru.gosuslugi.pgu.fs.component.geps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.core.lk.model.feed.Feed;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.pgu.client.PguFeedClient;
import ru.gosuslugi.pgu.fs.pgu.dto.feed.FeedDto;
import ru.gosuslugi.pgu.fs.pgu.dto.feed.Params;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.Objects;

/**
 * Компонент для получения данных из письма ГЭПС.
 * Может использоваться, как preload, данные будут подгружены в applicantAnswers с флагом {@code visited = false}.
 * Если хотя бы одно из требуемых полей отсутствует в письме, то данные не будут подгружаться (пустой ответ).
 * @see ScenarioDto#getApplicantAnswers()
 * @see AbstractComponent#preloadComponent(FieldComponent, ScenarioDto)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GepsDataComponent extends AbstractComponent<GepsData> {
    private final PguFeedClient pguFeedClient;

    private final NsiDictionaryService nsiDictionaryService;

    @Override
    public ComponentType getType() {
        return ComponentType.GepsData;
    }

    @Override
    public ComponentResponse<GepsData> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        FeedDto feed = pguFeedClient.findFeed(Feed.FeedType.GEPS, scenarioDto.getGepsId());

        if(hasRequiredData(feed)) {
            Params feedParams = feed.getDetail().getParams();
            GepsData gepsData = new GepsData();
            gepsData.setOrgINN(feedParams.getOrgINN());
            gepsData.setOrgName(feedParams.getOrgName());
            gepsData.setMobileNetworkOperator(feedParams.getMobileNetworkOperator());
            gepsData.setPhoneNumber(feedParams.getPhoneNumber());
            gepsData.setRouteNumber(feedParams.getRouteNumber());
            NsiDictionary gepsDictionary = nsiDictionaryService.getGepsDictionary();
            if(Objects.nonNull(gepsDictionary) && Objects.nonNull(gepsDictionary.getItems())){
                gepsDictionary
                        .getItems()
                        .stream()
                        .filter(i -> Objects.nonNull(i.getAttributeValue("routeNumber"))
                                && i.getAttributeValue("routeNumber").equalsIgnoreCase(feedParams.getRouteNumber()))
                        .findAny().ifPresent(v-> gepsData.setOperatorId(v.getAttributeValue("operatorId")));
            }
            return ComponentResponse.of(gepsData);
        }

        log.warn("Can't find appropriate geps data. GepsId: {}, feed response: {}", scenarioDto.getGepsId(), feed);
        return ComponentResponse.empty();
    }

    @Override
    public void preloadComponent(FieldComponent component, ScenarioDto scenarioDto) {
        scenarioDto.getApplicantAnswers().put(component.getId(), new ApplicantAnswer(false, component.getValue()));
    }

    /**
     * Осуществляет проверку на наличие необходиммых данных в письме
     * @param feed элемент ленты
     * @return имеются ли все необходимые данные
     */
    private boolean hasRequiredData(FeedDto feed) {
        return feed != null
                && feed.getDetail() != null
                && feed.getDetail().getParams() != null
                && StringUtils.isNoneBlank(feed.getDetail().getParams().getMobileNetworkOperator())
                && StringUtils.isNoneBlank(feed.getDetail().getParams().getOrgINN())
                && StringUtils.isNoneBlank(feed.getDetail().getParams().getOrgName())
                && StringUtils.isNoneBlank(feed.getDetail().getParams().getPhoneNumber())
                && StringUtils.isNoneBlank(feed.getDetail().getParams().getRouteNumber());
    }

}
