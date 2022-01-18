package ru.gosuslugi.pgu.fs.delirium;

import ru.gosuslugi.pgu.fs.delirium.model.DeliriumResponseDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumRequestDto;

public interface DeliriumClient {

    /**
     * Запустить процесс выполнения комплексной заявки. Выполняется асинхронно.
     * @param deliriumRequestDto контекст выполнения
     * @return статус выполнения
     */
    DeliriumResponseDto postOrder(DeliriumRequestDto deliriumRequestDto);

    /**
     * Состояние комплексного заявления
     * @param orderId идентификатор заявления
     * @return состояние заявления
     */
    DeliriumStageDto getStage(Long orderId);

    /**
     * Рассчитать состояние комплексного заявления.
     * Может выполнить при этом изменение состояния (переход на следующий стейдж)
     * @param orderId идентификатор заявления
     * @return состояние заявления
     */
    DeliriumStageDto calcStage(Long orderId);

    /**
     * Рассчитать состояние комплексного заявления при выполнении условий на основе данных черновика.
     * Может выполнить при этом изменение состояния (переход на следующий стейдж)
     * @param orderId идентификатор заявления
     * @return состояние заявления
     */
    DeliriumStageDto calcStageWithDraft(Long orderId);
}
