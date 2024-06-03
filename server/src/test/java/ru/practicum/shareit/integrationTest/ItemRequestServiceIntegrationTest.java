package ru.practicum.shareit.integrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequestor;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    private UserDto userDto;
    private UserDto user2Dto;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void beforeEach() {

        userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();

        user2Dto = UserDto.builder()
                .id(2L)
                .name("AnotherUser")
                .email("AnotherUser@email.com")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("RequestDescription")
                .build();
    }

    @Test
    void create_shouldCreateItemRequest() {
        userService.createUser(userDto);

        ItemRequestDto createdRequest = itemRequestService.createRequest(1L, itemRequestDto);

        assertEquals(itemRequestDto.getId(), createdRequest.getId());
        assertEquals(itemRequestDto.getDescription(), createdRequest.getDescription());
    }

    @Test
    void create_shouldThrowDataNotFoundException_WhenUserNotExist() {
        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.createRequest(999L, itemRequestDto));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getRequests_shouldThrowDataNotFoundException_WhenUserNotExist() {
        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.getRequests(999L));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getRequests_shouldGetRequests() {
        userService.createUser(userDto);
        itemRequestService.createRequest(1L, itemRequestDto);

        List<ItemRequestDtoRequestor> returnedRequests = itemRequestService.getRequests(1L);

        assertEquals(1, returnedRequests.size());
        assertEquals(itemRequestDto.getId(), returnedRequests.get(0).getId());
        assertEquals(itemRequestDto.getDescription(), returnedRequests.get(0).getDescription());
    }

    @Test
    void getRequestById_shouldThrowDataNotFoundException_WhenUserNotExist() {
        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.getRequestById(999L, 1L));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestById_shouldThrowDataNotFoundException_WhenRequestNotExist() {
        userService.createUser(userDto);

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.getRequestById(1L, 999L));

        assertEquals("Не найден запрос на предмет с id = 999", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestById_shouldGetRequestById() {
        userService.createUser(userDto);
        itemRequestService.createRequest(1L, itemRequestDto);

        ItemRequestDtoRequestor returnedRequest = itemRequestService.getRequestById(1L, 1L);

        assertEquals(itemRequestDto.getId(), returnedRequest.getId());
        assertEquals(itemRequestDto.getDescription(), returnedRequest.getDescription());
    }

    @Test
    void getRequestsByPage_shouldThrowDataNotFoundException_WhenUserNotExist() {
        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.getRequestsByPage(999L, 0, 10));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestsByPage_shouldGetRequestsByPage() {
        userService.createUser(userDto);
        userService.createUser(user2Dto);
        itemRequestService.createRequest(2L, itemRequestDto);

        List<ItemRequestDtoRequestor> returnedRequests = itemRequestService.getRequestsByPage(1L, 0, 10);

        assertEquals(1, returnedRequests.size());
        assertEquals(itemRequestDto.getId(), returnedRequests.get(0).getId());
        assertEquals(itemRequestDto.getDescription(), returnedRequests.get(0).getDescription());
    }
}