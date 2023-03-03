package ru.practicum.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {

    public static Request toRequest(RequestDto requestDto) {
        return Request.builder()
            .id(requestDto.getId())
            .event(requestDto.getEvent())
            .requester(requestDto.getRequester())
            .createdDate(requestDto.getCreatedDate())
            .status(requestDto.getStatus())
            .build();
    }

    public static RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
            .id(request.getId())
            .event(request.getEvent())
            .requester(request.getRequester())
            .status(request.getStatus())
            .createdDate(request.getCreatedDate())
            .build();
    }

    public static List<RequestDto> toRequestDtoList(List<Request> participationRequests) {
        List<RequestDto> requestDtoList = new ArrayList<>();
        for (Request participationRequest : participationRequests) {
            requestDtoList.add(toRequestDto(participationRequest));
        }
        return requestDtoList;
    }
}
