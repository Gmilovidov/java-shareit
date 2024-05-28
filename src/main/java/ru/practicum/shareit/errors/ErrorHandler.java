package ru.practicum.shareit.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exceptions.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({WrongIdException.class,
                        WrongAccessException.class,
                       DataNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final RuntimeException e) {
        log.error(e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

//    @ExceptionHandler(WrongStatusException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public Map<String, String> handleWrongStatusException(final WrongStatusException e) {
//        return Map.of("error", e.getMessage());
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String name = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            errors.put(name, msg);
        });
        log.info(errors.toString());
        return errors;
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MissingRequestHeaderException.class,
                       AvailableException.class,
                       WrongStatusException.class})
    public ErrorResponse handleWrongRequestException(final RuntimeException e) {
        log.error(e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherException(final Throwable e) {
        log.error(e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

//    @ExceptionHandler(AvailableException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public Map<String, String> handleValidationException(final AvailableException e) {
//        return Map.of("error", e.getMessage());
//    }

}
