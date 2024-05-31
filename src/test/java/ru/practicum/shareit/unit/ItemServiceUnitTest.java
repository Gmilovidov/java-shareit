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
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.mapper.BookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.AvailableException;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.WrongIdException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.CommentMapperImpl;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.mapper.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    private ItemMapper itemMapper;
    private CommentMapper commentMapper;

    private User user;
    private User user2;
    private Item item;
    private ItemDto itemDto;
    private ItemRequest itemRequest;
    private Item item2;
    private ItemDto itemDtoToupdate;
    private CommentDto commentDto;
    private Booking lastBooking;
    private Pageable pageable;
    private List<Booking> bookings;
    private List<Item> items;

    @BeforeEach
    void beforeEach() {
        itemMapper = new ItemMapper();
        commentMapper = new CommentMapperImpl();
        UserMapper userMapper = new UserMapperImpl();
        BookingMapper bookingMapper = new BookingMapperImpl();

        ReflectionTestUtils.setField(itemService, "itemMapper", itemMapper);
        ReflectionTestUtils.setField(itemService, "commentMapper", commentMapper);
        ReflectionTestUtils.setField(itemService, "bookingMapper", bookingMapper);

        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();
        user = userMapper.createUserDto(userDto);

        UserDto user2Dto = UserDto.builder()
                .id(2L)
                .name("SecondUser")
                .email("secondUser@email.com")
                .build();
        user2 = userMapper.createUserDto(user2Dto);

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("ItemDescription")
                .available(true)
                .requestId(1L)
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("ItemRequestDescription")
                .created(LocalDateTime.now())
                .build();

        item = itemMapper.createItemFromDto(itemDto, itemRequest);

        ItemDto item2Dto = ItemDto.builder()
                .id(2L)
                .name("Item2")
                .description("Item2Description")
                .available(true)
                .requestId(2L)
                .build();

        ItemRequest item2Request = ItemRequest.builder()
                .id(2L)
                .description("Item2RequestDescription")
                .created(LocalDateTime.now())
                .build();

        item2 = itemMapper.createItemFromDto(item2Dto, item2Request);

        lastBooking = Booking.builder()
                .id(1L)
                .booker(user)
                .item(item)
                .startTime(LocalDateTime.now().minusHours(2))
                .endTime(LocalDateTime.now().minusHours(1))
                .status(BookingStatus.APPROVED)
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .booker(user2)
                .item(item)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.APPROVED)
                .build();

        bookings = List.of(lastBooking, nextBooking);

        pageable = PageRequest.of(0, 10);

        items = List.of(item, item2);

        commentDto = CommentDto.builder()
                .id(1L)
                .text("commentText")
                .build();
    }

    @Test
    void createShouldCreateItemWithRequestId() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item);
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));

        ItemDto createdItemDto = itemService.createItem(1L, itemDto);

        Mockito.verify(userRepository).findById(1L);
        Mockito.verify(itemRequestRepository).findById(1L);
        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(itemDto.getName(), createdItemDto.getName());
        assertEquals(itemDto.getDescription(), createdItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), createdItemDto.getAvailable());
        assertEquals(itemDto.getRequestId(), createdItemDto.getRequestId());
    }

    @Test
    void createShouldCreateItemWithoutRequestId() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item);

        itemDto.setRequestId(null);
        ItemDto createdItemDto = itemService.createItem(1L, itemDto);

        Mockito.verify(userRepository).findById(1L);
        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(itemDto.getName(), createdItemDto.getName());
        assertEquals(itemDto.getDescription(), createdItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), createdItemDto.getAvailable());
    }

    @Test
    void createShouldThrowDataNotFoundException_WhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.createItem(999L, itemDto));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getItemByIdShouldThrowDataNotFoundException_WhenUserNotFound() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.getItemById(999L, 1L));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getItemByIdShouldThrowDataNotFoundException_WhenItemNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.getItemById(1L,999L));

        assertEquals("Предмет с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getItemByIdShouldReturnItemWithBookings_WhenUserIsOwner() {
        item.setOwner(user);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByItemId(1L)).thenReturn(bookings);
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(List.of());
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto returnedItem = itemService.getItemById(1L, 1L);

        Mockito.verify(itemRepository).findById(1L);

        assertEquals(itemDto.getId(), returnedItem.getId());
        assertEquals(itemDto.getName(), returnedItem.getName());
        assertEquals(itemDto.getAvailable(), returnedItem.getAvailable());
        assertEquals(1L, returnedItem.getLastBooking().getId());
        assertEquals(1L, returnedItem.getLastBooking().getBookerId());
        assertEquals(2L, returnedItem.getNextBooking().getId());
        assertEquals(2L, returnedItem.getNextBooking().getBookerId());
    }

    @Test
    void getItemByIdShouldReturnItemWithoutBookings_WhenUserIsNotOwner() {
        item.setOwner(user2);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(List.of());
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto returnedItem = itemService.getItemById(1L, 1L);

        Mockito.verify(itemRepository).findById(1L);

        assertEquals(itemDto.getId(), returnedItem.getId());
        assertEquals(itemDto.getName(), returnedItem.getName());
        assertEquals(itemDto.getAvailable(), returnedItem.getAvailable());
        assertNull(itemDto.getLastBooking());
        assertNull(itemDto.getNextBooking());
    }

    @Test
    void getItemsShouldThrowDataNotFoundException_WhenUserNotFound() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.getItems(999L, 1, 10));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getItemsShouldReturnItems() {
        item.setOwner(user);
        item2.setOwner(user2);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByItemId(1L)).thenReturn(bookings);
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(List.of());
        Mockito.when(itemRepository.findAllByOwnerId(1L, pageable)).thenReturn(items);

        List<ItemDto> returnedItems = itemService.getItems(1L, 0, 10);

        Mockito.verify(itemRepository).findAllByOwnerId(1L, pageable);

        assertEquals(items.size(), returnedItems.size());

        assertEquals(items.get(0).getId(), returnedItems.get(0).getId());
        assertEquals(items.get(0).getName(), returnedItems.get(0).getName());
        assertEquals(items.get(0).getDescription(), returnedItems.get(0).getDescription());
        assertEquals(items.get(0).getAvailable(), returnedItems.get(0).getAvailable());
        assertEquals(1L, returnedItems.get(0).getLastBooking().getId());
        assertEquals(1L, returnedItems.get(0).getLastBooking().getBookerId());
        assertEquals(2L, returnedItems.get(0).getNextBooking().getId());
        assertEquals(2L, returnedItems.get(0).getNextBooking().getBookerId());

        assertEquals(items.get(1).getId(), returnedItems.get(1).getId());
        assertEquals(items.get(1).getName(), returnedItems.get(1).getName());
        assertEquals(items.get(1).getDescription(), returnedItems.get(1).getDescription());
        assertEquals(items.get(1).getAvailable(), returnedItems.get(1).getAvailable());
    }

    @Test
    void updateShouldThrowDataNotFoundException_WhenUserNotFound() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.update(999L, 1L, itemDtoToupdate));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void updateShouldThrowDataNotFoundException_WhenItemNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.update(1L, 999L, itemDtoToupdate));

        assertEquals("Предмет с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void updateShouldThrowWrongIdException_WhenItemNotFound() {
        item.setOwner(user2);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        WrongIdException exception = assertThrows(WrongIdException.class,
                () -> itemService.update(1L, 1L, itemDtoToupdate));

        assertEquals("Вещь с id=1 не принадлежит пользователю с id = 1", exception.getMessage());
    }

    @Test
    void updateShouldUpdateItem() {
        item.setOwner(user);
        itemDtoToupdate = ItemDto.builder()
                .id(1L)
                .name("UpdatedItem")
                .description("UpdatedItemDescription")
                .available(false)
                .build();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(itemMapper.updateItemFromDto(item, itemDtoToupdate));
        Mockito.when(bookingRepository.findAllByItemId(1L)).thenReturn(bookings);
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(List.of());

        ItemDto updatedItem = itemService.update(1L, 1L, itemDtoToupdate);

        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(itemDtoToupdate.getId(), updatedItem.getId());
        assertEquals(itemDtoToupdate.getName(), updatedItem.getName());
        assertEquals(itemDtoToupdate.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoToupdate.getAvailable(), updatedItem.getAvailable());
        assertEquals(itemDto.getRequestId(), updatedItem.getRequestId());
    }

    @Test
    void getItemByTextShouldGetItem() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findItemByText("descr", pageable)).thenReturn(items);

        List<ItemDto> returnedItems = itemService.getItemsByText(1L, "descr", 0, 10);

        Mockito.verify(itemRepository).findItemByText("descr", pageable);

        assertEquals(2, returnedItems.size());
        assertEquals(items.get(0).getId(), returnedItems.get(0).getId());
        assertEquals(items.get(0).getName(), returnedItems.get(0).getName());
        assertEquals(items.get(1).getId(), returnedItems.get(1).getId());
        assertEquals(items.get(1).getName(), returnedItems.get(1).getName());
    }

    @Test
    void getItemByTextShouldThrowDataNotFoundException_WhenUserNotFound() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.getItemsByText(999L, "descr", 0, 10));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getItemByTextShouldGetEmptyList_WhenTextIsBlank() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<ItemDto> returnedItems = itemService.getItemsByText(1L, " ", 0, 10);

        assertEquals(0, returnedItems.size());
    }

    @Test
    void createCommentShouldCreateComment() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByItem_IdAndBooker_IdAndStatus(1L, 1L, BookingStatus.APPROVED))
                .thenReturn(List.of(lastBooking));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(commentRepository.save(Mockito.any()))
                .thenReturn(commentMapper.getCommentFromDto(commentDto, item, user));

        CommentDto returnedComment = itemService.createComment(1L, 1L, commentDto);

        Mockito.verify(commentRepository).save(Mockito.any());

        assertEquals(user.getName(), returnedComment.getAuthorName());
        assertEquals(1L, returnedComment.getId());
        assertEquals(commentDto.getText(), returnedComment.getText());
    }

    @Test
    void createCommentShouldThrowDataNotFoundException_WhenUserNotFound() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.createComment(999L, 1L, commentDto));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void createCommentShouldThrowItemAvailabilityException_WhenItemNotBookered() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByItem_IdAndBooker_IdAndStatus(1L, 1L, BookingStatus.APPROVED))
                .thenReturn(List.of());

        AvailableException exception = assertThrows(AvailableException.class,
                () -> itemService.createComment(1L, 1L, commentDto));

        assertEquals("Пользователь не бронировал вещь с id = 1, 1 соответствено", exception.getMessage());
    }

    @Test
    void createCommentShouldThrowDataNotFoundException_WhenItemNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByItem_IdAndBooker_IdAndStatus(999L, 1L, BookingStatus.APPROVED))
                .thenReturn(List.of(lastBooking));
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.createComment(1L, 999L, commentDto));

        assertEquals("Предмет с id=999 не найден", dataNotFoundException.getMessage());
    }
}