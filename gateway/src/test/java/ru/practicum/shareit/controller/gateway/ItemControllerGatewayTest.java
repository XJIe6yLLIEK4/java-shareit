package ru.practicum.shareit.controller.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.comment.CommentDto;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@ContextConfiguration(classes = ru.practicum.shareit.ShareItGateway.class)
class ItemControllerGatewayTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean ItemClient client;

    private final ItemDto sample = ItemDto.builder()
            .id(1L).name("Drill").description("Cordless").available(true).build();

    @Test
    void addItem_ok() throws Exception {
        when(client.add(eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(sample));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Drill")));
    }

    @Test
    void addItem_blankName() throws Exception {
        ItemDto bad = sample.toBuilder().name("").build();

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void patch_ok() throws Exception {
        when(client.patch(eq(1L), eq(1L), any()))
                .thenReturn(ResponseEntity.ok(sample));

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void get_ok() throws Exception {
        when(client.get(1L, 1L))
                .thenReturn(ResponseEntity.ok(sample));

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Cordless")));
    }

    @Test
    void ownerItems_ok() throws Exception {
        when(client.getOwnerItems(1L, 0, 10))
                .thenReturn(ResponseEntity.ok(java.util.List.of(sample)));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void search_ok() throws Exception {
        when(client.search("drill", 0, 10))
                .thenReturn(ResponseEntity.ok(java.util.List.of(sample)));

        mvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void comment_ok() throws Exception {
        CommentDto comment = CommentDto.builder().id(5L).text("Nice!").authorName("John").build();
        when(client.addComment(eq(1L), eq(1L), any()))
                .thenReturn(ResponseEntity.ok(comment));

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is("Nice!")));
    }

    @Test
    @DisplayName("POST /items - отсутствует заголовок X-Sharer-User-Id → 400 BAD REQUEST")
    void add_noHeader() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("PATCH /items/{id} - пустое тело → 400 BAD REQUEST")
    void patch_emptyBody() throws Exception {
        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }
}
