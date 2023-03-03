package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RequestDtoUpdated {

    List<RequestDto> confirmedRequests;

    List<RequestDto> rejectedRequests;
}
