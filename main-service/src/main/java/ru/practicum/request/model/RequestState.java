package ru.practicum.request.model;

public enum RequestState {
    PENDING, CONFIRMED, REJECTED, CANCELED;

    public static RequestState fromString(String text) {
        for (RequestState state : RequestState.values()) {
            if (state.name().equalsIgnoreCase(text)) {
                return state;
            }
        }
        return null;
    }
}
