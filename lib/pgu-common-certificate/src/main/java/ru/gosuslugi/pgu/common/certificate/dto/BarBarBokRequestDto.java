package ru.gosuslugi.pgu.common.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.beans.ConstructorProperties;

/**
 * Класс запроса в сервис barbarbok
 * в поле data строка, содержащая сформированный xml запроса о сертификате дополнительного образования
 * @see <a href="https://confluence.egovdev.ru/pages/viewpage.action?pageId=193302173">
 *     Необходимые поля для запроса в сервис барбарбок</a>
 */

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor(onConstructor_ = {@ConstructorProperties({"data", "ttl", "timeout", "eol", "smevVersion"})})
public class BarBarBokRequestDto {
    /** Текст запроса. */
    String data;
    /** Время жизни запроса в кэше. */
    Long ttl;
    /** Время ожидания ответа в секундах. Только для /get метода барбарбок. */
    Long timeout;
    /** Время жизни запроса в СМЭВ в секундах. */
    Long eol;
    /** Версия СМЭВ3: SMEV30MESSAGE - версия схемы 1.2, SMEV313MESSAGE - версия схемы 1.3. */
    String smevVersion;
}
