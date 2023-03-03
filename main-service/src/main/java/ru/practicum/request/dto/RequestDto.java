package ru.practicum.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.request.model.RequestState;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestDto {

    private Long id;

    private LocalDateTime createdDate;

    private Long event;

    private Long requester;

    private RequestState status;

}
