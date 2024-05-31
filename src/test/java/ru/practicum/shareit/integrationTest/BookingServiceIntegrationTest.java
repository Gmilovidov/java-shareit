package ru.practicum.shareit.integrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    private UserDto userDto;
    private UserDto user2Dto;
    private ItemDto itemDto;
    private  BookingDtoIn bookingDtoInput;
    private  BookingDtoIn previousBookingDto;
    private  BookingDtoIn futureBookingDto;

    @BeforeEach
    void beforeEach() {
        bookingDtoInput = BookingDtoIn.builder()
                .bookingId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .itemId(1L)
                .build();

        previousBookingDto = BookingDtoIn.builder()
                .bookingId(2L)
                .start(LocalDateTime.of(2024, 3, 1, 10, 0))
                .end(LocalDateTime.of(2024, 3, 1, 11, 0))
                .itemId(1L)
                .build();

        futureBookingDto = BookingDtoIn.builder()
                .bookingId(3L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(3))
                .itemId(1L)
                .build();

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

        itemDto = ItemDto.builder()
                .id(1L)
                .name("ItemName")
                .description("ItemDescription")
                .available(true)
                .build();
    }

    @Test
    void create_shouldCreateBooking() {
        UserDto savedUser = userService.createUser(userDto);
        itemService.createItem(savedUser.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);

        BookingDtoOut createdBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);

        assertEquals(1, createdBooking.getId());
        assertEquals(bookingDtoInput.getStart(), createdBooking.getStart());
        assertEquals(bookingDtoInput.getEnd(), createdBooking.getEnd());
        assertEquals(1, createdBooking.getItem().getId());
        assertEquals(itemDto.getName(), createdBooking.getItem().getName());
        assertEquals(2, createdBooking.getBooker().getId());
        assertEquals(user2Dto.getName(), createdBooking.getBooker().getName());
    }

    @Test
    void create_shouldThrowDataNotFoundException_WhenUserNotExist() {
        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.create(999L, bookingDtoInput));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void create_shouldThrowDataNotFoundException_WhenItemNotExist() {
        userService.createUser(userDto);
        userService.createUser(user2Dto);

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.create(2L, bookingDtoInput));

        assertEquals("Вещи с id=1 нет.", dataNotFoundException.getMessage());
    }

    @Test
    void create_shouldThrowDataNotFoundException_WhenBookerIsOwner() {
        userService.createUser(userDto);
        userService.createUser(user2Dto);
        itemService.createItem(1L, itemDto);

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.create(1L, bookingDtoInput));

        assertEquals("Нельзя забронировать свою вещь", dataNotFoundException.getMessage());
    }

    @Test
    void create_shouldThrowAvailableException_WhenItemNotAvailable() {
        itemDto.setAvailable(false);
        userService.createUser(userDto);
        userService.createUser(user2Dto);
        itemService.createItem(1L, itemDto);

        AvailableException availableException = assertThrows(AvailableException.class,
                () -> bookingService.create(2L, bookingDtoInput));

        assertEquals("Вещь забронирована", availableException.getMessage());
    }

    @Test
    void updateStatus_shouldThrowDataNotFoundException_WhenUserNotExist() {
        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.updateStatus(999L, 1L, true));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void updateStatus_shouldThrowDataNotFoundException_WhenBookingNotExist() {
        userService.createUser(userDto);
        userService.createUser(user2Dto);
        itemService.createItem(1L, itemDto);

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.updateStatus(2L, 999L, true));

        assertEquals("Бронь с id = 999 не найдена", dataNotFoundException.getMessage());
    }

    @Test
    void updateStatus_shouldThrowWrongAccesException_WhenNotOwner() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut createdBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);

        WrongAccessException wrongAccessException = assertThrows(WrongAccessException.class,
                () -> bookingService.updateStatus(savedBooker.getId(), createdBooking.getId(), true));

        assertEquals("Нельзя изменить статус брони вещи, пользователю, с ids 1 и 2 соответственно",
                wrongAccessException.getMessage());
    }

    @Test
    void updateStatus_shouldThrowWrongStatusException_WhenStatusNotWaiting() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut createdBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);
        bookingService.updateStatus(savedOwner.getId(), createdBooking.getId(), false);

        WrongStatusException wrongStatusException = assertThrows(WrongStatusException.class,
                () -> bookingService.updateStatus(savedOwner.getId(), createdBooking.getId(), true));

        assertEquals("Нельзя изменить статус вещи", wrongStatusException.getMessage());
    }

    @Test
    void updateStatus_shouldUpdateStatus() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut createdBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);

        BookingDtoOut returnedBooking = bookingService.updateStatus(savedOwner.getId(),
                createdBooking.getId(), true);

        assertEquals(BookingStatus.APPROVED, returnedBooking.getStatus());
        assertEquals(1, returnedBooking.getId());
    }

    @Test
    void getBooking_shouldThrowDataNotFoundException_WhenUserNotExist() {
        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.getBooking(999L, 1L));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getBooking_shouldThrowDataNotFoundException_WhenBookingNotFindByUser() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        userService.createUser(user2Dto);
        UserDto user3Dto = UserDto.builder()
                .id(3L)
                .name("ThirdUser")
                .email("thirdUser@email.com")
                .build();
        UserDto anotherBooker = userService.createUser(user3Dto);
        bookingService.create(anotherBooker.getId(), bookingDtoInput);

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.getBooking(2L, 1L));

        assertEquals("Для пользователя  с id = 2  бронь не найдена", dataNotFoundException.getMessage());
    }

    @Test
    void getBooking_shouldThrowDataNotFoundException_WhenBookingNotExist() {
        userService.createUser(userDto);
        userService.createUser(user2Dto);

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.getBooking(2L, 999L));

        assertEquals("Бронь с id = 999 не найдена", dataNotFoundException.getMessage());
    }

    @Test
    void getBooking_shouldGetBooking() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut createdBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);

        BookingDtoOut returnedBooking =  bookingService.getBooking(2L, 1L);

        assertEquals(createdBooking.getId(), returnedBooking.getId());
        assertEquals(createdBooking.getItem().getId(), returnedBooking.getItem().getId());
        assertEquals(createdBooking.getBooker().getId(), returnedBooking.getBooker().getId());
    }

    @Test
    void getAllBookerBookings_shouldThrowDataNotFoundException_WhenUserNotExist() {
        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.getAllBooker(999L, "ALL", 0, 10));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getAllBookerBookings_shouldThrowWrongStatusException_WhenWrongState() {
        userService.createUser(userDto);
        WrongStatusException wrongStatusException = assertThrows(WrongStatusException.class,
                () -> bookingService.getAllBooker(1L, " ", 0, 10));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", wrongStatusException.getMessage());
    }

    @Test
    void getAllBookerBookings_WhenStateALL() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut currentBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);
        BookingDtoOut previousBooking = bookingService.create(savedBooker.getId(), previousBookingDto);
        BookingDtoOut futureBooking = bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "ALL", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(futureBooking.getId(), returnedBookings.get(0).getId());
        assertEquals(currentBooking.getId(), returnedBookings.get(1).getId());
        assertEquals(previousBooking.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllBookerBookings_WhenStateCURRENT() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut currentBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);
        bookingService.create(savedBooker.getId(), previousBookingDto);
        bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "CURRENT", 0, 10);

        assertEquals(1, returnedBookings.size());
        assertEquals(currentBooking.getId(), returnedBookings.get(0).getId());
    }

    @Test
    void getAllBookerBookings_WhenStatePAST() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        bookingService.create(savedBooker.getId(), bookingDtoInput);
        BookingDtoOut previousBooking = bookingService.create(savedBooker.getId(), previousBookingDto);
        bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "PAST", 0, 10);

        assertEquals(1, returnedBookings.size());
        assertEquals(previousBooking.getId(), returnedBookings.get(0).getId());
    }

    @Test
    void getAllBookerBookings_WhenStateFUTURE() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        bookingService.create(savedBooker.getId(), bookingDtoInput);
        bookingService.create(savedBooker.getId(), previousBookingDto);
        BookingDtoOut futureBooking = bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "FUTURE", 0, 10);

        assertEquals(1, returnedBookings.size());
        assertEquals(futureBooking.getId(), returnedBookings.get(0).getId());
    }

    @Test
    void getAllBookerBookings_WhenStateWAITING_or_REJECTED() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut currentBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);
        BookingDtoOut previousBooking = bookingService.create(savedBooker.getId(), previousBookingDto);
        BookingDtoOut futureBooking = bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllBooker(2L, "WAITING", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(futureBooking.getId(), returnedBookings.get(0).getId());
        assertEquals(currentBooking.getId(), returnedBookings.get(1).getId());
        assertEquals(previousBooking.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookings_shouldThrowDataNotFoundException_WhenUserNotExist() {
        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.getAllOwnerItem(999L, "ALL", 0, 10));

        assertEquals("Пользователь с id=999 не найден", dataNotFoundException.getMessage());
    }

    @Test
    void getAllOwnerItemBookings_shouldThrowWrongStatusException_WhenWrongState() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        userService.createUser(user2Dto);

        WrongStatusException statusException = assertThrows(WrongStatusException.class,
                () -> bookingService.getAllOwnerItem(1L, " ", 0, 10));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", statusException.getMessage());
    }

    @Test
    void getAllOwnerItemBookings_WhenStateALL() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut currentBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);
        BookingDtoOut previousBooking = bookingService.create(savedBooker.getId(), previousBookingDto);
        BookingDtoOut futureBooking = bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(1L, "ALL", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(futureBooking.getId(), returnedBookings.get(0).getId());
        assertEquals(currentBooking.getId(), returnedBookings.get(1).getId());
        assertEquals(previousBooking.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookings_WhenStateCURRENT() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut currentBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);
        bookingService.create(savedBooker.getId(), previousBookingDto);
        bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(1L, "CURRENT", 0, 10);

        assertEquals(1, returnedBookings.size());
        assertEquals(currentBooking.getId(), returnedBookings.get(0).getId());
    }

    @Test
    void getAllOwnerItemBookings_WhenStatePAST() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        bookingService.create(savedBooker.getId(), bookingDtoInput);
        BookingDtoOut previousBooking = bookingService.create(savedBooker.getId(), previousBookingDto);
        bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(1L, "PAST", 0, 10);

        assertEquals(1, returnedBookings.size());
        assertEquals(previousBooking.getId(), returnedBookings.get(0).getId());
    }

    @Test
    void getAllOwnerItemBookings_WhenStateFUTURE() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        bookingService.create(savedBooker.getId(), bookingDtoInput);
        bookingService.create(savedBooker.getId(), previousBookingDto);
        BookingDtoOut futureBooking = bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(1L, "FUTURE", 0, 10);

        assertEquals(1, returnedBookings.size());
        assertEquals(futureBooking.getId(), returnedBookings.get(0).getId());
    }

    @Test
    void getAllOwnerItemBookings_WhenStateWAITING_or_REJECTED() {
        UserDto savedOwner = userService.createUser(userDto);
        itemService.createItem(savedOwner.getId(), itemDto);
        UserDto savedBooker = userService.createUser(user2Dto);
        BookingDtoOut currentBooking = bookingService.create(savedBooker.getId(), bookingDtoInput);
        BookingDtoOut previousBooking = bookingService.create(savedBooker.getId(), previousBookingDto);
        BookingDtoOut futureBooking = bookingService.create(savedBooker.getId(), futureBookingDto);

        List<BookingDtoOut> returnedBookings = bookingService.getAllOwnerItem(1L, "WAITING", 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(futureBooking.getId(), returnedBookings.get(0).getId());
        assertEquals(currentBooking.getId(), returnedBookings.get(1).getId());
        assertEquals(previousBooking.getId(), returnedBookings.get(2).getId());
    }
}