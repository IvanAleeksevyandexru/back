package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.components.descriptor.types.Stage;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;

/**
 * Сервис инкапулирующий всю работу с delirium
 */
public interface DeliriumService {
    /** Стадия заявки по умолчанию при которой у пользователя должна быть возможность начать заполнение услуги заново */
    String DEFAULT_STAGE = Stage.Applicant.name();

    /** Роль пользователя по умолчанию */
    ApplicantRole DEFAULT_USER_ROLE = ApplicantRole.Applicant;


    void notifyScenarioEnd(ScenarioDto scenarioDto);

    void requestAction(ScenarioDto scenarioDto, String deliriumAction);

    /**
     * Возвращает статус комплексной заявки.
     * Если Делириум не участвует в процессе, то возвращается статус по умолчнию {@link #DEFAULT_STAGE}
     * @param orderId номер заявки
     * @return статус комплексной заявки
     */
    DeliriumStageDto getStage(long orderId);

    /**
     * Рассчитывает статус комплексной заявки.
     * Если Делириум не участвует в процессе, то возвращается статус по умолчнию {@link #DEFAULT_STAGE}.
     * Во время расчета может измениться статус заявки
     * @param orderId номер заявки
     * @return статус комплексной заявки
     */
    DeliriumStageDto calcStage(long orderId);

    /**
     * Рассчитывает статус комплексной заявки при выполнении условий на основе данных черновика.
     * @param orderId номер заявки
     * @return статус комплексной заявки
     */
    DeliriumStageDto calcStageWithDraft(long orderId);

    /**
     * Возвращает текущего роль пользователя на основе данных об участниках услуги из черновика
     * @param draftHolderDto черновик из БД
     * @return роль пользователя
     */
    ApplicantRole getUserRole(DraftHolderDto draftHolderDto);

    /**
     * Возвращает стадию заявки в зависимости от передаваемых параметров.
     * Для созаявителей выполняется расчет стадии.
     * @param role роль текущего пользователя
     * @param orderId идентификатор заявки
     * @param useDefaultStage если {@code true}, то будет использоваться стадия по умолчанию, без обращения к Делириуму
     * @return стадия заявки
     * @see #calcStage(long)
     */
    DeliriumStageDto getStage(ApplicantRole role, Long orderId, boolean useDefaultStage);
}
