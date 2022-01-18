package ru.gosuslugi.pgu.fs.descriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Модальные окна, определенные в json */
@Getter
@RequiredArgsConstructor
public enum ErrorModalView {

    /* error */

    /** Модальное окно с ошибкой по-умолчанию */
    DEFAULT_ERROR(MappingType.error),

    /** Не удалось загрузить файл */
    DOWNLOAD_FILE_ERROR(MappingType.error),

    /* warning */

    /** Модальное окно с предупреждением по-умолчанию */
    DEFAULT_WARNING(MappingType.warning),

    /** Невозможно создать заявление */
    COUNT(MappingType.warning),

    /** Нет прав на отправку заявления */
    NO_RIGHTS_FOR_SENDING_APPLICATION(MappingType.warning),

    /** Черновик заявления не найден */
    DRAFT_NOT_FOUND(MappingType.warning),

    /** Вы уже проверили данные и отправили заявление */
    GEPS(MappingType.warning),

    /** Ошибка загрузки */
    LOADING_ERROR(MappingType.warning),

    /** Код подтверждения не введен */
    TOO_MANY_REQUESTS_PHONE(MappingType.warning),

    /** Код подтверждения не введен */
    TOO_MANY_REQUESTS_EMAIL(MappingType.warning),

    /** Запрашиваемого черновика не существует */
    REQUESTED_ORDER_DOES_NOT_EXIST(MappingType.warning),

    /** Нашлось несколько учётных записей */
    SNILS(MappingType.warning),

    /** Нашлось несколько учётных записей */
    PASSPORT(MappingType.warning),

    /* stop */

    /** Пока нельзя отправить */
    REPEATABLE_INVITATION(MappingType.stop),
    ;

    /** перечень имен файлов json с модальными окнами */
    public enum MappingType {error, warning, stop}

    /** Имя файла для поиска json с модальным окном */
    private final MappingType mappingType;

    private static final Map<String, ErrorModalView> modalViews =
            Stream.of(values()).collect(Collectors.toMap(Enum::name, Function.identity()));

    public static ErrorModalView fromStringOrDefault(String view, ErrorModalView defaultView) {
        return modalViews.getOrDefault(view, defaultView);
    }
}
