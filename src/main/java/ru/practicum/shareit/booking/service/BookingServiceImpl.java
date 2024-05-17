package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDtoOut create(Long holderId, BookingDtoIn bookingDtoIn) {
        return null;
    }

    @Override
    public BookingDtoOut updateStatus(Long ownerId, Long bookingId, Boolean isApproved) {
        return null;
    }

    @Override
    public BookingDtoOut getBooking(Long userId, Long bookingId) {
        return null;
    }

    @Override
    public List<BookingDtoOut> getAllBooker(Long userId, String state) {
        return null;
    }

    @Override
    public List<BookingDtoOut> getAllOwnerItem(Long ownerId, String state) {
        return null;
    }
}
