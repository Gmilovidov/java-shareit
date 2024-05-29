package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.mapper.BookingMapperImpl;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private  UserService userService;
    @Mock
    private  BookingService bookingService;
    @Mock
    private  CommentRepository commentRepository;
    @Mock
    private  ItemRequestRepository itemRequestRepository;
    private  ItemMapper itemMapper;
    private  BookingMapper bookingMapper;
    private  CommentMapper commentMapper;

    private User user;
    private UserDto userDto;
    private User user1;
    private UserDto userDto1;
    private Item item;
    private ItemDto itemDto;
    private ItemRequest itemRequest;
    private Item item1;
    private ItemDto itemDto1;

    @BeforeEach
    void beforeEach() {
        itemMapper = new ItemMapper();
        bookingMapper = new BookingMapperImpl();
        commentMapper = new CommentMapper();

        ReflectionTestUtils.setField(itemService, "itemMapper", itemMapper);
        ReflectionTestUtils.setField(itemService, "commentMapper", commentMapper);
        ReflectionTestUtils.setField(itemService, "bookingMapper", bookingMapper);
    }
}
