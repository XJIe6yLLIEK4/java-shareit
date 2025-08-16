package ru.practicum.shareit.serviceIT;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.exception.ExceptionSameEmail;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureTestDatabase
@Transactional
class UserServiceImplIT {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepo;

    @Test
    void create_and_get_ok() {
        UserDto dto = UserDto.builder()
                .name("John")
                .email("john@mail.com")
                .build();

        UserDto saved = userService.create(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepo.findById(saved.getId())).isPresent();
        assertThat(userService.getById(saved.getId()).getEmail())
                .isEqualTo("john@mail.com");
    }

    @Test
    void duplicateEmail_throws() {
        userService.create(new UserDto(null, "Ann", "a@mail.com"));
        assertThatThrownBy(() ->
                userService.create(new UserDto(null, "Bob", "a@mail.com")))
                .isInstanceOf(ExceptionSameEmail.class);
    }

    @Test
    void update_changes_only_specified_fields() {
        UserDto orig = userService.create(new UserDto(null, "Mike", "m@mail.com"));

        UserDto patch = UserDto.builder().name("Mikhail").build();
        UserDto updated = userService.update(orig.getId(), patch);

        assertThat(updated.getName()).isEqualTo("Mikhail");
        assertThat(updated.getEmail()).isEqualTo("m@mail.com");
    }

    @Test
    void create_withoutEmail_throwsValidation() {
        assertThatThrownBy(() -> userService.create(new UserDto(null, "x", null)))
                .isInstanceOf(ValidationException.class);
    }
}
