package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.event.model.StateAction;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDtoUpdateByAdmin {
    @Length(min = 20, max = 2000)
    String annotation;
    Long category;
    @Length(min = 20, max = 7000)
    String description;
    String eventDate;
    LocationDto location;
    Boolean paid;
    Long participantLimit;
    Boolean requestModeration;
    StateAction stateAction;
    @Length(min = 3, max = 120)
    String title;

}
