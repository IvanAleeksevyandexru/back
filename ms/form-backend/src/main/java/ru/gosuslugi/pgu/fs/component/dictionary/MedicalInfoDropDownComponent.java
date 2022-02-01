package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponse;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalItemsFilter;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalItemsRequest;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalItemsUnion;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalSimpleFilter;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalSimpleItem;
import ru.gosuslugi.pgu.fs.service.MedicineService;
import ru.gosuslugi.pgu.fs.utils.MedicineServiceUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;

import static ru.gosuslugi.pgu.components.ComponentAttributes.MEDICAL_POS_ARGS;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.MedicalInfoDropDown;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalInfoDropDownComponent extends AbstractComponent<String> {
    /** Коллектор для получения набора всех атрибутов из стрима фильтров. */
    private static final Collector<MedicalSimpleFilter, Set<String>, Set<String>> SIMPLE_FILTER_COLLECTOR =
            Collector.of(
                    HashSet::new,
                    (Set<String> set, MedicalSimpleFilter element) -> set.add(element.getSimple().getAttributeName()),
                    (r1, r2) -> {
                        r1.addAll(r2);
                        return r1;
                    }
            );

    private final MedicineService medicineService;

    @Override
    public ComponentType getType() {
        return MedicalInfoDropDown;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component) {
        var hasReferral = Boolean.valueOf(component.getArgument("hasReferral"));
        MedicalItemsRequest request;
        if(hasReferral){
            request = MedicineServiceUtils.createRequestByReferral(component);
        }else{
            request = MedicineServiceUtils.createRequestNonReferral(component);
        }
        MedDictionaryResponse medDictionaryResponse = medicineService.getSpecialists(request);
        return ComponentResponse.of(JsonProcessingUtil.toJson(medDictionaryResponse));
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new RequiredNotBlankValidation("Значение не задано")
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String entryValue = AnswerUtil.getValue(entry);
        if (StringUtils.isEmpty(entryValue)) {
            return;
        }
        var hasReferral = Boolean.valueOf(fieldComponent.getArgument("hasReferral"));
        MedicalItemsRequest request;
        if(hasReferral){
            request = MedicineServiceUtils.createRequestByReferral(fieldComponent);
        }else{
            request = MedicineServiceUtils.createRequestNonReferral(fieldComponent);
        }
        Optional<List<MedicalSimpleFilter>> filtersOptional =
                Optional.ofNullable(request.getFilter())
                        .map(MedicalItemsFilter::getUnion)
                        .map(MedicalItemsUnion::getSubs);
        if(filtersOptional.isEmpty()) {
            throw new FormBaseWorkflowException(String.format("Фильтры не заданы, ключ %s", entry.getKey()));
        }

        Set<String> attrNameSet = filtersOptional.get().stream()
                .filter(
                        simpleFilter ->
                                Optional.ofNullable(simpleFilter.getSimple())
                                        .map(MedicalSimpleItem::getAttributeName)
                                        .filter(MEDICAL_POS_ARGS::contains).isPresent()
                )
                .collect(SIMPLE_FILTER_COLLECTOR);
        if (attrNameSet.size() < MEDICAL_POS_ARGS.size()) {
            Set<String> absentAttrSet = new HashSet<>(MEDICAL_POS_ARGS);
            absentAttrSet.removeAll(attrNameSet);
            throw new FormBaseWorkflowException(String.format("Отсутствуют атрибуты %s в фильтрах запроса, ключ %s", absentAttrSet, entry.getKey()));
        }

        MedDictionaryResponse medDictionaryResponse = medicineService.getSpecialists(request);
        if (CollectionUtils.isEmpty(medDictionaryResponse.getItems())) {
            String json = jsonProcessingService.toJson(request);
            throw new FormBaseWorkflowException(String.format("По запросу %s не найдено ничего, ключ %s", json, entry.getKey()));
        }
    }
}
