package ru.practicum.request.dto;

import lombok.Data;
import ru.practicum.request.model.RequestState;

import java.util.List;

@Data
public class RequestDtoUpdateStatus {
    List<Long> requestIds;

    RequestState status;
}
