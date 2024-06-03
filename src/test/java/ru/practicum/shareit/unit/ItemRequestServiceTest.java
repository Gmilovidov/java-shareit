package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequestor;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private ItemRequestMapper itemRequestMapper;

    private User user;
    private Item item;
    private List<Item> items;
    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;
    private ItemRequest itemRequest2;
    private Pageable pageable;

    @BeforeEach
    void beforeEach() {
        ItemMapper itemMapper = new ItemMapper();
        itemRequestMapper = new ItemRequestMapper();

        ReflectionTestUtils.setField(itemRequestService, "itemMapper", itemMapper);
        ReflectionTestUtils.setField(itemRequestService, "itemRequestMapper", itemRequestMapper);

        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("AnotherUser")
                .email("AnotherUser@email.com")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("RequestDescription")
                .build();

        ItemRequestDto itemRequest2Dto = ItemRequestDto.builder()
                .id(2L)
                .description("SecondRequestDescription")
                .build();

        itemRequest = itemRequestMapper.createRequestFromDto(itemRequestDto, user);
        itemRequest2 = itemRequestMapper.createRequestFromDto(itemRequest2Dto, user2);

        item = Item.builder()
                .id(1L)
                .description("ItemDescription")
                .available(true)
                .request(itemRequest)
                .build();

        items = List.of(item);

        pageable = PageRequest.of(0, 10, Sort.by("created").descending());
    }

    @Test
    void createShouldCreateItemRequest() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.save(Mockito.any()))
                .thenReturn(itemRequestMapper.createRequestFromDto(itemRequestDto, user));

        ItemRequestDto createdRequest = itemRequestService.createRequest(1L, itemRequestDto);

        Mockito.verify(itemRequestRepository).save(Mockito.any());

        assertEquals(itemRequestDto.getId(), createdRequest.getId());
        assertEquals(itemRequestDto.getDescription(), createdRequest.getDescription());
    }

    @Test
    void createShouldThrowDataNotFoundException_WhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.createRequest(999L, itemRequestDto));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestsShouldThrowDataNotFoundException_WhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.getRequests(999L));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestsShouldGetRequests() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findAllByRequestId(1L)).thenReturn(items);
        Mockito.when(itemRequestRepository.findAllByRequestorId(1L)).thenReturn(List.of(itemRequest));

        List<ItemRequestDtoRequestor> returnedRequests = itemRequestService.getRequests(1L);

        Mockito.verify(itemRequestRepository).findAllByRequestorId(1L);

        assertEquals(1, returnedRequests.size());
        assertEquals(itemRequest.getId(), returnedRequests.get(0).getId());
        assertEquals(itemRequest.getDescription(), returnedRequests.get(0).getDescription());
        assertEquals(1, returnedRequests.get(0).getItems().size());
        assertEquals(item.getId(), returnedRequests.get(0).getItems().get(0).getId());
    }

    @Test
    void getRequestByIdShouldThrowDataNotFoundException_WhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.getRequestById(999L, 1L));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestByIdShouldThrowDataNotFoundException_WhenRequestNotExist() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.getRequestById(1L, 999L));

        assertEquals("Не найден запрос на предмет с id = 999", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestByIdShouldGetRequestById() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestId(1L)).thenReturn(items);

        ItemRequestDtoRequestor returnedRequest = itemRequestService.getRequestById(1L, 1L);

        Mockito.verify(itemRequestRepository).findById(1L);

        assertEquals(itemRequest.getId(), returnedRequest.getId());
        assertEquals(itemRequest.getDescription(), returnedRequest.getDescription());
        assertEquals(1, returnedRequest.getItems().size());
        assertEquals(item.getId(), returnedRequest.getItems().get(0).getId());
    }

    @Test
    void getRequestsByPageShouldThrowDataNotFoundException_WhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemRequestService.getRequestsByPage(999L, 0, 10));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestsByPageShouldGetRequestsByPage() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findAllByRequestorIdNot(1L, pageable)).thenReturn(List.of(itemRequest2));
        Mockito.when(itemRepository.findAllByRequestId(2L)).thenReturn(items);

        List<ItemRequestDtoRequestor> returnedRequests = itemRequestService.getRequestsByPage(1L, 0, 10);

        Mockito.verify(itemRequestRepository).findAllByRequestorIdNot(1L, pageable);

        assertEquals(1, returnedRequests.size());
        assertEquals(itemRequest2.getId(), returnedRequests.get(0).getId());
        assertEquals(itemRequest2.getDescription(), returnedRequests.get(0).getDescription());
        assertEquals(1, returnedRequests.get(0).getItems().size());
        assertEquals(item.getId(), returnedRequests.get(0).getItems().get(0).getId());
    }
}