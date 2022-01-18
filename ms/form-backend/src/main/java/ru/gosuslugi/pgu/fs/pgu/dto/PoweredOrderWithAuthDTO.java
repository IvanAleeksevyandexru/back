package ru.gosuslugi.pgu.fs.pgu.dto;

import lombok.Data;
import ru.gosuslugi.pgu.core.lk.model.order.dto.OrderWithAuthDTO;

/**
 * Обновленный DTO для формирования orderId для юр.лица с привилегиями
 * В случае если при создании orderId были указаны привилегии, и они настроены на уровне услуги,
 * то средствами SP будет производится прикладывание файлов доверенности (файл и его подпись) к запросу в ведомство
 * Потенциально должны будем перейти на обновленный OrderWithAuthDTO, который предоставит РтЛабс
 */
@Data
public class PoweredOrderWithAuthDTO extends OrderWithAuthDTO {
    private String orgPowers;
}
