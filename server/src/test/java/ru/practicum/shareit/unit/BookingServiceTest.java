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
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.mapper.BookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.AvailableException;
import ru.practicum.shareit.exceptions.WrongAccessException;
import ru.practicum.shareit.exceptions.WrongStatusException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.mapper.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private Booking booking;
    private Booking updatedBooking;
    private BookingDtoIn bookingDtoInput;
    private Booking booking1;
    private Booking booking2;
    private User user;
    private User user2;
    private Item item;
    private Item item2;
    private Item item3;
    private Pageable pageable;

    @BeforeEach
    void beforeEach() {
        BookingMapper bookingMapper = new BookingMapperImpl();
        ItemMapper itemMapper = new ItemMapper();
        UserMapper userMapper = new UserMapperImpl();
        ReflectionTestUtils.setField(bookingService, "bookingMapper", bookingMapper);
        ReflectionTestUtils.setField(bookingService, "itemMapper", itemMapper);
        ReflectionTestUtils.setField(bookingService, "userMapper", userMapper);


        bookingDtoInput = BookingDtoIn.builder()
                .bookingId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .itemId(1L)
                .build();

        BookingDtoIn previousBooking = BookingDtoIn.builder()
                .bookingId(1L)
                .start(LocalDateTime.of(2024, 3, 1, 10, 0))
                .end(LocalDateTime.of(2024, 3, 1, 11, 0))
                .itemId(1L)
                .build();

        BookingDtoIn futureBooking = BookingDtoIn.builder()
                .bookingId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(3))
                .itemId(1L)
                .build();

        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();

        user2 = User.builder()
                .id(2L)
                .name("AnotherUser")
                .email("AnotherUser@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .description("ItemDescription")
                .available(true)
                .owner(user)
                .build();

        item2 = Item.builder()
                .id(2L)
                .description("SecondItemDescription")
                .available(true)
                .owner(user)
                .build();

        item3 = Item.builder()
                .id(3L)
                .description("ThirdItemDescription")
                .available(true)
                .owner(user)
                .build();

        booking = bookingMapper.getBookingFromDto(bookingDtoInput, item, user2, BookingStatus.WAITING);
        booking1 = bookingMapper.getBookingFromDto(previousBooking, item, user2, BookingStatus.WAITING);
        booking2 = bookingMapper.getBookingFromDto(futureBooking, item, user2, BookingStatus.APPROVED);
        updatedBooking = bookingMapper.getBookingFromDto(bookingDtoInput, item, user2, BookingStatus.APPROVED);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createShouldCreateBooking() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        BookingDtoOut createdBooking = bookingService.create(2L, bookingDtoInput);

        Mockito.verify(bookingRepository).save(Mockito.any());

        assertEquals(bookingDtoInput.getBookingId(), createdBooking.getId());
        assertEquals(bookingDtoInput.getStart(), createdBooking.getStart());
        assertEquals(bookingDtoInput.getEnd(), createdBooking.getEnd());
        assertEquals(item.getId(), createdBooking.getItem().getId());
        assertEquals(item.getName(), createdBooking.getItem().getName());
        assertEquals(user2.getId(), createdBooking.getBooker().getId());
        assertEquals(user2.getName(), createdBooking.getBooker().getName());
    }

    @Test
    void createShouldThrowDataNotFoundExceptionWhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingService.create(999L, bookingDtoInput));

        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    void createShouldThrowDataNotFoundExceptionWhenItemNotExist() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.create(2L, bookingDtoInput));

        assertEquals("Вещи с id=1 нет.", dataNotFoundException.getMessage());
    }

    @Test
    void createShouldThrowDataNotFoundExceptionWhenBookerIsOwner() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.create(1L, bookingDtoInput));

        assertEquals("Нельзя забронировать свою вещь", dataNotFoundException.getMessage());
    }

    @Test
    void createShouldThrowAvailableExceptionWhenItemNotAvailable() {
        item.setAvailable(false);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        AvailableException exception = assertThrows(AvailableException.class,
                () -> bookingService.create(2L, bookingDtoInput));

        assertEquals("Вещь забронирована", exception.getMessage());
    }

    @Test
    void updateStatusShouldThrowDataNotFoundExceptionWhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.updateStatus(999L, 1L, true));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void updateStatusShouldThrowDataNotFoundExceptionWhenBookingNotExist() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.updateStatus(2L, 999L, true));

        assertEquals("Бронь с id = 999 не найдена", dataNotFoundException.getMessage());
    }

    @Test
    void updateStatusShouldThrowWrongAccessExceptionWhenNotOwner() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        WrongAccessException exception = assertThrows(WrongAccessException.class,
                () -> bookingService.updateStatus(2L, 1L, true));

        assertEquals("Нельзя изменить статус брони вещи, пользователю, с ids 1 и 2 соответственно", exception.getMessage());
    }

    @Test
    void updateStatusShouldThrowWrongStatusException_WhenStatusNotWaiting() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        booking.setStatus(BookingStatus.APPROVED);

        WrongStatusException exception = assertThrows(WrongStatusException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        assertEquals("Нельзя изменить статус вещи", exception.getMessage());
    }

    @Test
    void updateStatusShouldUpdateStatus() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(updatedBooking);
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BookingDtoOut returnedBooking = bookingService.updateStatus(1L, 1L, true);

        Mockito.verify(bookingRepository).save(Mockito.any());

        assertEquals(BookingStatus.APPROVED, returnedBooking.getStatus());
        assertEquals(booking.getId(), returnedBooking.getId());
    }

    @Test
    void getBookingShouldThrowDataNotFoundException_WhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.getBooking(999L, 1L));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getBookingShouldThrowDataNotFoundException_WhenBookingNotFindByUser() {
        booking.setBooker(user);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.getBooking(2L, 1L));

        assertEquals("Вещь не найдена", dataNotFoundException.getMessage());
    }

    @Test
    void getBookingShouldThrowDataNotFoundExceptionWhenBookingNotExist() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingService.getBooking(2L, 999L));

        assertEquals("Бронь с id = 999 не найдена", exception.getMessage());
    }

    @Test
    void getBookingShouldGetBooking() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BookingDtoOut returnedBooking =  bookingService.getBooking(2L, 1L);

        assertEquals(booking.getId(), returnedBooking.getId());
        assertEquals(booking.getItem().getId(), returnedBooking.getItem().getId());
        assertEquals(booking.getBooker().getId(), returnedBooking.getBooker().getId());
    }

    @Test
    void getAllBookerBookingsShouldThrowDataNotFoundExceptionWhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.getAllBooker(999L, "ALL", 0, 10));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getAllBookerBookingsShouldThrowWrongStatusExceptionWhenWrongState() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        WrongStatusException exception = assertThrows(WrongStatusException.class,
                () -> bookingService.getAllBooker(1L, " ", 0, 10));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @Test
    void getAllBookerBookingsWhenStateALL() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findAllByBooker_IdOrderByStartTimeDesc(2L, pageable))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "ALL", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllBookerBookingsWhenStateCURRENT() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.readAllBookerCurrentBookings(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "CURRENT", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllBookerBookingsWhenStatePAST() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.readAllBookerPastBookings(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "PAST", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllBookerBookingsWhenStateFUTURE() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.readAllBookerFutureBookings(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "FUTURE", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllBookerBookingsWhenStateWAITING_OrREJECTED() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findAllByBooker_IdAndStatusOrderByStartTimeDesc(Mockito.anyLong(),
                        Mockito.any(BookingStatus.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "WAITING", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookingsShouldThrowDataNotFoundExceptionWhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.getAllOwnerItem(999L, "ALL", 0, 10));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getAllOwnerItemBookingsShouldThrowWrongStatusExceptionWhenWrongState() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of(item, item2, item3));

        WrongStatusException exception = assertThrows(WrongStatusException.class,
                () -> bookingService.getAllOwnerItem(1L, " ", 0, 10));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @Test
    void getAllOwnerItemBookingsWhenStateALL() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findAllByOwnerId(2L)).thenReturn(List.of(item, item2, item3));
        Mockito.when(bookingRepository.findAllByItem_IdInOrderByStartTimeDesc(List.of(1L, 2L, 3L), pageable))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(2L, "ALL", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookingsWhenStateCURRENT() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findAllByOwnerId(2L)).thenReturn(List.of(item, item2, item3));
        Mockito.when(bookingRepository.readAllOwnerItemsCurrentBookings(Mockito.anyList(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(2L, "CURRENT", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookingsWhenStatePAST() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findAllByOwnerId(2L)).thenReturn(List.of(item, item2, item3));
        Mockito.when(bookingRepository.readAllOwnerItemsPastBookings(Mockito.anyList(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(2L, "PAST", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookingsWhenStateFUTURE() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findAllByOwnerId(2L)).thenReturn(List.of(item, item2, item3));
        Mockito.when(bookingRepository.readAllOwnerItemsFutureBookings(Mockito.anyList(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(2L, "FUTURE", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookingsWhenStateWAITING_or_REJECTED() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findAllByOwnerId(2L)).thenReturn(List.of(item, item2, item3));
        Mockito.when(bookingRepository.findAllByItem_IdInAndStatusInOrderByStartTimeDesc(List.of(1L, 2L, 3L),
                        List.of(BookingStatus.WAITING), pageable))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(2L, "WAITING", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }
}