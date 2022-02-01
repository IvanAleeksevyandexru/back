package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;

@Slf4j
@Component
public class SelectFromListComponent extends SelectedAbstractList {

    public SelectFromListComponent(DictionaryFilterService dictionaryFilterService) {
        super(dictionaryFilterService);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.SelectFromList;
    }
}
