package ru.gosuslugi.pgu.fs.exception;

import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
public class BookingUnavailableException extends FormBaseException {

    private static final String YANDEX_MAPS_LINK = "https://yandex.ru/maps/?text=";

    private final BookingUnavailableExceptionDto value;

    public BookingUnavailableException(String s, String text) {
        super(s);
        this.value = new BookingUnavailableExceptionDto(YANDEX_MAPS_LINK.concat(text), text);
    }

    @Override
    public Object getValue() {
        return value;
    }
}

@Value
class BookingUnavailableExceptionDto{
    String url;
    String text;
}

