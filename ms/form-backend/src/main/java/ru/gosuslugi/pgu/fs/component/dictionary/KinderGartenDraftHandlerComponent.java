package ru.gosuslugi.pgu.fs.component.dictionary;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.atc.idecs.common.util.ws.type.AttributeType;
import ru.atc.idecs.common.util.ws.type.AttributeValue;
import ru.atc.idecs.refregistry.ws.Condition;
import ru.atc.idecs.refregistry.ws.ListRefItemsRequest;
import ru.atc.idecs.refregistry.ws.ListRefItemsResponse;
import ru.atc.idecs.refregistry.ws.LogicalUnionKind;
import ru.atc.idecs.refregistry.ws.LogicalUnionPredicate;
import ru.atc.idecs.refregistry.ws.Predicate;
import ru.atc.idecs.refregistry.ws.RefItem;
import ru.atc.idecs.refregistry.ws.SimplePredicate;
import ru.gosuslugi.pgu.client.draftconverter.DraftConverterClient;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto.BarBarBokResponseDtoBuilder;
import ru.gosuslugi.pgu.common.certificate.service.marshaller.KinderGartenXmlMarshaller;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.common.kindergarten.xsd.dto.FormDataResponse;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.XmlDraftConvertRequest;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.service.impl.ComputeDictionaryItemService;
import ru.gosuslugi.pgu.fs.common.utils.NsiUtils;
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry;
import ru.gosuslugi.pgu.fs.common.variable.VariableType;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenConverterDto;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenInputParams;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenNsiResponse;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.KinderGartenOutputParams;
import ru.gosuslugi.pgu.fs.service.KinderGartenClient;
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.debug;
import static ru.gosuslugi.pgu.components.ComponentAttributes.TIMEOUT_ATTR;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.KinderGartenDraftHandler;
import static ru.gosuslugi.pgu.dto.kindergarten.KinderGartenHandlerStatus.ConverterError;
import static ru.gosuslugi.pgu.dto.kindergarten.KinderGartenHandlerStatus.NsiError;
import static ru.gosuslugi.pgu.dto.kindergarten.KinderGartenHandlerStatus.SmevError;
import static ru.gosuslugi.pgu.dto.kindergarten.KinderGartenHandlerStatus.TerrabyteError;

/** Компонент для запроса данных сертификата финансирования дополнительного образования детсадов. */
@Slf4j
@Component
@RequiredArgsConstructor
public class KinderGartenDraftHandlerComponent extends AbstractComponent<String> {
    private static final String TYPE_OF_REQUEST = "typeOfRequest";
    private static final String SMEV_VERSION = "smevVersion";
    private static final String PASS_CODE = "passCode";

    private final KinderGartenClient kinderGartenClient;
    private final KinderGartenXmlMarshaller kinderGartenXmlMarshaller;
    private final TerrabyteClient terrabyteClient;
    private final UserPersonalData userPersonalData;
    private final DraftConverterClient draftConverterClient;
    private final ComputeDictionaryItemService dictionaryItemService;
    private final VariableRegistry variableRegistry;

