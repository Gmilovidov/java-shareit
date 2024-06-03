package ru.practicum.shareit.exceptions;

public class WrongIdException extends RuntimeException {
    public WrongIdException(String msg) {
        super(msg);
    }
}
