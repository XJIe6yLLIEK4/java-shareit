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
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BookingClient;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@ContextConfiguration(classes = ru.practicum.shareit.ShareItGateway.class)
class BookingControllerGatewayTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean BookingClient client;

    private static Map<String, Object> buildResp(String status) {
        return Map.of(
                "id", 1,
                "item", Map.of("id", 2, "name", "Drill"),
                "booker", Map.of("id", 3, "name", "John"),
                "start", LocalDateTime.now().plusDays(1).toString(),
                "end", LocalDateTime.now().plusDays(2).toString(),
                "status", status
        );
    }

    private final Map<String, Object> resp = buildResp("WAITING");

    @Test
    void listBookings_ok() throws Exception {
        when(client.getBookings(1L, BookingState.ALL, 0, 10))
                .thenReturn(ResponseEntity.ok(List.of(resp)));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "ALL")
                        .param("from", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void createBooking_ok() throws Exception {
        var dto = new BookItemRequestDto(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(client.bookItem(eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(resp));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    void getOne_ok() throws Exception {
        when(client.getBooking(1L, 1L)).thenReturn(ResponseEntity.ok(resp));

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id", is(2)));
    }

    @Test
    void approve_ok() throws Exception {
        Map<String, Object> approved = new LinkedHashMap<>(resp);
        approved.put("status", "APPROVED");

        when(client.approve(1L, 1L, true))
                .thenReturn(ResponseEntity.ok(approved));

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void ownerBookings_ok() throws Exception {
        when(client.getAllByOwner(1L, "ALL"))
                .thenReturn(ResponseEntity.ok(List.of(resp)));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @DisplayName("GET /bookings – невалидный state → 400 BAD REQUEST")
    void listBookings_badState() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("POST /bookings – end < start → 400 BAD REQUEST")
    void create_endBeforeStart_badRequest() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        var bad = new BookItemRequestDto(2L, now.plusDays(2), now.plusDays(1));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("PATCH /bookings/{id}?approved=foo → 400 BAD REQUEST")
    void approve_badBooleanParam() throws Exception {
        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "foo"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("GET /bookings – отрицательный from → 400 BAD REQUEST")
    void list_negativeFrom() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "ALL")
                        .param("from", "-5")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }
}
