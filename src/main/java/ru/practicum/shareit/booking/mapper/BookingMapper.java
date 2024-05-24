package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public Booking getBookingFromDto(BookingDtoIn bookingDtoIn, Item item, User booker, BookingStatus status) {
        return Booking.builder()
                .id(bookingDtoIn.getBookingId())
                .startTime(bookingDtoIn.getStart())
                .endTime(bookingDtoIn.getEnd())
                .item(item)
                .booker(booker)
                .status(status)
                .build();
    }

    public BookingDtoOut getBookingDtoOut(Booking booking) {
        return BookingDtoOut.builder()
                .id(booking.getId())
                .start(booking.getStartTime())
                .end(booking.getEndTime())
                .item(itemMapper.getItemDto(booking.getItem(), null, null, null))
                .booker(userMapper.getUserDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }

    public BookingDto getBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public List<BookingDtoOut> getDtoOutList(List<Booking> bookingList) {
        return bookingList.stream()
                .map(this::getBookingDtoOut)
                .collect(Collectors.toList());
    }
}