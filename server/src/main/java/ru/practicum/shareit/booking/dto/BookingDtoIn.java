package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.annotation.StartEndBookingValidation;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@StartEndBookingValidation
public class BookingDtoIn {
    private Long bookingId;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
}
