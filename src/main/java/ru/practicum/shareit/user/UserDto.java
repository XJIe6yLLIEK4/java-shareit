package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String name;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неправильный формат email")
    private String email;
}
