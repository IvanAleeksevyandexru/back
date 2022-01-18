package ru.gosuslugi.pgu.fs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;

/**
 * @deprecated не используется фронтом
 */
@Slf4j
@RequestMapping(value = "/drafts")
@RestController
@RequiredArgsConstructor
@Deprecated
public class DraftController {

    private final DraftClient draftClient;
    private final UserPersonalData personalData;

    /**
     * Метод обработки входящих запросов черновиков
     */
    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET, produces = "application/json")
    public DraftHolderDto getDraft(@PathVariable Long orderId) {
        DraftHolderDto draft = draftClient.getDraftById(orderId, personalData.getUserId(), personalData.getOrgId());
        if (draft == null) {
            throw new EntityNotFoundException("Draft not found");
        }
        return draft;
    }

}
