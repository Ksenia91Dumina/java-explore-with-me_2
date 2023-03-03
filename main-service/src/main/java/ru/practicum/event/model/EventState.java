package ru.practicum.event.model;

import ru.practicum.exception.ConflictException;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static EventState fromString(String text) {
        EventState state;
        try {
            if (text == null) {
                state = EventState.PENDING;
            } else {
                state = EventState.valueOf(text);
            }
        } catch (Exception e) {
            throw new ConflictException("Статуса события не существует");
        }
        return state;
    }
}

