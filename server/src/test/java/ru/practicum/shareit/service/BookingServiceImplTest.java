package ru.practicum.shareit.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking waitingBooking;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").email("o@mail.com").build();
        booker = User.builder().id(2L).name("Booker").email("b@mail.com").build();

        item = Item.builder()
                .id(10L)
                .name("Дрель")
                .description("Ударная")
                .available(true)
                .owner(owner)
                .build();

        waitingBooking = Booking.builder()
                .id(100L)
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void create_shouldSaveBooking_whenAllOk() {
        // given
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(waitingBooking.getStart())
                .end(waitingBooking.getEnd())
                .build();
        given(userRepository.findById(booker.getId())).willReturn(Optional.of(booker));
        given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
        given(bookingRepository.save(any(Booking.class))).willAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(waitingBooking.getId());
            return b;
        });

        var resp = bookingService.create(booker.getId(), dto);

        then(bookingRepository).should().save(argThat(b ->
                b.getItem().equals(item) &&
                        b.getBooker().equals(booker) &&
                        b.getStatus() == BookingStatus.WAITING));
        assertThat(resp.getId()).isEqualTo(waitingBooking.getId());
        assertThat(resp.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void create_shouldThrow_whenItemNotAvailable() {
        item.setAvailable(false);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(waitingBooking.getStart())
                .end(waitingBooking.getEnd())
                .build();
        given(userRepository.findById(booker.getId())).willReturn(Optional.of(booker));
        given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Товар недоступен");
        then(bookingRepository).shouldHaveNoInteractions();
    }

    @Nested
    class Approve {
        @Test
        void approve_shouldSetApproved_whenOwnerApproves() {
            given(bookingRepository.findById(waitingBooking.getId()))
                    .willReturn(Optional.of(waitingBooking));
            given(bookingRepository.save(waitingBooking)).willReturn(waitingBooking);

            try (MockedStatic<BookingMapper> mapperMock = mockStatic(BookingMapper.class)) {
                BookingResponseDto approvedDto = BookingResponseDto.builder()
                        .id(waitingBooking.getId())
                        .item(item)
                        .booker(booker)
                        .start(waitingBooking.getStart())
                        .end(waitingBooking.getEnd())
                        .status(BookingStatus.APPROVED)
                        .build();

                mapperMock.when(() -> BookingMapper.toDto(waitingBooking))
                        .thenReturn(approvedDto);

                BookingResponseDto dto = bookingService.approve(
                        owner.getId(), waitingBooking.getId(), true);

                then(bookingRepository).should().save(waitingBooking);
                assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
            }
        }

        @Test
        void approve_shouldThrow_whenUserNotOwner() {
            given(bookingRepository.findById(waitingBooking.getId()))
                    .willReturn(Optional.of(waitingBooking));

            assertThatThrownBy(() ->
                    bookingService.approve(booker.getId(), waitingBooking.getId(), true))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("owner");

            then(bookingRepository).should(never()).save(any());
        }
    }

    @Test
    void getById_shouldReturnBooking_forOwner() {
        given(bookingRepository.findById(waitingBooking.getId()))
                .willReturn(Optional.of(waitingBooking));

        var dto = bookingService.getById(owner.getId(), waitingBooking.getId());

        assertThat(dto.getId()).isEqualTo(waitingBooking.getId());
    }

    @Test
    void getById_shouldThrow_forStranger() {
        given(bookingRepository.findById(waitingBooking.getId()))
                .willReturn(Optional.of(waitingBooking));

        assertThatThrownBy(() ->
                bookingService.getById(99L, waitingBooking.getId()))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void getAllByBooker_allState_returnsSortedList() {
        given(bookingRepository.findByBookerIdOrderByStartDesc(booker.getId()))
                .willReturn(List.of(waitingBooking));

        var list = bookingService.getAllByBooker(booker.getId(), "ALL");

        assertThat(list).hasSize(1)
                .first()
                .extracting("id")
                .isEqualTo(waitingBooking.getId());
    }

    @Test
    void getAllByBooker_unknownState_throws() {
        assertThatThrownBy(() -> bookingService.getAllByBooker(booker.getId(), "SMTH"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getAllByOwner_waitingState_ok() {
        given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));
        given(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.WAITING))
                .willReturn(List.of(waitingBooking));

        var list = bookingService.getAllByOwner(owner.getId(), "waiting");

        assertThat(list).hasSize(1);
    }

    @Test
    void contains_returnsTrue_whenBookingExists() {
        given(bookingRepository.existsByBooker_IdAndItemIdAndEndBeforeAndStatus(
                eq(booker.getId()), eq(item.getId()), any(), eq(BookingStatus.APPROVED)))
                .willReturn(true);

        boolean actual = bookingService.contains(
                booker.getId(), item.getId(), LocalDateTime.now(), BookingStatus.APPROVED);

        assertThat(actual).isTrue();
    }

    @Test
    void getLastAndNextBooking_delegatesToRepository() {
        LocalDateTime now = LocalDateTime.now();
        given(bookingRepository.findTopByItemIdAndEndBeforeAndStatusOrderByStartDesc(
                item.getId(), now, BookingStatus.APPROVED))
                .willReturn(Optional.of(waitingBooking));
        given(bookingRepository.findTopByItemIdAndStartAfterAndStatusOrderByStartAsc(
                item.getId(), now, BookingStatus.APPROVED))
                .willReturn(Optional.empty());

        assertThat(bookingService.getLastBooking(item.getId(), now)).isPresent();
        assertThat(bookingService.getNextBooking(item.getId(), now)).isEmpty();
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(99L, new BookingRequestDto()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Nested
    class FilterByState {

        @BeforeEach
        void mockOwner() {
            lenient()
                    .when(userRepository.findById(owner.getId()))
                    .thenReturn(Optional.of(owner));
        }

        @Test
        void booker_current_invokesProperRepo() {
            given(bookingRepository
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(booker.getId()), any(), any()))
                    .willReturn(List.of(waitingBooking));

            bookingService.getAllByBooker(booker.getId(), "current");

            then(bookingRepository).should()
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(booker.getId()), any(), any());
        }

        @Test
        void booker_past_invokesProperRepo() {
            given(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(eq(booker.getId()), any()))
                    .willReturn(List.of(waitingBooking));

            bookingService.getAllByBooker(booker.getId(), "past");

            then(bookingRepository).should()
                    .findByBookerIdAndEndBeforeOrderByStartDesc(eq(booker.getId()), any());
        }

        @Test
        void booker_future_invokesProperRepo() {
            given(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(eq(booker.getId()), any()))
                    .willReturn(List.of(waitingBooking));

            bookingService.getAllByBooker(booker.getId(), "future");

            then(bookingRepository).should()
                    .findByBookerIdAndStartAfterOrderByStartDesc(eq(booker.getId()), any());
        }

        @Test
        void booker_waiting_invokesProperRepo() {
            given(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(booker.getId(), BookingStatus.WAITING))
                    .willReturn(List.of(waitingBooking));

            bookingService.getAllByBooker(booker.getId(), "waiting");

            then(bookingRepository).should()
                    .findByBookerIdAndStatusOrderByStartDesc(booker.getId(), BookingStatus.WAITING);
        }

        @Test
        void booker_rejected_invokesProperRepo() {
            given(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(booker.getId(), BookingStatus.REJECTED))
                    .willReturn(List.of());

            bookingService.getAllByBooker(booker.getId(), "rejected");

            then(bookingRepository).should()
                    .findByBookerIdAndStatusOrderByStartDesc(booker.getId(), BookingStatus.REJECTED);
        }

        @Test
        void owner_current_invokesProperRepo() {
            given(bookingRepository
                    .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(owner.getId()), any(), any()))
                    .willReturn(List.of(waitingBooking));

            bookingService.getAllByOwner(owner.getId(), "current");

            then(bookingRepository).should()
                    .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(owner.getId()), any(), any());
        }

        @Test
        void owner_past_invokesProperRepo() {
            given(bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()), any()))
                    .willReturn(List.of(waitingBooking));

            bookingService.getAllByOwner(owner.getId(), "past");

            then(bookingRepository).should()
                    .findByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()), any());
        }

        @Test
        void owner_future_invokesProperRepo() {
            given(bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()), any()))
                    .willReturn(List.of(waitingBooking));

            bookingService.getAllByOwner(owner.getId(), "future");

            then(bookingRepository).should()
                    .findByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()), any());
        }

        @Test
        void owner_waiting_invokesProperRepo() {
            given(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.WAITING))
                    .willReturn(List.of(waitingBooking));

            bookingService.getAllByOwner(owner.getId(), "waiting");

            then(bookingRepository).should()
                    .findByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.WAITING);
        }

        @Test
        void owner_rejected_invokesProperRepo() {
            given(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.REJECTED))
                    .willReturn(List.of());

            bookingService.getAllByOwner(owner.getId(), "rejected");

            then(bookingRepository).should()
                    .findByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.REJECTED);
        }
    }
}
