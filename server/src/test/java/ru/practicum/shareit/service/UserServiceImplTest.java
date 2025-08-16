package ru.practicum.shareit.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ExceptionSameEmail;
import ru.practicum.shareit.user.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock  UserRepository userRepository;
    @InjectMocks UserServiceImpl userService;

    @Test
    @DisplayName("create(): happy-path сохраняет пользователя")
    void create_ok() {
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setId(1L);
            return u;
        });

        UserDto saved = userService.create(new UserDto(null, "Bob", "bob@mail.com"));

        assertThat(saved.getId()).isEqualTo(1L);
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("create(): дубликат e-mail → ExceptionSameEmail")
    void create_duplicateEmail() {
        when(userRepository.existsByEmail("bob@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(new UserDto(null, "Bob", "bob@mail.com")))
                .isInstanceOf(ExceptionSameEmail.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("create(): null e-mail → ValidationException")
    void create_nullEmail() {
        when(userRepository.existsByEmail(null)).thenReturn(false);

        assertThatThrownBy(() ->
                userService.create(new UserDto(null, "Bob", null)))
                .isInstanceOf(ValidationException.class);

        verify(userRepository).existsByEmail(null);
        verify(userRepository, never()).save(any());
    }

    @Nested
    class Update {

        private final User existing = new User(1L, "Bob", "old@mail.com");

        @Test
        @DisplayName("update(): меняет только имя, e-mail остаётся")
        void update_nameOnly() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserDto dto = userService.update(1L, new UserDto(null, "Bobby", null));

            assertThat(dto.getName()).isEqualTo("Bobby");
            assertThat(dto.getEmail()).isEqualTo("old@mail.com");
        }

        @Test
        @DisplayName("update(): меняет e-mail, имя остаётся")
        void update_emailOnly() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(userRepository.existsByEmailAndIdNot("new@mail.com", 1L)).thenReturn(false);
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserDto dto = userService.update(1L, new UserDto(null, null, "new@mail.com"));

            assertThat(dto.getEmail()).isEqualTo("new@mail.com");
            assertThat(dto.getName()).isEqualTo("Bob");
        }

        @Test
        @DisplayName("update(): e-mail уже занят другим → ExceptionSameEmail")
        void update_duplicateEmail() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(userRepository.existsByEmailAndIdNot("dup@mail.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> userService.update(1L, new UserDto(null, null, "dup@mail.com")))
                    .isInstanceOf(ExceptionSameEmail.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void getById_ok() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "Bob", "bob@mail.com")));

        UserDto dto = userService.getById(1L);

        assertThat(dto.getEmail()).isEqualTo("bob@mail.com");
    }

    @Test
    void getById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getAll_ok() {
        when(userRepository.findAll()).thenReturn(List.of(
                new User(1L, "A", "a@mail.com"),
                new User(2L, "B", "b@mail.com")
        ));

        List<UserDto> list = userService.getAll();

        assertThat(list).hasSize(2)
                .extracting(UserDto::getId)
                .containsExactly(1L, 2L);
    }

    @Test
    void delete_ok() {
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }
}
