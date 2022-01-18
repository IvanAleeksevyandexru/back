package ru.gosuslugi.pgu.fs.pgu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.dto.catalog.CatalogInfoDtoList;
import ru.gosuslugi.pgu.fs.pgu.service.CatalogService;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final RestTemplate restTemplate;

    @Value("${pgu.order-url}")
    private String pguUrl;

    private final static String CATALOG_INFO = "/api/catalog/v3/targetsInfo/get?targetIdList={targetIdList}";

    @Override
    public CatalogInfoDtoList getCatalogInfoByTargetId(String targetId) {
        var catalogInfoDtoList = new CatalogInfoDtoList();
        try{
            Map<String, String> requestParameters = Map.of(
                    "targetIdList", targetId);
            ResponseEntity<CatalogInfoDtoList> response = restTemplate
                    .exchange(pguUrl + CATALOG_INFO,
                            HttpMethod.GET,
                            new HttpEntity<>(this.prepareHeaders()),
                            CatalogInfoDtoList.class,
                            requestParameters);
            if (Objects.nonNull(response.getBody())) {
                catalogInfoDtoList =  response.getBody();
            }
        } catch (Exception e){
            log.error("API Каталога опять недоступно. Игнорируем");
        }
        return catalogInfoDtoList;
    }

    private HttpHeaders prepareHeaders() {
        return new HttpHeaders();
    }

}
