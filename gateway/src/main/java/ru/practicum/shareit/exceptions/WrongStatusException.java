package ru.practicum.shareit.exceptions;

public class WrongStatusException extends RuntimeException {
    public WrongStatusException(String msg) {
        super(msg);
    }
}
