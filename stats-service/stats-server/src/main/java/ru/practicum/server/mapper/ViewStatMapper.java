package ru.practicum.server.mapper;

import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.model.ViewStat;

import java.util.ArrayList;
import java.util.List;

public class ViewStatMapper {
    public static ViewStatsDto toViewStatsDto(ViewStat viewStat) {
        return ViewStatsDto.builder()
            .app(viewStat.getApp())
            .uri(viewStat.getUri())
            .hits(viewStat.getHits())
            .build();
    }

    public static ViewStat toViewStat(ViewStatsDto viewStatsDto) {
        return ViewStat.builder()
            .app(viewStatsDto.getApp())
            .uri(viewStatsDto.getUri())
            .hits(viewStatsDto.getHits())
            .build();
    }

    public static List<ViewStatsDto> toViewStatsDtoList(List<ViewStat> viewStats) {
        List<ViewStatsDto> viewStatsDtoList = new ArrayList<>();
        for (ViewStat viewStat : viewStats) {
            viewStatsDtoList.add(toViewStatsDto(viewStat));
        }
        return viewStatsDtoList;
    }
}
