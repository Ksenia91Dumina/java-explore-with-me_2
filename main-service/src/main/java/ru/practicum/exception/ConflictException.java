package ru.practicum.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {

    public ConflictException(final String message) {
        super(message);
    }
}
