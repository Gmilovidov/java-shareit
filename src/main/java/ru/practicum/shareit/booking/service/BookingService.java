package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingDtoOut create(Long bookerId, BookingDtoIn bookingDtoIn);

    BookingDtoOut updateStatus(Long ownerId, Long bookingId, Boolean isApproved);

    BookingDtoOut getBooking(Long userId, Long bookingId);

    List<BookingDtoOut> getAllBooker(Long userId, String state);

    List<BookingDtoOut> getAllOwnerItem(Long ownerId, String state);

    Booking getBookingByIdWithoutDto(Long bookingId);

    List<Booking> getAllBookingsListByItemId(Long itemId);

    List<Booking> getAllBookingByParams(Long itemId, Long bookerId, BookingStatus status);
}
