package ru.practicum.shareit.annotation;

import ru.practicum.shareit.validateGroup.StartEndBookingValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = StartEndBookingValidator.class)
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface StartEndBookingValidation {
    String message() default "Время брони не прошло валидацию, " +
            "начало и конец не должны быть равны и противоречить друг другу";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
