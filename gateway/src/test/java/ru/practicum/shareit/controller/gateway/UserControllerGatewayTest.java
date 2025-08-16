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
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserDto;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = ru.practicum.shareit.ShareItGateway.class)
class UserControllerGatewayTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean UserClient client;

    private final UserDto sample = UserDto.builder()
            .id(1L)
            .name("John")
            .email("john@example.com")
            .build();

    @Test
    void addUser_ok() throws Exception {
        when(client.add(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(sample));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    @Test
    void addUser_badEmail() throws Exception {
        UserDto bad = sample.toBuilder().email("wrong").build();

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void patchUser_ok() throws Exception {
        when(client.patch(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(sample));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getUser_ok() throws Exception {
        when(client.get(1L)).thenReturn(ResponseEntity.ok(sample));

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getAll_ok() throws Exception {
        when(client.getAll(0, 10))
                .thenReturn(ResponseEntity.ok(java.util.List.of(sample)));

        mvc.perform(get("/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email", is("john@example.com")));
    }

    @Test
    void delete_ok() throws Exception {
        when(client.delete(1L)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /users/{id} - пустое тело → 400 BAD REQUEST (и клиент не вызывается)")
    void patch_emptyBody() throws Exception {
        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }
}
