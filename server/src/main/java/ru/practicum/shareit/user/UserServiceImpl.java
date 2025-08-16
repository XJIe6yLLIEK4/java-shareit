package ru.practicum.shareit.user;

import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ExceptionSameEmail;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        if (repository.existsByEmail(userDto.getEmail())) {
            throw new ExceptionSameEmail();
        }
        if (userDto.getEmail() == null) {
            throw new ValidationException("email не может быть пустым");
        }
        User user = UserMapper.toModel(userDto);
        User saved = repository.save(user);
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User existing = repository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (repository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
            throw new ExceptionSameEmail();
        }
        if (userDto.getName() != null) existing.setName(userDto.getName());
        if (userDto.getEmail() != null) existing.setEmail(userDto.getEmail());
        User updated = repository.save(existing);
        return UserMapper.toDto(updated);
    }

    @Override
    public UserDto getById(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return repository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        repository.deleteById(userId);
    }
}
