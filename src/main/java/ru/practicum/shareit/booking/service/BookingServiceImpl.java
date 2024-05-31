package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public BookingDtoOut create(Long bookerId, BookingDtoIn bookingDtoIn) {
        User booker =  userRepository.findById(bookerId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + bookerId + " не найден"));
        Item item = itemRepository.findById(bookingDtoIn.getItemId())
                .orElseThrow(() -> new DataNotFoundException("Вещи с id=" + bookingDtoIn.getItemId() + " нет."));
        if (bookerId.equals(item.getOwner().getId())) {
            throw new DataNotFoundException("Нельзя забронировать свою вещь");
        }
        if (!item.getAvailable()) {
            throw new AvailableException("Вещь забронирована");
        }
        Booking booking = bookingMapper.getBookingFromDto(bookingDtoIn, item, booker, BookingStatus.WAITING);
        return bookingMapper.getBookingDtoOut(bookingRepository.save(booking),
                itemMapper.getItemDto(booking.getItem(), null, null, null),
                userMapper.getUserDto(booking.getBooker()));
    }

    @Override
    public BookingDtoOut updateStatus(Long ownerId, Long bookingId, Boolean isApproved) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + ownerId + " не найден"));
        Booking booking = getBookingByIdWithoutDto(bookingId);
        itemRepository.findById(booking.getItem().getId()).orElseThrow(() ->
                new DataNotFoundException("Вещь не найдена"));
        if (!ownerId.equals(booking.getItem().getOwner().getId())) {
           throw new WrongAccessException("Нельзя изменить статус брони вещи, пользователю, с ids "
                   + bookingId + " и " + ownerId + " соответственно");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new WrongStatusException("Нельзя изменить статус вещи");
        }
        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.getBookingDtoOut(bookingRepository.save(booking),
                itemMapper.getItemDto(booking.getItem(), null, null, null),
                userMapper.getUserDto(booking.getBooker()));
    }

    @Override
    public BookingDtoOut getBooking(Long userId, Long bookingId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));
        Booking booking = getBookingByIdWithoutDto(bookingId);
        itemRepository.findById(booking.getItem().getId()).orElseThrow(() ->
                new DataNotFoundException("Вещь не найдена"));
        boolean checkBookingUser = userId.equals(booking.getBooker().getId())
                || userId.equals(booking.getItem().getOwner().getId());
        if (!checkBookingUser) {
            throw new DataNotFoundException("Для пользователя  с id = " + userId + "  бронь не найдена");
        }
        return bookingMapper.getBookingDtoOut(booking,
                itemMapper.getItemDto(booking.getItem(), null, null, null),
                userMapper.getUserDto(booking.getBooker()));
    }

    @Override
    public List<BookingDtoOut> getAllBooker(Long userId, String state, Integer start, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));

        Pageable pageable = PageRequest.of(start / size, size);

        switch (state.toUpperCase()) {
            case "CURRENT":
                return getDtoOutList(bookingRepository
                        .readAllBookerCurrentBookings(userId, LocalDateTime.now(), pageable));
            case "PAST":
                return getDtoOutList(bookingRepository
                        .readAllBookerPastBookings(userId, LocalDateTime.now(), pageable));
            case "FUTURE":
                return getDtoOutList(bookingRepository
                        .readAllBookerFutureBookings(userId, LocalDateTime.now(), pageable));
            case "WAITING":
                return getDtoOutList(bookingRepository
                        .findAllByBooker_IdAndStatusOrderByStartTimeDesc(userId, BookingStatus.WAITING, pageable));
            case "REJECTED":
                return getDtoOutList(bookingRepository
                        .findAllByBooker_IdAndStatusOrderByStartTimeDesc(userId, BookingStatus.REJECTED, pageable));
            case "ALL":
                return getDtoOutList(bookingRepository
                        .findAllByBooker_IdOrderByStartTimeDesc(userId, pageable));
            default:
                throw new WrongStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingDtoOut> getAllOwnerItem(Long ownerId, String state, Integer start, Integer size) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + ownerId + " не найден"));
        Pageable pageable = PageRequest.of(start / size, size);
        List<Long> ids = itemRepository.findAllByOwnerId(ownerId).stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        switch (state.toUpperCase()) {
            case "CURRENT":
                return getDtoOutList(bookingRepository
                        .readAllOwnerItemsCurrentBookings(ids, LocalDateTime.now(), pageable));
            case "PAST":
                return getDtoOutList(bookingRepository
                        .readAllOwnerItemsPastBookings(ids, LocalDateTime.now(), pageable));
            case "FUTURE":
                return getDtoOutList(bookingRepository
                        .readAllOwnerItemsFutureBookings(ids, LocalDateTime.now(), pageable));
            case "WAITING":
                return getDtoOutList(bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartTimeDesc(ids,
                                List.of(BookingStatus.WAITING), pageable));
            case "REJECTED":
                return getDtoOutList(bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartTimeDesc(ids,
                                List.of(BookingStatus.REJECTED), pageable));
            case "ALL":
                return getDtoOutList(bookingRepository
                        .findAllByItem_IdInOrderByStartTimeDesc(ids, pageable));
            default:
                throw new WrongStatusException("Unknown state: UNSUPPORTED_STATUS");
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

    private List<BookingDtoOut> getDtoOutList(List<Booking> bookingList) {
        List<BookingDtoOut> dtoOut = new ArrayList<>();
        for (Booking b : bookingList) {
            dtoOut.add(bookingMapper.getBookingDtoOut(b,
                    itemMapper.getItemDto(b.getItem(), null, null, null),
                    userMapper.getUserDto(b.getBooker())));
        }
        return dtoOut;
    }
}
