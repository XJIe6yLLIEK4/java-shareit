package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock  private ItemRequestRepository requestRepo;
    @Mock  private ItemRepository itemRepo;
    @Mock  private UserRepository userRepo;
    @Mock  private ItemRequestMapper mapper;

    @InjectMocks
    private ItemRequestServiceImpl service;

    private User user;
    private ItemRequest request;
    private ItemRequestDto dto;
    private Item item;

    @BeforeEach
    void init() {
        user = User.builder().id(1L).name("u").email("e@mail.com").build();
        request = ItemRequest.builder().id(3L).description("need").requester(user).created(LocalDateTime.now()).build();
        dto = ItemRequestDto.builder().id(3L).description("need").created(request.getCreated()).build();
        item = Item.builder().id(10L).name("drill").description("perf").request(request).available(true).build();
    }

    @Test
    void create_ok() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toModel("need", user)).thenReturn(request);
        when(requestRepo.save(request)).thenReturn(request);
        when(mapper.toDto(request, List.of())).thenReturn(dto);

        ItemRequestDto result = service.create(1L, ItemRequestDto.builder().description("need").build());

        assertThat(result).isEqualTo(dto);
        verify(requestRepo).save(request);
    }

    @Test
    void getByUser_ok() {
        when(requestRepo.findAllByRequesterIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));
        when(itemRepo.findByRequestIdIn(List.of(3L))).thenReturn(List.of(item));
        when(mapper.toDto(eq(request), anyList())).thenReturn(dto);

        List<ItemRequestDto> list = service.getByUser(1L);

        assertThat(list).containsExactly(dto);
    }

    @Test
    void getAll_ok() {
        when(requestRepo.findOtherUsersRequests(eq(1L), any()))
                .thenReturn(List.of(request));
        when(itemRepo.findByRequestIdIn(List.of(3L))).thenReturn(List.of(item));
        when(mapper.toDto(eq(request), anyList())).thenReturn(dto);

        List<ItemRequestDto> list = service.getAll(1L);

        assertThat(list).hasSize(1);
    }

    @Test
    void getById_ok() {
        when(requestRepo.findById(3L)).thenReturn(Optional.of(request));
        when(itemRepo.findByRequestId(3L)).thenReturn(List.of(item));
        when(mapper.toDto(request, List.of(item))).thenReturn(dto);

        ItemRequestDto result = service.getById(1L, 3L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getById_notFound() {
        when(requestRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L, 99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void create_userNotFound_throws() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.create(99L, ItemRequestDto.builder().description("need").build()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getAll_noRequests_returnsEmpty() {
        when(requestRepo.findOtherUsersRequests(eq(1L), any()))
                .thenReturn(Collections.emptyList());
        when(itemRepo.findByRequestIdIn(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        var list = service.getAll(1L);

        assertThat(list).isEmpty();
        verify(itemRepo).findByRequestIdIn(Collections.emptyList());
    }
}
