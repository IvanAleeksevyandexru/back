package ru.gosuslugi.pgu.fs.service.impl;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.PdfFilePackage;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.TerrabyteFileInfo;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;
import ru.gosuslugi.pgu.fs.common.service.ComputeAnswerService;
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.ScreenFinderService;
import ru.gosuslugi.pgu.fs.common.service.impl.ScenarioDtoServiceImpl;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.file.model.FileUploadAnswerDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
public class FormScenarioDtoServiceImpl extends ScenarioDtoServiceImpl {

    private final UserPersonalData userPersonalData;
    private final DraftClient draftClient;
    private final AdditionalAttributesHelper additionalAttributesHelper;

    public FormScenarioDtoServiceImpl(UserPersonalData userPersonalData, ComponentService componentService, HelperScreenRegistry screenRegistry,
                                      ScreenFinderService screenFinderService, JsonProcessingService jsonProcessingService,
                                      DraftClient draftClient,
                                      DisplayReferenceService displayReferenceService,ComputeAnswerService computeAnswerService,
                                      AdditionalAttributesHelper additionalAttributesHelper)
    {
        super(componentService, screenRegistry, screenFinderService, jsonProcessingService, displayReferenceService, computeAnswerService);
        this.userPersonalData = userPersonalData;
        this.draftClient = draftClient;
        this.additionalAttributesHelper = additionalAttributesHelper;
    }

    /**
     * Устанавливает/обновляет дополнительные атрибуты в DTO
     * @param scenarioDto DTO для обновления
     * @see AdditionalAttributesHelper#updateAdditionalAttributes(ScenarioDto, String)
     */
    public void updateAdditionalAttributes(ScenarioDto scenarioDto, String smevEnv) {
        additionalAttributesHelper.updateAdditionalAttributes(scenarioDto, smevEnv);
    }

    public void updateAdditionalAttributesWithDraftSaving(ScenarioDto scenarioDto, String smevEnv, ServiceDescriptor serviceDescriptor) {
        additionalAttributesHelper.updateAdditionalAttributes(scenarioDto, smevEnv);
        draftClient.saveDraft(scenarioDto, scenarioDto.getServiceCode(), userPersonalData.getUserId(), userPersonalData.getOrgId(), serviceDescriptor.getDraftTtl(), serviceDescriptor.getOrderTtl());
    }

    /**
     * Восстанавливает DTO из черновика и устанавливает начальный экран в зависимости от роли, стейджа и стадии
     * заполнения пользователем
     *
     * @param scenarioDto сценарий сервиса форм
     * @param serviceId   идентификатор услуги
     * @param stage       стейдж
     * @param role        роль пользователя
     */
    public void prepareScenarioDto(ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor, String serviceId, DeliriumStageDto stage, ApplicantRole role) {

        boolean needSaveDraft = super.prepareScenarioDto(scenarioDto, currentDescriptor, serviceId, getFullStage(stage), role);

        if(needSaveDraft) {
            draftClient.saveDraft(scenarioDto, serviceId, userPersonalData.getUserId(), userPersonalData.getOrgId(), currentDescriptor.getDraftTtl(), currentDescriptor.getOrderTtl());
        }

    }

    /**
     * Выполняет группировку загруженных файлов по заданным настройкам (названию файла) в компоненте {@link ComponentType#FileUploadComponent}
     * @param scenarioDto дта, обновляется в результате работы метода
     * @param serviceDescriptor описание услуги
     * @see ScenarioDto#getPackageToPdf()
     */
    public void mergePdfDocuments(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        Set<PdfFilePackage> packageToPdf = new HashSet<>();

        scenarioDto.getApplicantAnswers().entrySet().stream()
                .filter(answerEntry -> ComponentType.FileUploadComponent == serviceDescriptor.getFieldComponentById(answerEntry.getKey()).map(FieldComponent::getType).orElse(null))
                .map(AnswerUtil::getValue)
                .filter(StringUtils::isNotEmpty)
                .map(answerValue -> getJsonProcessingService().fromJson(answerValue, FileUploadAnswerDto.class))
                .filter(fileUpload -> fileUpload.getUploads() != null)
                .flatMap(fileUploadAnswerDto -> fileUploadAnswerDto.getUploads().stream())
                .filter(uploadDto -> !StringUtils.isEmpty(uploadDto.getPdfFileName()))
                .forEach(uploadDto -> {
                    val pdfPackage = packageToPdf
                            .stream()
                            .filter(it -> it.getFilename().equals(uploadDto.getPdfFileName()))
                            .findFirst();
                    val fileInfos = Stream.ofNullable(uploadDto.getValue())
                            .flatMap(Collection::stream)
                            .map(it -> new TerrabyteFileInfo(it.getFileUid(), it.getFileExt()))
                            .collect(Collectors.toSet());
                    if (pdfPackage.isPresent()) {
                        pdfPackage.get().getFileInfos().addAll(fileInfos);
                    } else {
                        val newPack = new PdfFilePackage(uploadDto.getPdfFileName());
                        newPack.setFileInfos(fileInfos);
                        packageToPdf.add(newPack);
                    }
                });
        scenarioDto.setPackageToPdf(packageToPdf);
    }

    /**
     * Возвращает полный статус для заявки {@code <стейдж>_<статус>}
     * @param deliriumStageDto ответ от делириума
     * @return полный статус
     */
    private String getFullStage(DeliriumStageDto deliriumStageDto) {
        String fullStage = deliriumStageDto.getStage();
        if(deliriumStageDto.getComplete() != null) {
            fullStage += "_" + deliriumStageDto.getComplete().name();
        }
        return fullStage;
    }
}
