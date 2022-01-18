package ru.gosuslugi.pgu.fs.component.gender;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponse;
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponseAttribute;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalInfoDto;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;
import ru.gosuslugi.pgu.fs.service.MedicineService;
import ru.gosuslugi.pgu.fs.service.RegionInfoService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.GLookup;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.Lookup;

@Component
@RequiredArgsConstructor
public class LookupGenderComponent extends AbstractDictionaryGenderComponent<String> {

    private final DictionaryFilterService dictionaryFilterService;
    private final RegionInfoService regionInfoService;
    private final MedicineService medicineService;

    @Override
    public ComponentType getType() {
        return GLookup;
    }

    @Override
    protected ComponentType getTargetComponentType() {
        return Lookup;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> presetValues = new HashMap<>(dictionaryFilterService.getInitialValue(component, scenarioDto));
        return presetValues.isEmpty() ? ComponentResponse.empty() : ComponentResponse.of(jsonProcessingService.toJson(presetValues));
    }

    @Override
    public void preloadComponent(FieldComponent component, ScenarioDto scenarioDto) {
        dictionaryFilterService.preloadComponent(component, scenarioDto, () -> getInitialValue(component, scenarioDto));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if (StringUtils.hasText(AnswerUtil.getValue(entry))) {
            dictionaryFilterService.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent, () -> getInitialValue(fieldComponent, scenarioDto));
        }

        if ("medicalInfo".equals(fieldComponent.getAttrs().get("externalIntegration"))){
            Map<String, Object> answerMap = jsonProcessingService.fromJson(entry.getValue().getValue(), LinkedHashMap.class);
            Map<String, Object> originalItem = (Map<String, Object>) answerMap.get(ORIGINAL_ITEM);
            Map<String, Object> attributeValues = (Map<String, Object>) originalItem.get("attributeValues");
            String regionCode = attributeValues.getOrDefault("OKATO","").toString();
            String serviceCode = fieldComponent.getAttrs().getOrDefault("serviceCode","").toString();
            Integer smevVersion = regionInfoService.getIntegerSmevVersion(regionCode, serviceCode);

            var sessionId = UUID.randomUUID().toString();
            var eserviceId = fieldComponent.getAttrs().getOrDefault(smevVersion == 2 ? "ESERVICEID_V2" : "ESERVICEID_V3","").toString();

            MedDictionaryResponse medicalData = medicineService.getSmevResponse(sessionId, smevVersion, fieldComponent);
            medicalData.getItems().forEach(item -> {
                item.setConvertedAttributes(item
                        .getAttributes()
                        .stream()
                        .collect(Collectors.toMap(MedDictionaryResponseAttribute::getName, MedDictionaryResponseAttribute::getValue))
                );
            });
            MedicalInfoDto valueDto = MedicalInfoDto.builder()
                    .smevVersion(smevVersion)
                    .sessionId(sessionId)
                    .medicalData(medicalData)
                    .eserviceId(eserviceId)
                    .build();
            valueDto.fillBookAttributes();
            answerMap.put("medicalInfo", valueDto);
            entry.getValue().setValue(jsonProcessingService.toJson(answerMap));
        }
    }
}

