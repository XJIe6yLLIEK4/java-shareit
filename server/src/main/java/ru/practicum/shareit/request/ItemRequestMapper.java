package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    public ItemRequestDto toDto(ItemRequest r, List<Item> answers) {
        return ItemRequestDto.builder()
                .id(r.getId())
                .description(r.getDescription())
                .created(r.getCreated())
                .items(answers.stream()
                        .map(itemMapper::toDto)
                        .toList())
                .build();
    }

    public ItemRequest toModel(String description, User requester) {
        return ItemRequest.builder()
                .description(description)
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }
}

