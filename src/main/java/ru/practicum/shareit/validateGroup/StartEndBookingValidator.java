package ru.practicum.shareit.validateGroup;

import ru.practicum.shareit.annotation.StartEndBookingValidation;
import ru.practicum.shareit.booking.dto.BookingDtoIn;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class StartEndBookingValidator implements ConstraintValidator<StartEndBookingValidation, BookingDtoIn> {

    public void startEndValidator(BookingDtoIn dtoIn) {
        if (dtoIn.getStart() == null || dtoIn.getEnd() == null) {
            throw new IllegalStateException("Время начала и конца брони не должно быть null");
        }
        boolean check = !dtoIn.getStart().isAfter(dtoIn.getEnd())
                && !dtoIn.getEnd().isBefore(dtoIn.getStart())
                && dtoIn.getStart() != dtoIn.getEnd()
                && !dtoIn.getStart().equals(dtoIn.getEnd())
                && !dtoIn.getStart().isBefore(LocalDateTime.now());
        if (!check) {
            throw new IllegalStateException("Время брони не прошло валидацию, " +
                    "начало и конец не должны быть равны и противоречить друг другу");
        }
    }

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
