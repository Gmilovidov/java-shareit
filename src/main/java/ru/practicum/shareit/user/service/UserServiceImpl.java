package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(userMapper::getUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(int id) {
        return userMapper.getUserDto(userStorage.getUserById(id)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + id + " не найден")));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.createUserDto(userDto);
        return userMapper.getUserDto(userStorage.createUser(user));
    }

    @Override
    public UserDto update(int id, UserDto userDto) {
        User user = User.buildUser(userStorage.getUserById(id)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + id + " не найден")));
        user = updateUserFromDto(user, userDto);
        return userMapper.getUserDto(userStorage.update(id, user));
    }

    @Override
    public void delete(int id) {
        userStorage.delete(id);
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
