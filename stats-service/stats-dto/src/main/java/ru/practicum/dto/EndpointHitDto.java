package ru.practicum.dto;

import lombok.Data;

import java.time.LocalDateTime;
import lombok.Builder;

@Data
@Builder
public class EndpointHitDto {
    private Long id;

    private String app;

    private String uri;

    private String ip;

    private LocalDateTime timestamp;
}