package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService;

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.SearchableDropDown;

/**
 * Компонент "Список с возможностью выбора".
 * Используется для разделения логики и будущих улучшений - чтобы не нужно было делать рефакторинг и использовать вместо DropDown
 */
@Component
public class SearchableDropDownComponent extends DropDownComponent {

    public SearchableDropDownComponent(UserPersonalData userPersonalData, DictionaryListPreprocessorService dictionaryListPreprocessorService) {
        super(userPersonalData, dictionaryListPreprocessorService);
    }

    @Override
    public ComponentType getType() {
        return SearchableDropDown;
    }
}
