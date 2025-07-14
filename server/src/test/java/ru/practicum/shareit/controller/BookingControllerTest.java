package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String BASE_PATH = "/bookings";
    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean BookingService service;

    private final BookingResponseDto sample = BookingResponseDto.builder()
            .id(55L)
            .item(Item.builder().id(99L).name("Drill").build())
            .booker(User.builder().id(1L).name("John").build())
            .status(BookingStatus.WAITING)
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(2))
            .build();

    @Test
    @DisplayName("POST /bookings ‑ create booking")
    void create() throws Exception {
        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(99L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        when(service.create(eq(1L), any())).thenReturn(sample);

        mvc.perform(post(BASE_PATH)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(55)));
    }

    @Test
    @DisplayName("PATCH /bookings/{id}?approved=true ‑ owner approves booking")
    void approve() throws Exception {
        BookingResponseDto approved = sample.toBuilder().status(BookingStatus.APPROVED).build();
        when(service.approve(eq(1L), eq(55L), eq(true))).thenReturn(approved);

        mvc.perform(patch(BASE_PATH + "/55")
                        .header(HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    @DisplayName("GET /bookings/{id} ‑ booker fetches booking")
    void getById() throws Exception {
        when(service.getById(1L, 55L)).thenReturn(sample);

        mvc.perform(get(BASE_PATH + "/55")
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id", is(99)));
    }

    @Test
    @DisplayName("GET /bookings?state=ALL ‑ booker list")
    void allByBooker() throws Exception {
        when(service.getAllByBooker(1L, "ALL")).thenReturn(List.of(sample));

        mvc.perform(get(BASE_PATH)
                        .header(HEADER, 1)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /bookings/owner?state=WAITING ‑ owner list")
    void allByOwner() throws Exception {
        when(service.getAllByOwner(1L, "WAITING")).thenReturn(List.of(sample));

        mvc.perform(get(BASE_PATH + "/owner")
                        .header(HEADER, 1)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("WAITING")));
    }

    @DisplayName("PATCH /bookings/{id}?approved=false – бронирование отклонено")
    @Test
    void approve_reject() throws Exception {
        BookingResponseDto rejected = sample.toBuilder()
                .status(BookingStatus.REJECTED)
                .build();
        when(service.approve(1L, 55L, false)).thenReturn(rejected);

        mvc.perform(patch(BASE_PATH + "/55")
                        .header(HEADER, 1)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @DisplayName("GET /bookings – 400 BAD_REQUEST при неизвестном state")
    @Test
    void listBooker_badState() throws Exception {
        when(service.getAllByBooker(1L, "UNKNOWN"))
                .thenThrow(new jakarta.validation.ValidationException());

        mvc.perform(get(BASE_PATH)
                        .header(HEADER, 1)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /bookings/{id} – 404 NOT_FOUND, если не найдено")
    @Test
    void getById_notFound() throws Exception {
        when(service.getById(1L, 123L)).thenThrow(NoSuchElementException.class);

        mvc.perform(get(BASE_PATH + "/123").header(HEADER, 1))
                .andExpect(status().isNotFound());
    }
}
