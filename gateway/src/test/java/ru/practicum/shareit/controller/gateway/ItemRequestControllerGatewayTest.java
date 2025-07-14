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
import ru.practicum.shareit.client.RequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@ContextConfiguration(classes = ru.practicum.shareit.ShareItGateway.class)
class ItemRequestControllerGatewayTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean RequestClient client;

    private final ItemRequestDto sample = ItemRequestDto.builder()
            .id(1L).description("Need ladder").created(LocalDateTime.now()).build();

    @Test
    void createRequest_ok() throws Exception {
        when(client.add(eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(sample));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void own_ok() throws Exception {
        when(client.getOwn(1L)).thenReturn(ResponseEntity.ok(List.of(sample)));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description", is("Need ladder")));
    }

    @Test
    void others_ok() throws Exception {
        when(client.getOthers(1L, 0, 10)).thenReturn(ResponseEntity.ok(List.of(sample)));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void one_ok() throws Exception {
        when(client.getOne(1L, 1L)).thenReturn(ResponseEntity.ok(sample));

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("POST /requests - blank description → 400 BAD REQUEST")
    void create_blankDescription() throws Exception {
        var bad = sample.toBuilder().description("  ").build();

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }
}
