package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingRequestDto {
    private Long id;
    @NotBlank
    private Long itemId;
    private String itemName;
    private Long bookerId;
    @NotBlank
    private LocalDateTime start;
    @NotBlank
    private LocalDateTime end;
    private String status;
}
