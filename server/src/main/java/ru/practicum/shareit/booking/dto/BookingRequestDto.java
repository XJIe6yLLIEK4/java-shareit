package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
}
