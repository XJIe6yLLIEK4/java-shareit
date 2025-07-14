package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String BASE_PATH = "/requests";
    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean ItemRequestService service;

    private final ItemRequestDto sample = ItemRequestDto.builder()
            .id(3L)
            .description("Need a ladder")
            .created(LocalDateTime.now())
            .build();

    @Test
    @DisplayName("POST /requests ‑ create request")
    void create() throws Exception {
        when(service.create(eq(1L), any())).thenReturn(sample);

        mvc.perform(post(BASE_PATH)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)));
    }

    @Test
    @DisplayName("GET /requests ‑ own requests")
    void own() throws Exception {
        when(service.getByUser(1L)).thenReturn(List.of(sample));

        mvc.perform(get(BASE_PATH)
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /requests/{id} ‑ get specific request")
    void one() throws Exception {
        when(service.getById(1L, 3L)).thenReturn(sample);

        mvc.perform(get(BASE_PATH + "/3")
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Need a ladder")));
    }

    @DisplayName("GET /requests/{id} – 404 NOT_FOUND, если не найден")
    @Test
    void one_notFound() throws Exception {
        when(service.getById(1L, 777L)).thenThrow(NoSuchElementException.class);

        mvc.perform(get(BASE_PATH + "/777").header(HEADER, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /requests/user - список запросов конкретного пользователя")
    void byUser() throws Exception {
        when(service.getByUser(1L)).thenReturn(List.of(sample));

        mvc.perform(get(BASE_PATH + "/user")
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].description", is("Need a ladder")));
    }
}

