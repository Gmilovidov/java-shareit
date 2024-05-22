package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", source = "bookingDtoIn.bookingId")
    Booking getBookingFromDto(BookingDtoIn bookingDtoIn, Item item, User booker, BookingStatus status);

    BookingDtoOut getBookingDtoOut(Booking booking);

    @Mapping(target = "bookerId", source = "booker.id")
    @Mapping(target = "id", source = "booking.id")
    BookingDto getBookingDto(Booking booking, User booker);
}
