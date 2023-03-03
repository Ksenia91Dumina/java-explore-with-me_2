package ru.practicum.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.event.dto.EventDtoNew;
import ru.practicum.event.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationMapper {

    public static Location toLocation(EventDtoNew.Location eventLocation) {
        Location location = new Location();
        location.setLon(eventLocation.getLon());
        location.setLat(eventLocation.getLat());
        return location;
    }
}