    @Override
    public ComponentType getType() {
        return KinderGartenDraftHandler;
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent component) {
        int timeout = Integer.parseInt(component.getArgument(TIMEOUT_ATTR, "30000"));
        String typeOfRequest = component.getArgument(TYPE_OF_REQUEST);
        String smevVersion = component.getArgument(SMEV_VERSION);
        String passCode = component.getArgument(PASS_CODE);
        KinderGartenInputParams inputParams = new KinderGartenInputParams(timeout, typeOfRequest, smevVersion, passCode, scenarioDto.getOrderId());

        Map<String, ApplicantAnswer> applicantAnswers = scenarioDto.getApplicantAnswers();
        KinderGartenOutputParams outputParams = createXmlByInputParams(inputParams, component, applicantAnswers);
        if (outputParams == null) return;

        BarBarBokResponseDtoBuilder responseDtoBuilder = barbarbokSearch(outputParams.getXmlRequest(), component, applicantAnswers, smevVersion, scenarioDto.getOrderId(), timeout);
        if (responseDtoBuilder == null) return;

        FormDataResponse dataResponse = unmarshallSmevResponse(responseDtoBuilder, component, applicantAnswers);
        if (dataResponse == null) return;

        Map<String, FileInfo> terrabyteFiles = getFileInfoFromTerrabyte(responseDtoBuilder, component, applicantAnswers, scenarioDto.getOrderId());
        if (terrabyteFiles == null) return;

        String smevDataJson = jsonProcessingService.toJson(dataResponse);
        debug(log, () -> String.format("Smev data response in json format: {%s}, orderId=%s, userId=%s",
                smevDataJson, scenarioDto.getOrderId(), userPersonalData.getUserId()));
        String jsonPath = (String) component.getAttrs().get("jsonPath");
        List<String> values = null;
        try {
            DocumentContext dc = JsonPath.parse(smevDataJson);
            Object value = dc.read(jsonPath);
            if (value instanceof JSONArray) {
                values = ((JSONArray)value).stream().map(Object::toString).collect(Collectors.toList());
            } else if(value instanceof String) {
                values = Collections.singletonList((String)value);
            }
        } catch (PathNotFoundException e) {
            responseDtoBuilder.status(SmevError.name()).errorMessage(ExceptionUtils.getRootCauseMessage(e));
            applicantAnswers.put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(responseDtoBuilder.build())));
            return;
        }
        if (values == null) {
            responseDtoBuilder.status(SmevError.name()).errorMessage("Barbarbok answer doesn't contain the values by jsonPath");
            applicantAnswers.put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(responseDtoBuilder.build())));
            return;
        }
        String dictionaryQuery = (String) component.getAttrs().get("dictionaryQuery");
        Map<String, Set<String>> replacements = new HashMap<>();
        replacements.put("${eduCode}", new HashSet<>(values));
        replacements.put("${OKTMO}", Collections.singleton(outputParams.getUserSelectedRegion().substring(0, 2)));
        debug(log, () -> String.format("Kindergarten nsi template replacements: {%s}, orderId=%s, userId=%s",
                replacements, scenarioDto.getOrderId(), userPersonalData.getUserId()));

        ListRefItemsRequest nsiRequest = JsonProcessingUtil.fromJson(dictionaryQuery, ListRefItemsRequest.class);
        Optional.ofNullable(nsiRequest)
                .map(ListRefItemsRequest::getFilter)
                .map(Predicate::getUnion)
                .map(LogicalUnionPredicate::getSubs)
                .stream().flatMap(Collection::stream)
                .forEach(predicate -> modifyPredicate(replacements, predicate));

        String dictionaryName = (String) component.getAttrs().get("dictionaryName");
        KinderGartenConverterDto jsonData = new KinderGartenConverterDto();
        jsonData.setNsiRegion(outputParams.getRegionResponse());
        jsonData.setTerrabyteFiles(terrabyteFiles);
        try {
            ListRefItemsResponse responseItems = dictionaryItemService.getDictionaryItems(nsiRequest, dictionaryName);
            Map<String, RefItem> items = Optional.of(responseItems).map(ListRefItemsResponse::getItems).stream().flatMap(Collection::stream)
                    .collect(LinkedHashMap::new,
                            (map, refItem) -> map.put(refItem.getValue(), refItem),
                            LinkedHashMap::putAll);
            long total = responseItems.getTotal();
            KinderGartenNsiResponse nsiResponse = new KinderGartenNsiResponse();
            nsiResponse.setTotal(total);
            nsiResponse.setItems(items);
            jsonData.setNsiResponse(nsiResponse);
        } catch (Exception e) {
            responseDtoBuilder.status(NsiError.name()).errorMessage(ExceptionUtils.getRootCauseMessage(e));
            applicantAnswers.put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(responseDtoBuilder.build())));
            return;
        }

        String serviceId = variableRegistry.getVariable(VariableType.serviceId).getValue(scenarioDto);

        XmlDraftConvertRequest request = new XmlDraftConvertRequest(responseDtoBuilder.build().getData());
        String json = jsonProcessingService.toJson(jsonData);
        request.setJsonData(json);
        request.setServiceId(Optional.ofNullable(serviceId).orElseGet(scenarioDto::getServiceCode));

        try {
            ScenarioDto convertedApplicantAnswers = draftConverterClient.convertXmlDraft(request, timeout);
            Map<String, ApplicantAnswer> applicantAnswersFromConverter = convertedApplicantAnswers.getApplicantAnswers();
            applicantAnswers.putAll(applicantAnswersFromConverter);
        } catch (Exception e) {
            responseDtoBuilder.status(ConverterError.name()).errorMessage(ExceptionUtils.getRootCauseMessage(e));
            applicantAnswers.put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(responseDtoBuilder.build())));
        }
    }

    /**
     * Подготовка сообщения для СМЭВ по входным данным, запись в статус кода с ошибкой и сохранение в {@link ScenarioDto#getApplicantAnswers()}.
     * @param inputParams входные параметры для поиска информации по дет.садам
     * @param component компонент
     * @param applicantAnswers ответы пользователя для каждого компонента
     * @return
     */
    private KinderGartenOutputParams createXmlByInputParams(KinderGartenInputParams inputParams,
                                                            FieldComponent component, Map<String, ApplicantAnswer> applicantAnswers) {
        KinderGartenOutputParams outputParams = kinderGartenClient.createXmlByInputParams(component, inputParams);
        if (outputParams.getMessage() != null) {
            applicantAnswers.put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(outputParams)));
            return null;
        }
        debug(log, () -> String.format("Process xml creation with inputParams={%s}, outputParams={%s}", inputParams, outputParams));
        return outputParams;
    }

    /**
     * Отправка сообщение в СМЭВ и сихронное получение ответа от СМЭВ,
     * в случае ошибки происходит запись статус-кода и причины ошибки, затем сохранение в {@link ScenarioDto#getApplicantAnswers()}.
     * @param xmlRequest xml для отправки в СМЭВ
     * @param component компонент
     * @param applicantAnswers ответы пользователя для каждого компонента
     * @param smevVersion версия СМЭВ
     * @param orderId id черновика
     * @param timeout таймаут
     * @return ответное сообщение от СМЭВ-адаптера и возможное сообщение с ошибкой
     */
    private BarBarBokResponseDtoBuilder barbarbokSearch(String xmlRequest,
                                                 FieldComponent component, Map<String, ApplicantAnswer> applicantAnswers,
                                                 String smevVersion, long orderId, int timeout) {
        BarBarBokResponseDtoBuilder builder = kinderGartenClient.sendMessage(xmlRequest, smevVersion, orderId, timeout);
        if (builder.build().getData() == null) {
            applicantAnswers.put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(builder.build())));
            return null;
        }
        debug(log, () -> String.format("Process send and receive jms message for SMEV adapter with responseDto={%s}", builder));
        return builder;
    }

    /**
     * Анмаршаллинг xml-ответа от СМЭВ в java-объект,
     * в случае ошибки происходит запись статус-кода и причины ошибки, затем сохранение в {@link ScenarioDto#getApplicantAnswers()}.
     * @param responseDtoBuilder ответ от СМЭВ
     * @param component компонент
     * @param applicantAnswers ответы пользователя для каждого компонента
     * @return ответ от СМЭВ в виде объекта
     */
    private FormDataResponse unmarshallSmevResponse(BarBarBokResponseDtoBuilder responseDtoBuilder, FieldComponent component, Map<String, ApplicantAnswer> applicantAnswers) {
        FormDataResponse dataResponse = null;
        try {
            dataResponse = kinderGartenXmlMarshaller.unmarshal(responseDtoBuilder.build().getData());
        } catch (Exception e) {
            responseDtoBuilder.status(SmevError.name()).errorMessage(ExceptionUtils.getRootCauseMessage(e));
            applicantAnswers.put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(responseDtoBuilder.build())));
        }
        debug(log, () -> String.format("Process unmarhalling receiving message from SMEV adapter with responseDto={%s}", responseDtoBuilder));
        return dataResponse;
    }

    /**
     * Получение файлов загруженных в сервис "Терабайт" по orderId
     * @param responseDtoBuilder ответное сообщение от СМЭВ-адаптера и возможное сообщение с ошибкой
     * @param component компонент
     * @param applicantAnswers ответы пользователя для каждого компонента
     * @param orderId id черновика
     * @return мапка файлов - в ключе мнемоника, в значении информация по файлу
     */
    private Map<String, FileInfo> getFileInfoFromTerrabyte(BarBarBokResponseDtoBuilder responseDtoBuilder, FieldComponent component, Map<String, ApplicantAnswer> applicantAnswers,
                                                           long orderId) {
        Map<String, FileInfo> terrabyteFiles = null;
        try {
            List<FileInfo> fileInfoList = terrabyteClient.getAllFilesByOrderId(orderId, userPersonalData.getUserId(), userPersonalData.getToken());
            terrabyteFiles = Optional.ofNullable(fileInfoList).stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(FileInfo::getMnemonic, Function.identity()));
        } catch (Exception e) {
            responseDtoBuilder.status(TerrabyteError.name()).errorMessage(ExceptionUtils.getRootCauseMessage(e));
            applicantAnswers.put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(responseDtoBuilder.build())));
        }
        debug(log, () -> String.format("Process terrabyte request with orderId=%s, userId=%s, token=%s",
                orderId, userPersonalData.getUserId(), userPersonalData.getToken()));
        return terrabyteFiles;
    }

    /**
     * Осуществляет копирование нужного количества simple-фильтров по шаблону.
     * @param replacementValues мапа имени атрибута на список его значений из каждого элемента ответа nsi-сервиса для подстановки в шаблон nsi-запроса набора фильтров
     * @param predicate фильтр из тела запроса, в который динамически добавляется нужное количество простых фильтров со значениями
     * @return линк, связывающий имя атрибута из значений предыдущего ответа от nsi-справочника
     * и имя атрибута в фильтре из текущего запроса, в которое нужно подставить значения из предыдущего ответа
     */
    private static void modifyPredicate(Map<String, Set<String>> replacementValues, Predicate predicate) {
        if (predicate.getSimple() != null) {
            SimplePredicate simple = predicate.getSimple();
            String attributeName = simple.getAttributeName();
            AttributeValue value = simple.getValue();
            AttributeType type = value.getTypeOfValue();
            Condition condition = simple.getCondition();
            String variable = Optional.ofNullable(value.getValue()).map(Object::toString).orElse(attributeName);
            Set<String> values = replacementValues.get(variable);
            if (!CollectionUtils.isEmpty(values)) {
                SimplePredicate simplePredicate = NsiUtils.getSimplePredicate(attributeName, type, values.iterator().next(), condition);
                predicate.setSimple(simplePredicate);
            }
        }
        if (predicate.getUnion() != null) {
            LogicalUnionPredicate union = predicate.getUnion();
            LogicalUnionKind internalUnionKind = union.getUnionKind();
            Predicate dynamicPredicate = union.getSubs().get(0);
            if (dynamicPredicate != null && dynamicPredicate.getSimple() != null) {
                union.getSubs().remove(dynamicPredicate);
                SimplePredicate simplePredicate = dynamicPredicate.getSimple();
                String attributeName = simplePredicate.getAttributeName();
                AttributeValue value = simplePredicate.getValue();
                AttributeType type = value.getTypeOfValue();
                Condition condition = simplePredicate.getCondition();
                String variable = Optional.ofNullable(value.getValue()).map(Object::toString).orElse(attributeName);
                Set<String> values = replacementValues.get(variable);
                if (!CollectionUtils.isEmpty(values)) {
                    LogicalUnionPredicate unionPredicate = NsiUtils.getUnionPredicate(internalUnionKind, attributeName, type, condition, values.toArray(new Object[0]));
                    union.getSubs().add(new Predicate(unionPredicate));
                }
            }
        }

    }
}
