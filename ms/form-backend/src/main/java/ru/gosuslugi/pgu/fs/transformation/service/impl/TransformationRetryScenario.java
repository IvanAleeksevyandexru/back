package ru.gosuslugi.pgu.fs.transformation.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.attachments.AttachmentService;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.AttachmentInfo;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOperation;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.transformation.Transformation;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class TransformationRetryScenario implements Transformation {

    private static final String OBJECT_ID_ATTR_NAME = "objectId";
    private static final String UPLOADS_ATTR_NAME = "uploads";
    private static final String VALUE_ATTR_NAME = "value";

    private final TransformationFallbackToScreen transformationFallbackToScreen;
    private final PguOrderService pguOrderService;
    private final AttachmentService attachmentService;
    private final DraftClient draftClient;
    private final UserPersonalData userPersonalData;
    private final MainDescriptorService mainDescriptorService;


    @Override
    public TransformationOperation getOperation() {
        return TransformationOperation.retryScenario;
    }

    @Override
    public TransformationResult transform(DraftHolderDto draftOrigin, Order orderOrigin, Map<String, Object> spec) {
        TransformationResult fallbackToScreenResults = transformationFallbackToScreen.transform(draftOrigin, orderOrigin, spec);
        if (!fallbackToScreenResults.isTransformed()) {
            return fallbackToScreenResults;
        }
        DraftHolderDto currentDraft = fallbackToScreenResults.getDraftHolderDto();
        ScenarioDto currentDraftBody = currentDraft.getBody();
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(currentDraftBody.getServiceDescriptorId());
        Long newOrderId = pguOrderService.createOrderId(currentDraftBody.getServiceCode(),
                currentDraftBody.getTargetCode(), serviceDescriptor.getOrderType(), serviceDescriptor.getHighloadParameters());
        currentDraft.setOrderId(newOrderId);
        currentDraftBody.setOrderId(newOrderId);
        copyAttachments(draftOrigin, currentDraft, newOrderId, serviceDescriptor);
        Order currentOrder = pguOrderService.findOrderByIdCached(newOrderId);
        draftClient.saveDraft(currentDraftBody, currentDraftBody.getServiceCode(), userPersonalData.getUserId(), userPersonalData.getOrgId(), serviceDescriptor.getDraftTtl(), serviceDescriptor.getOrderTtl());

        return new TransformationResult(true, currentDraft, currentOrder);
    }

    private void copyAttachments(DraftHolderDto source, DraftHolderDto target, Long targetOrderId, ServiceDescriptor serviceDescriptor) {
        Map<String, List<AttachmentInfo>> attachmentInfoMap = attachmentService.copyAttachments(source.getBody(), source.getOrderId(), targetOrderId);
        target.getBody().setAttachmentInfo(attachmentInfoMap);
        attachmentInfoMap.forEach((k,v) -> {
            Optional<FieldComponent> optionalComponent = serviceDescriptor.getFieldComponentById(k);
            if (optionalComponent.isPresent()) {
                ComponentType componentType = optionalComponent.get().getType();
                ApplicantAnswer answer = target.getBody().getApplicantAnswerByFieldId(k);
                updateObjectIdInUploadComponentAnswer(componentType, answer, targetOrderId);
                ApplicantAnswer cachedAnswer = target.getBody().getCachedAnswers().get(k);
                updateObjectIdInUploadComponentAnswer(componentType, cachedAnswer, targetOrderId);
            }
        });
    }

    private void updateObjectIdInUploadComponentAnswer(ComponentType componentType, ApplicantAnswer answer, Long newObjectId) {
        if (Objects.nonNull(answer)) {
            switch (componentType) {
                case FileUploadComponent:
                    LinkedHashMap<String, Object> answerMap = JsonProcessingUtil.fromJson(answer.getValue(), new TypeReference<>() {});
                    List<Map<String, Object>> uploads = (List<Map<String, Object>>) answerMap.get(UPLOADS_ATTR_NAME);
                    uploads.forEach(upload -> {
                        List<Map<String, Object>>  uploadValues = (List<Map<String, Object>> ) upload.get(VALUE_ATTR_NAME);
                        uploadValues.forEach(m -> m.put(OBJECT_ID_ATTR_NAME, newObjectId));
                    });
                    answer.setValue(JsonProcessingUtil.toJson(answerMap));
                    break;
                case PhotoUploadComponent:
                    LinkedHashMap<String, Object> photo = JsonProcessingUtil.fromJson(answer.getValue(), new TypeReference<>() {});
                    photo.put(OBJECT_ID_ATTR_NAME, newObjectId);
                    answer.setValue(JsonProcessingUtil.toJson(photo));
                    break;
            }
        }
    }
}
