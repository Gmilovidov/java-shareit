package ru.practicum.shareit.validateGroup;

import ru.practicum.shareit.annotation.StartEndBookingValidation;
import ru.practicum.shareit.booking.dto.BookingDtoIn;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class StartEndBookingValidator implements ConstraintValidator<StartEndBookingValidation, BookingDtoIn> {

    @Override
    public boolean isValid(BookingDtoIn bookingDtoIn, ConstraintValidatorContext constraintValidatorContext) {
        boolean isEndNull = (bookingDtoIn.getEnd() == null);

        boolean isStartNull = (bookingDtoIn.getStart() == null);

        if (isEndNull || isStartNull) {
            return false;
        }

        boolean isEndFutureOrPresent = bookingDtoIn.getEnd().equals(LocalDateTime.now())
                || bookingDtoIn.getEnd().isAfter(LocalDateTime.now());

        boolean isStartFutureOrPresent = bookingDtoIn.getStart().equals(LocalDateTime.now())
                || bookingDtoIn.getStart().isAfter(LocalDateTime.now());

        boolean isEndAfterStart = bookingDtoIn.getEnd().isAfter(bookingDtoIn.getStart());

        boolean isNotEndEqualStart = !bookingDtoIn.getEnd().equals(bookingDtoIn.getStart());

        return isEndFutureOrPresent
                && isStartFutureOrPresent
                && isEndAfterStart
                && isNotEndEqualStart;
    }
}
