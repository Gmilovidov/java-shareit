package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers();

    UserDto getUserById(Long id);

    UserDto createUser(UserDto userDto);

    UserDto update(Long id, UserDto userDto);

    void delete(Long id);
}
