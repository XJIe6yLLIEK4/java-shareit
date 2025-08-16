package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ExceptionSameEmail;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean UserService userService;

    private String json(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("POST /users - 201 CREATED")
    void create_ok() throws Exception {
        UserDto in  = new UserDto(null, "Bob", "bob@mail.com");
        UserDto out = new UserDto(1L, "Bob", "bob@mail.com");
        when(userService.create(any())).thenReturn(out);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(in)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("bob@mail.com"));
    }

    @Test
    @DisplayName("POST /users - 409 duplicated e-mail")
    void create_duplicateEmail() throws Exception {
        when(userService.create(any()))
                .thenThrow(new ExceptionSameEmail("email exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UserDto(null, "Bob", "bob@mail.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PATCH /users/{id} - partial update")
    void patch_ok() throws Exception {
        UserDto patch = new UserDto(null, "Bobby", null);
        UserDto out   = new UserDto(1L, "Bobby", "bob@mail.com");

        when(userService.update(eq(1L), any())).thenReturn(out);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bobby"))
                .andExpect(jsonPath("$.email").value("bob@mail.com"));
    }

    @Test
    @DisplayName("GET /users/{id} - 404 NOT FOUND")
    void get_notFound() throws Exception {
        when(userService.getById(99L)).thenThrow(new NoSuchElementException());

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("PATCH /users/{id} – 409 CONFLICT, если e-mail уже занят")
    @Test
    void patch_conflictEmail() throws Exception {
        when(userService.update(eq(1L), any()))
                .thenThrow(new ExceptionSameEmail("duplicate"));

        UserDto patch = new UserDto(null, null, "dup@mail.com");

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(patch)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("duplicate")));
    }

    @DisplayName("GET /users – 200 OK и пустой список")
    @Test
    void list_empty() throws Exception {
        when(userService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("DELETE /users/{id} – 200 OK и вызов сервиса")
    void delete_ok() throws Exception {
        org.mockito.Mockito.doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(userService).delete(1L);
    }
}
