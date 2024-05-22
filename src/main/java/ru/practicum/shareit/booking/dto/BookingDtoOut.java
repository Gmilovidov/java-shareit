package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingDtoOut {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ItemDto item;
    private UserDto booker;
    private BookingStatus status;
}
