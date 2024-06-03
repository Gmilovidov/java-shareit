package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    private User user;
    private UserDto userDto;
    private UserMapperImpl userMapper;

    @BeforeEach
    void beforeEach() {
        userMapper = new UserMapperImpl();
        userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email("name@mail.ru")
                .build();
        user = userMapper.createUserDto(userDto);
        ReflectionTestUtils.setField(userService, "userMapper", userMapper);
    }

    @Test
    void createUser() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        UserDto savedUser = userService.createUser(userDto);
        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(userDto.getId(), savedUser.getId());
        assertEquals(userDto.getName(), savedUser.getName());
        assertEquals(userDto.getEmail(), savedUser.getEmail());
    }

    @Test
    void updateUser() {
        UserDto userDtoUpdate = UserDto.builder()
                .id(1L)
                .name("UpdatedName")
                .email("updatedName@mail.ru")
                .build();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(userMapper.createUserDto(userDtoUpdate));
        UserDto updatedUser = userService.update(userDtoUpdate.getId(), userDtoUpdate);

        Mockito.verify(userRepository).findById(userDtoUpdate.getId());
        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(userDtoUpdate.getEmail(), updatedUser.getEmail());
        assertEquals(userDtoUpdate.getName(), updatedUser.getName());
    }

    @Test
    void getUser() {
        Mockito.when((userRepository.findById(1L))).thenReturn(Optional.of(user));
        UserDto savedUser = userService.getUserById(1L);

        Mockito.verify(userRepository).findById(user.getId());

        assertEquals(userDto.getId(), savedUser.getId());
        assertEquals(userDto.getName(), savedUser.getName());
        assertEquals(userDto.getEmail(), savedUser.getEmail());
    }

    @Test
    void getAllUsers() {
        List<User> users = List.of(user);

        Mockito.when(userRepository.findAll()).thenReturn(users);
        List<UserDto> userDtos = userService.getUsers();

        Mockito.verify(userRepository).findAll();

        assertEquals(userDto.getId(), userDtos.get(0).getId());
        assertEquals(userDto.getName(), userDtos.get(0).getName());
        assertEquals(userDto.getEmail(), userDtos.get(0).getEmail());
        assertEquals(users.size(), userDtos.size());
    }

    @Test
    void dataNotFoundExcForGetUser() {
        Mockito.when(userRepository.findById(100L)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> userService.getUserById(100L));

        assertEquals("Пользователь с id=100 не найден", exception.getMessage());
    }

    @Test
    void dataNotFoundExcForUpdateUser() {
        Mockito.when(userRepository.findById(100L)).thenReturn(Optional.empty());

        UserDto userDtoUpdate = UserDto.builder()
                .id(1L)
                .name("UpdatedName")
                .email("updatedName@mail.ru")
                .build();

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.update(100L, userDtoUpdate));

        assertEquals("Пользователь с id=100 не найден", exception.getMessage());
    }

    @Test
    void dataNotFoundExcForDeleteUser() {
        Mockito.when(userRepository.findById(100L)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.delete(100L));

        assertEquals("Пользователь с id=100 не найден", exception.getMessage());
    }
}
