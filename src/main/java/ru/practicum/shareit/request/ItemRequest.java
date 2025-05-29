package ru.practicum.shareit.request;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Data
@Component
public class ItemRequest {
    private Long id;
    private String description;
    private Long requesterId;
    private LocalDateTime created;
}
