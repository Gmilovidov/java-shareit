package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import lombok.*;
import ru.practicum.shareit.annotation.StartEndBookingValidation;

@Setter
@Getter
@EqualsAndHashCode
@StartEndBookingValidation
public class BookingDtoIn {
	private Long bookingId;
	private LocalDateTime start;
	private LocalDateTime end;
	private Long itemId;
}
