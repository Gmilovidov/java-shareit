package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.AvailableException;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.WrongIdException;
import ru.practicum.shareit.exceptions.WrongStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDtoOut create(Long bookerId, BookingDtoIn bookingDtoIn) {
        User booker = userService.getUserByIdWithoutDto(bookerId);
        Item item = itemService.getItemByIdWithoutDto(bookingDtoIn.getBookingId());
        if (bookerId.equals(item.getOwner().getId())) {
            throw new DataNotFoundException("Нельзя забронировать свою вещь");
        }
        if (!item.getAvailable()) {
            throw new AvailableException("Вещь забронирована");
        }
        return bookingMapper.getBookingDtoOut(bookingRepository.save(
                bookingMapper.getBookingFromDto(bookingDtoIn, item, booker, BookingStatus.WAITING)));
    }

    @Override
    public BookingDtoOut updateStatus(Long ownerId, Long bookingId, Boolean isApproved) {
        userService.getUserByIdWithoutDto(ownerId);
        Booking booking = getBookingByIdWithoutDto(bookingId);
        if (!ownerId.equals(booking.getItem().getId())) {
           throw new WrongIdException("Нельзя изменить статус брони вещи, пользователю, с ids "
                   + bookingId + " и " + ownerId + " соответственно");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new WrongStatusException("Нельзя изменить статус вещи");
        }
        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.getBookingDtoOut(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoOut getBooking(Long userId, Long bookingId) {
        userService.getUserByIdWithoutDto(userId);
        Booking booking = getBookingByIdWithoutDto(bookingId);
        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new DataNotFoundException("Для пользователя  с id = " + userId + "  бронь не найдена");
        }
        return bookingMapper.getBookingDtoOut(booking);
    }

    @Override
    public List<BookingDtoOut> getAllBooker(Long userId, String state) {
        userService.getUserByIdWithoutDto(userId);
        switch (state.toUpperCase()) {
            case "CURRENT":
                return getDtoOutListBookings(bookingRepository
                        .readAllBookerCurrentBookings(userId, LocalDateTime.now()));
            case "PAST":
                return getDtoOutListBookings(bookingRepository
                        .readAllBookerPastBookings(userId, LocalDateTime.now()));
            case "FUTURE":
                return getDtoOutListBookings(bookingRepository
                        .readAllBookerFutureBookings(userId, LocalDateTime.now()));
            case "WAITING":
                return getDtoOutListBookings(bookingRepository
                        .findAllByBooker_IdAndStatusOrderByStartTimeDesc(userId, BookingStatus.WAITING));
            case "REJECTED":
                return getDtoOutListBookings(bookingRepository
                        .findAllByBooker_IdAndStatusOrderByStartTimeDesc(userId, BookingStatus.REJECTED));
            case "ALL":
                return getDtoOutListBookings(bookingRepository
                        .findAllByBooker_IdOrderByStartTimeDesc(userId));
            default:
                throw new WrongStatusException("Неизвестный статус");
        }
    }

    @Override
    public List<BookingDtoOut> getAllOwnerItem(Long ownerId, String state) {
        userService.getUserByIdWithoutDto(ownerId);
        List<Long> ids = itemService.getAlItemsByOwnerId(ownerId).stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        switch (state.toUpperCase()) {
            case "CURRENT":
                return getDtoOutListBookings(bookingRepository
                        .readAllOwnerItemsCurrentBookings(ids, LocalDateTime.now()));
            case "PAST":
                return getDtoOutListBookings(bookingRepository
                        .readAllOwnerItemsPastBookings(ids, LocalDateTime.now()));
            case "FUTURE":
                return getDtoOutListBookings(bookingRepository
                        .readAllOwnerItemsFutureBookings(ids, LocalDateTime.now()));
            case "WAITING":
                return getDtoOutListBookings(bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartTimeDesc(ids, List.of(BookingStatus.WAITING)));
            case "REJECTED":
                return getDtoOutListBookings(bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartTimeDesc(ids, List.of(BookingStatus.REJECTED)));
            case "ALL":
                return getDtoOutListBookings(bookingRepository
                        .findAllByItem_IdInOrderByStartTimeDesc(ids));
            default:
                throw new WrongStatusException("Неизвестный статус");
        }
    }

    @Override
    public Booking getBookingByIdWithoutDto(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Бронь с id = " + bookingId + " не найдена"));
    }

    @Override
    public List<Booking> getAllBookingsListByItemId(Long itemId) {
        return bookingRepository.findAllByItemId(itemId);
    }

    @Override
    public List<Booking> getAllBookingByParams(Long itemId, Long bookerId, BookingStatus status) {
        return bookingRepository.findAllByItem_IdAndBooker_IdAndStatus(itemId, bookerId, status);
    }

    private List<BookingDtoOut> getDtoOutListBookings(List<Booking> bookings) {
        List<BookingDtoOut> bookingDtoOuts = new ArrayList<>();
        for (Booking b : bookings) {
            bookingDtoOuts.add(bookingMapper.getBookingDtoOut(b)) ;
        }
        return bookingDtoOuts;
    }
}
