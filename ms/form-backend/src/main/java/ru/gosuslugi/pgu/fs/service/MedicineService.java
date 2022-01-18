package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.EpguRegionResponse;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalItemsRequest;
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponse;


/** Сервис для работы с медицинскими компонентами похожими на {@link ComponentType#DropDown} */
public interface MedicineService {

    MedDictionaryResponse getSmevResponse(String sessionId, Integer smevVersion, FieldComponent component);

    /**
     * Получение списка медицинских должностей по запросу с заполненными атрибутами.
     *
     * @param requestBody тело запроса к справочнику СМЭВ
     * @return найденный элемент справочника со всеми атрибутами
     */
    MedDictionaryResponse getSpecialists(MedicalItemsRequest requestBody);
}
