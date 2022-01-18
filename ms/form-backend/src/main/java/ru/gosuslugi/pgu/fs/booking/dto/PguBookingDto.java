package ru.gosuslugi.pgu.fs.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.gosuslugi.pgu.dto.descriptor.BookingInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PguBookingDto {

    private Map<String, List<PguBookingObjectDto>> orderAttributeMap = new HashMap<>();

    public static PguBookingDto createFrom(BookingInfo bookingInfo) {
        var result = new PguBookingDto();
        List<PguBookingObjectDto> orderAttributes = new ArrayList<>();
        var bookingStatusDto = new PguBookingObjectDto();

        bookingStatusDto.setName("booking-status");
        bookingStatusDto.setTitle("статус");
        bookingStatusDto.setNewValue(String.join(",", bookingInfo.getAvailableStatusList()));
        orderAttributes.add(bookingStatusDto);

        var bookingDto = new PguBookingObjectDto();
        bookingDto.setName("booking");
        bookingDto.setNewValue(bookingInfo.getBookingLink());
        bookingDto.setTitle("ссылка на запись на прием");
        orderAttributes.add(bookingDto);

        result.getOrderAttributeMap().put("OrderAttribute", orderAttributes);
        return result;
    }
}
