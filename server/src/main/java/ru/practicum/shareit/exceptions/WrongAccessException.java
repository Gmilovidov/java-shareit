package ru.practicum.shareit.exceptions;

public class WrongAccessException extends RuntimeException {
    public WrongAccessException(String msg) {
        super(msg);
    }
}
