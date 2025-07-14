package ru.practicum.shareit.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock  private ItemRepository itemRepository;
    @Mock  private UserRepository userRepository;
    @Mock  private BookingServiceImpl bookingService;
    @Mock  private CommentRepository commentRepository;
    @Mock  private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item   item;
    private ItemDto dtoIn;
    private ItemDto dtoOut;

    @BeforeEach
    void setUp() {
        owner  = User.builder().id(1L).name("owner").email("o@mail.com").build();
        item   = Item.builder().id(10L).name("drill").description("perf").available(true).owner(owner).build();
        dtoIn  = ItemDto.builder().name("drill").description("perf").available(true).build();
        dtoOut = ItemDto.builder().id(10L).name("drill").description("perf").available(true).build();
    }

    @Test
    void create_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toModel(dtoIn, owner)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(dtoOut);

        ItemDto result = itemService.create(1L, dtoIn);

        assertThat(result).isEqualTo(dtoOut);
        verify(itemRepository).save(item);
    }

    @Test
    void update_owner_ok() {
        ItemDto patch = ItemDto.builder().name("new").build();
        Item saved = item.toBuilder().name("new").build();
        ItemDto patchedDto = dtoOut.toBuilder().name("new").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(saved);
        when(itemMapper.toDto(saved)).thenReturn(patchedDto);

        ItemDto result = itemService.update(1L, 10L, patch);

        assertThat(result.getName()).isEqualTo("new");
    }

    @Test
    void update_notOwner_throws() {
        User stranger = User.builder().id(99L).build();
        when(userRepository.findById(99L)).thenReturn(Optional.of(stranger));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.update(99L, 10L, dtoIn))
                .isInstanceOf(SecurityException.class);
    }

    @Nested
    class GetById {

        @Test
        void owner_getsFullDto() {
            when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
            when(itemMapper.toDto(item)).thenReturn(dtoOut);

            ItemDto result = itemService.getById(1L, 10L);

            assertThat(result).isEqualTo(dtoOut);
            verify(itemMapper).toDto(item);
            verify(itemMapper, never()).toSimpleDto(any());
        }

        @Test
        void otherUser_getsSimpleDto() {
            when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
            when(itemMapper.toSimpleDto(item)).thenReturn(dtoOut);

            ItemDto result = itemService.getById(99L, 10L);

            assertThat(result).isEqualTo(dtoOut);
            verify(itemMapper).toSimpleDto(item);
        }
    }

    @Test
    void ownerItems_returnsList() {
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(item));
        when(itemMapper.toSimpleDto(item)).thenReturn(dtoOut);

        List<ItemDto> list = itemService.getAllByOwner(1L);

        assertThat(list).containsExactly(dtoOut);
    }

    @Test
    void search_emptyText_returnsEmpty() {
        List<ItemDto> list = itemService.search("");

        assertThat(list).isEmpty();
        verify(itemRepository, never()).search(any());
    }

    @Test
    void addComment_ok() {
        long userId = 1L, itemId = 10L;

        when(bookingService.contains(eq(userId), eq(itemId),
                any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(true);

        lenient().when(userRepository.findById(userId))
                .thenReturn(Optional.of(owner));

        Comment comment = Comment.builder()
                .id(5L).text("cool").author(owner).item(item)
                .created(LocalDateTime.now())
                .build();
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto expected = CommentDto.builder()
                .id(5L).text("cool").authorName("owner").build();

        try (MockedStatic<CommentMapper> mocked =
                     Mockito.mockStatic(CommentMapper.class)) {

            mocked.when(() -> CommentMapper.toDto(comment))
                    .thenReturn(expected);

            CommentDto result = itemService.addComment(
                    userId, itemId,
                    CommentDto.builder().text("cool").build());

            assertThat(result).isEqualTo(expected);
        }
    }

    @Test
    void addComment_withoutBooking_throws() {
        when(bookingService.contains(anyLong(), anyLong(), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(false);

        assertThatThrownBy(() ->
                itemService.addComment(1L, 10L, CommentDto.builder().text("x").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void search_query_returnsDtoList() {
        when(itemRepository.search("dr")).thenReturn(List.of(item));
        when(itemMapper.toSimpleDto(item)).thenReturn(dtoOut);

        var list = itemService.search("dr");

        assertThat(list).containsExactly(dtoOut);
        verify(itemRepository).search("dr");
    }

    @Test
    void create_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(99L, dtoIn))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void commentMapper_roundtrip() {
        User u = new User(1L, "Bob", "b@mail.com");
        Item i = Item.builder().id(2L).name("Drill").build();

        Comment model = Comment.builder()
                .id(3L).text("cool").author(u).item(i)
                .created(LocalDateTime.now())
                .build();

        CommentDto dto = CommentMapper.toDto(model);
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getAuthorName()).isEqualTo("Bob");
    }

}
