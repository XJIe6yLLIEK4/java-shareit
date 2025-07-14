package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    @Mock
    CommentRepository commentRepository;
    @Mock
    BookingServiceImpl bookingService;
    @Mock
    ItemRequestRepository requestRepository;

    @InjectMocks
    ItemMapper mapper;

    private User owner;
    private Item item;
    private LocalDateTime now;
    private Booking past;
    private Booking future;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        owner = User.builder().id(1L).name("Owner").email("o@mail.com").build();
        item = Item.builder()
                .id(10L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .owner(owner)
                .build();

        past = Booking.builder()
                .id(101L)
                .item(item)
                .booker(User.builder().id(2L).build())
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();

        future = Booking.builder()
                .id(102L)
                .item(item)
                .booker(User.builder().id(3L).build())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    @DisplayName("toDto() — заполняет last/next booking и комментарии")
    void toDto_ok() {
        when(bookingService.getLastBooking(eq(item.getId()), any()))
                .thenReturn(Optional.of(past));
        when(bookingService.getNextBooking(eq(item.getId()), any()))
                .thenReturn(Optional.of(future));
        when(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()))
                .thenReturn(List.of());

        ItemDto dto = mapper.toDto(item);

        assertThat(dto).extracting(ItemDto::getId,
                        ItemDto::getName,
                        ItemDto::getDescription,
                        ItemDto::getAvailable)
                .containsExactly(10L, "Drill", "Cordless", true);

        assertThat(dto.getLastBooking()).isEqualTo(past.getEnd());
        assertThat(dto.getNextBooking()).isEqualTo(future.getStart());
        assertThat(dto.getComments()).isEmpty();
    }

    @Test
    @DisplayName("toSimpleDto() — last/next booking всегда null")
    void toSimpleDto_ok() {
        when(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()))
                .thenReturn(List.of());

        ItemDto dto = mapper.toSimpleDto(item);

        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
        verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("toModel() — маппит request, когда requestId указан")
    void toModel_withRequest() {
        ItemRequest req = ItemRequest.builder().id(99L).build();
        when(requestRepository.getReferenceById(99L)).thenReturn(req);

        ItemDto dto = ItemDto.builder()
                .id(20L)
                .name("Saw")
                .description("Hand saw")
                .available(false)
                .requestId(99L)
                .build();

        Item result = mapper.toModel(dto, owner);

        assertThat(result.getName()).isEqualTo("Saw");
        assertThat(result.getOwner()).isSameAs(owner);
        assertThat(result.getRequest()).isSameAs(req);

        verify(requestRepository).getReferenceById(99L);
    }

    @Test
    @DisplayName("toModel() — без requestId репозиторий не дергается")
    void toModel_withoutRequest() {
        ItemDto dto = ItemDto.builder()
                .id(21L)
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        Item result = mapper.toModel(dto, owner);

        assertThat(result.getRequest()).isNull();
        verifyNoInteractions(requestRepository);
    }
}
