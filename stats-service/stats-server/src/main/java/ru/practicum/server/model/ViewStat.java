package ru.practicum.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ViewStat {

    private String app;

    private String uri;

    private Long hits;
}
