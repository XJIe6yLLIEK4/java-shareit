package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String BASE_PATH = "/items";
    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean ItemService service;

    private final ItemDto sample = ItemDto.builder()
            .id(99L)
            .name("Drill")
            .description("Powerful drill")
            .available(true)
            .build();

    @Test
    @DisplayName("POST /items ‑ should create item and return 201")
    void create() throws Exception {
        when(service.create(eq(1L), any())).thenReturn(sample);

        mvc.perform(post(BASE_PATH)
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(99)));
    }

    @Test
    @DisplayName("GET /items/{id} ‑ owner request returns full DTO")
    void getByIdOwner() throws Exception {
        when(service.getById(1L, 99L)).thenReturn(sample);

        mvc.perform(get(BASE_PATH + "/99")
                        .header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Powerful drill")));
    }

    @Test
    @DisplayName("GET /items/search ‑ should return search results")
    void search() throws Exception {
        when(service.search("drill")).thenReturn(List.of(sample));

        mvc.perform(get(BASE_PATH + "/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("POST /items/{id}/comment ‑ should add comment and return 200")
    void addComment() throws Exception {
        CommentDto comment = CommentDto.builder()
                .id(10L)
                .text("Great tool")
                .authorName("John")
                .build();
        when(service.addComment(eq(1L), eq(99L), any())).thenReturn(comment);

        mvc.perform(post(BASE_PATH + "/99/comment")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is("Great tool")));
    }

    @DisplayName("PATCH /items/{id} – частичное обновление владельцем")
    @Test
    void patch_item() throws Exception {
        ItemDto patch = ItemDto.builder().description("new desc").build();
        ItemDto patched = sample.toBuilder().description("new desc").build();

        when(service.update(eq(1L), eq(99L), any())).thenReturn(patched);

        mvc.perform(patch(BASE_PATH + "/99")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("new desc"));
    }

    @DisplayName("GET /items/search – пустой результат при text=''")
    @Test
    void search_emptyText() throws Exception {
        when(service.search("")).thenReturn(List.of());

        mvc.perform(get(BASE_PATH + "/search").param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

