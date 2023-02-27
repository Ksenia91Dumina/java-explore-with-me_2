package ru.practicum.server.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ViewStat {

    private String app;

    private String uri;

    private Long hits;
}
