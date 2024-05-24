package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.DuplicateDataException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::getUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.getUserDto(userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + id + " не найден")));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        try {
            User user = userMapper.createUserDto(userDto);
            return userMapper.getUserDto(userRepository.save(user));
        } catch (ConstraintViolationException exception) {
            throw new DuplicateDataException(exception.getMessage());
        }


    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + id + " не найден"));
        user = updateUserFromDto(user, userDto);
        return userMapper.getUserDto(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User getUserByIdWithoutDto(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public User updateUserFromDto(User user, UserDto userDto) {
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        return user;
    }
}
