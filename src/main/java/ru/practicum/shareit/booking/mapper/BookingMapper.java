package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "id", source = "bookingDtoIn.bookingId")
    @Mapping(target = "startTime", source = "bookingDtoIn.start")
    @Mapping(target = "endTime", source = "bookingDtoIn.end")
    Booking getBookingFromDto(BookingDtoIn bookingDtoIn, Item item, User booker, BookingStatus status);

    @Mapping(target = "id", source = "booking.id")
    @Mapping(target = "start", source = "booking.startTime")
    @Mapping(target = "end", source = "booking.endTime")
    BookingDtoOut getBookingDtoOut(Booking booking, ItemDto itemDto, UserDto booker);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingDto getBookingDto(Booking booking);
}
