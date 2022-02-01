package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponse;
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponseAttribute;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalInfoDto;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;
import ru.gosuslugi.pgu.fs.service.MedicineService;
import ru.gosuslugi.pgu.fs.service.RegionInfoService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.Lookup;

@Slf4j
@Component
public class LookupComponent extends SelectedAbstractList {

    private final RegionInfoService regionInfoService;
    private final MedicineService medicineService;

    public LookupComponent(DictionaryFilterService dictionaryFilterService, RegionInfoService regionInfoService, MedicineService medicineService) {
        super(dictionaryFilterService);
        this.regionInfoService = regionInfoService;
        this.medicineService = medicineService;
    }

    @Override
    public ComponentType getType() {
        return Lookup;
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        super.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent);

        if ("medicalInfo".equals(fieldComponent.getAttrs().get("externalIntegration"))) {

            Map<String, Object> answerMap = jsonProcessingService.fromJson(entry.getValue().getValue(), LinkedHashMap.class);
            Map<String, Object> originalItem = (Map<String, Object>) answerMap.get(ORIGINAL_ITEM);
            Map<String, Object> attributeValues = (Map<String, Object>) originalItem.get("attributeValues");
            String regionCode = attributeValues.getOrDefault("OKATO", "").toString();
            String serviceCode = fieldComponent.getAttrs().getOrDefault("serviceCode", "").toString();
            Integer smevVersion = Integer.parseInt(fieldComponent.getAttrs().getOrDefault("smevVersion", "0").toString());
            smevVersion = (smevVersion == 0) ? regionInfoService.getIntegerSmevVersion(regionCode, serviceCode) : smevVersion;


            var sessionId = UUID.randomUUID().toString();
            var eserviceId = fieldComponent.getAttrs().getOrDefault(smevVersion == 2 ? "ESERVICEID_V2" : "ESERVICEID_V3", "").toString();

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
