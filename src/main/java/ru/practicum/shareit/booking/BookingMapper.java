package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

public class BookingMapper {
    public static BookingDto toDto(Booking booking) {
        if (booking == null) return null;
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setItemId(booking.getItemId());
        dto.setBookerId(booking.getBookerId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus().name());
        return dto;
    }

    public static Booking toModel(BookingDto dto) {
        if (dto == null) return null;
        Booking b = new Booking();
        b.setId(dto.getId());
        b.setItemId(dto.getItemId());
        b.setBookerId(dto.getBookerId());
        b.setStart(dto.getStart());
        b.setEnd(dto.getEnd());
        b.setStatus(BookingStatus.valueOf(dto.getStatus()));
        return b;
    }
}
