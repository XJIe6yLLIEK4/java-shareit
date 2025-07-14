package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestMapperTest {

    @Mock
    private ItemMapper itemMapper;

    private ItemRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ItemRequestMapper(itemMapper);
    }

    @Test
    void toDto_shouldCopyAllFields_andCallItemMapper() {
        // given
        LocalDateTime now = LocalDateTime.now();
        ItemRequest request = ItemRequest.builder()
                .id(7L)
                .description("Need a drill")
                .created(now)
                .build();

        Item item = Item.builder()
                .id(10L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(10L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .build();

        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ItemRequestDto result = mapper.toDto(request, List.of(item));

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getCreated()).isEqualTo(now);
        assertThat(result.getItems()).containsExactly(itemDto);

        verify(itemMapper, times(1)).toDto(item);
        verifyNoMoreInteractions(itemMapper);
    }

    @Test
    void toModel_shouldCreateEntityWithCurrentTime() {
        // given
        String description = "Need a ladder";
        User requester = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@mail.com")
                .build();

        LocalDateTime before = LocalDateTime.now();

        ItemRequest entity = mapper.toModel(description, requester);

        LocalDateTime after = LocalDateTime.now();

        assertThat(entity.getId()).isNull();
        assertThat(entity.getDescription()).isEqualTo(description);
        assertThat(entity.getRequester()).isEqualTo(requester);
        assertThat(entity.getCreated()).isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }
}
