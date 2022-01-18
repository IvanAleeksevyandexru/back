package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.exception.MultiplePersonFoundException;
import ru.gosuslugi.pgu.common.esia.search.dto.PersonWithAge;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.component.FormDto;
import ru.gosuslugi.pgu.fs.component.confirm.internal.PassportDoc;
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalUserData;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;

import java.util.List;
import java.util.Map;

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.ConfirmAnotherUserData;

/**
 * Компонент показывает персональные данные пользователя из ЕСИА
 */
@Component
@RequiredArgsConstructor
public class ConfirmAnotherUserDataComponent extends AbstractCycledComponent<FormDto<ConfirmPersonalUserData>> {

    private final PersonSearchService personSearch;
    private final ErrorModalDescriptorService errorModalDescriptorService;

    @Override
    public ComponentType getType() {
        return ConfirmAnotherUserData;
    }

    /**
     * Определяет данные для начального заполнения компонента ввода пользователя
     * @param component компонент ввода пользователя
     * @param externalData данные для определения значения по умоляанию
     * @return данные для заполнения
     */
    @Override
    public ComponentResponse<FormDto<ConfirmPersonalUserData>> getCycledInitialValue(FieldComponent component, Map<String, Object> externalData) {
        PassportDoc passport = new PassportDoc(externalData);
        PersonWithAge person;
        try {
            person = personSearch.searchOneTrusted(passport.getSeries(), passport.getNumber());
        } catch (MultiplePersonFoundException e) {
            throw new ErrorModalException(errorModalDescriptorService.getErrorModal(ErrorModalView.PASSPORT), e);
        }
        if (person == null)
            return ComponentResponse.empty();

        UserPersonalData anotherUserData = new UserPersonalData();
        anotherUserData.setPerson(person);
        anotherUserData.setDocs(List.of(passport));

        return ComponentResponse.of(
                ConfirmPersonalUserDataComponent.prepareForm(anotherUserData, BasicComponentUtil.getPreSetFields(component))
        );
    }
}
