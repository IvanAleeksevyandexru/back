package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.AttachmentInfo;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.SpAdapterDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.booking.service.BookingService;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;
import ru.gosuslugi.pgu.fs.exception.TerrabyteFileCheckException;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.service.OrderAttributesService;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.DeliriumService;
import ru.gosuslugi.pgu.fs.service.IntegrationService;
import ru.gosuslugi.pgu.fs.service.ParticipantService;
import ru.gosuslugi.pgu.fs.service.TerrabyteService;
import ru.gosuslugi.pgu.fs.sp.ServiceProcessingClient;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationServiceImpl implements IntegrationService {

    private static final String SENT_TIME_KEY = "sentTime";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final String FILES_NOT_FOUND_IN_FILE_STORAGE = "В файловом хранилище отсутствуют необходимые файлы";

    private final UserPersonalData userPersonalData;
    private final ServiceProcessingClient serviceProcessingClient;
    private final DeliriumService deliriumService;
    private final PguOrderService pguOrderService;
    private final TerrabyteService terrabyteService;
    private final ParticipantService participantService;
    private final DraftClient draftClient;
    private final BookingService bookingService;
    private final ErrorModalDescriptorService errorModalDescriptorService;
    private final OrderAttributesService orderAttributesService;

    @Value("${spring.kafka.producer.sp-adapter-batch.enabled}")
    private boolean spKafkaEnabled;

    @Override
    public void performIntegrationSteps(ScenarioResponse scenarioResponse, String serviceId, ServiceDescriptor descriptor) {
        var scenarioDto = scenarioResponse.getScenarioDto();
        var display = scenarioDto.getDisplay();
        if (display.isTerminal() && !display.isNotSendToSp()) {
            terrabyteService.deleteRedundantFiles(scenarioDto, descriptor);
            participantService.deleteRedundantParticipants(scenarioDto, descriptor);
            boolean notDeliriumScenario = descriptor.checkOnlyOneApplicantAndStage() && CollectionUtils.isEmpty(scenarioResponse.getScenarioDto().getParticipants());
            if (notDeliriumScenario && !display.isForceDeliriumCall()) {
                beforeSendingToSPAdapter(scenarioResponse, serviceId, descriptor);
                log.info("before sending SP Adapter for order "+ scenarioResponse.getScenarioDto().getOrderId());
                sendToSPAdapter(scenarioResponse, descriptor);
                log.info("after in integration steps "+ scenarioResponse.getScenarioDto().getOrderId());
                afterSendingToSPAdapter(serviceId, scenarioResponse);
            } else {
                deliriumService.notifyScenarioEnd(scenarioResponse.getScenarioDto());
            }
        }
    }

    private void beforeSendingToSPAdapter(ScenarioResponse scenarioResponse, String serviceId, ServiceDescriptor descriptor) {

        checkTerrabyteForFiles(scenarioResponse, descriptor);
        setSentTime(scenarioResponse.getScenarioDto(), serviceId, descriptor);
        checkOrderExists(scenarioResponse.getScenarioDto().getOrderId(), descriptor);
    }


    private void checkOrderExists(Long orderId, ServiceDescriptor serviceDescriptor){
        if(Objects.nonNull(serviceDescriptor.getHighloadParameters()) &&
                serviceDescriptor.getHighloadParameters().isEnabled()){
            pguOrderService.checkOrderExists(orderId);
        }
    }

    private void afterSendingToSPAdapter(String serviceId, ScenarioResponse scenarioResponse) {
        log.info("After sending SP Adapter for order "+ scenarioResponse.getScenarioDto().getOrderId());
        bookingService.sendBookingInfo(serviceId, scenarioResponse.getScenarioDto());
        if (!spKafkaEnabled) {
            deleteFromPguOrderService(scenarioResponse);
            return;
        }
        orderAttributesService.updateTechOrderStatus(OrderStatuses.REGISTERED_ON_PORTAL.getStatusId(),
                scenarioResponse.getScenarioDto().getOrderId()
        );
    }

    private void setSentTime(ScenarioDto scenarioDto, String serviceId, ServiceDescriptor descriptor) {

        // Добавление времени отправвки заявления
        ApplicantAnswer answer = new ApplicantAnswer();
        answer.setValue(ZonedDateTime.now().format(DATE_TIME_FORMATTER));
        scenarioDto.getApplicantAnswers().put(SENT_TIME_KEY, answer);

        // сохранение черновика
        draftClient.saveDraft(scenarioDto, serviceId, userPersonalData.getUserId(), userPersonalData.getOrgId(), descriptor.getDraftTtl(), descriptor.getOrderTtl());
    }

    private void sendToSPAdapter(ScenarioResponse scenarioResponse, ServiceDescriptor descriptor) {

        String targetTopic = null;
        if(descriptor.isSendToServiceTopic()){
            targetTopic = "form-service-"+scenarioResponse.getScenarioDto().getServiceCode()+"-service-topic";
        }

        SpAdapterDto spAdapterDto = new SpAdapterDto(
                scenarioResponse.getScenarioDto().getServiceCode(),
                scenarioResponse.getScenarioDto().getTargetCode(),
                scenarioResponse.getScenarioDto().getOrderId(),
                userPersonalData.getUserId(),
                ApplicantRole.Applicant.name(),
                null,
                userPersonalData.getOrgId(),
                false
        );

        //Если использовался УКЭП при заполнении
        if (scenarioResponse.getScenarioDto().getSignInfoMap().size() > 0) {
            serviceProcessingClient.sendSigned(spAdapterDto, targetTopic);
            return;
        }
        serviceProcessingClient.send(spAdapterDto, targetTopic);
    }

    private void deleteFromPguOrderService(ScenarioResponse scenarioResponse) {
        pguOrderService.findDrafts(scenarioResponse.getScenarioDto().getServiceCode(), scenarioResponse.getScenarioDto().getTargetCode())
                .forEach(draft -> pguOrderService.deleteOrderById(draft.getId()));
    }

    private void checkTerrabyteForFiles(ScenarioResponse scenarioResponse, ServiceDescriptor serviceDescriptor) {
        DraftHolderDto draft = draftClient.getDraftById(
                scenarioResponse.getScenarioDto().getOrderId(),
                userPersonalData.getUserId(),
                userPersonalData.getOrgId()
        );

        if (Objects.nonNull(draft) && Objects.nonNull(draft.getBody())) {
            List<FileInfo> storedFiles = terrabyteService.getAllFilesInfoForOrderId(scenarioResponse.getScenarioDto());
            Map<String, List<AttachmentInfo>> attachmentInfos = draft.getBody().getAttachmentInfo();

            if (Objects.nonNull(attachmentInfos) && !attachmentInfos.isEmpty()) {
                List<ScreenDescriptor> finishedAndCurrentScreens = draft.getBody().getFinishedAndCurrentScreens().stream()
                        .map(serviceDescriptor::getScreenDescriptorById)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                List<String> visitedComponentIds = finishedAndCurrentScreens.stream()
                        .map(serviceDescriptor::getFieldComponentsForScreen)
                        .flatMap(Collection::stream)
                        .map(FieldComponent::getId)
                        .collect(Collectors.toList());
                List<String> uploadFileNames = attachmentInfos.entrySet().stream()
                        .filter(entry -> visitedComponentIds.contains(entry.getKey()))
                        .flatMap(stringListEntry -> stringListEntry.getValue().stream())
                        .map(AttachmentInfo::getUploadFilename)
                        .collect(Collectors.toList());

                // отсутствуют файлы для проверки
                if (uploadFileNames.isEmpty()) {
                    return;
                }

                ErrorModalWindow errorModalWindow = errorModalDescriptorService.getErrorModal(ErrorModalView.DOWNLOAD_FILE_ERROR);

                // в ответе от террабайта не содержится информации о файлах
                if (storedFiles.isEmpty()) {
                    throw new TerrabyteFileCheckException(errorModalWindow, FILES_NOT_FOUND_IN_FILE_STORAGE);
                }

                List<String> storedFileNames = storedFiles.stream()
                        .map(FileInfo::getFileName)
                        .collect(Collectors.toList());

                // необходимые файлы отсутствуют в файловом хранилище
                if (!storedFileNames.containsAll(uploadFileNames)) {
                    throw new TerrabyteFileCheckException(errorModalWindow, FILES_NOT_FOUND_IN_FILE_STORAGE);
                }
            }
        }
    }
}
