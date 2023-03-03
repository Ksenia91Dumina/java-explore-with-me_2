package ru.practicum.server.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.model.ViewStat;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ViewStatMapper {
    public static ViewStatsDto toViewStatsDto(ViewStat viewStat) {
        ViewStatsDto viewStatsDto = new ViewStatsDto(
            viewStat.getApp(),
            viewStat.getUri(),
            viewStat.getHits());
        return viewStatsDto;
    }

    public static ViewStat toViewStat(ViewStatsDto viewStatsDto) {
        ViewStat viewStat = new ViewStat(
            viewStatsDto.getApp(),
            viewStatsDto.getUri(),
            viewStatsDto.getHits());
        return viewStat;
    }

    public static List<ViewStatsDto> toViewStatsDtoList(List<ViewStat> viewStats) {
        List<ViewStatsDto> viewStatsDtoList = new ArrayList<>();
        for (ViewStat viewStat : viewStats) {
            viewStatsDtoList.add(toViewStatsDto(viewStat));
        }
        return viewStatsDtoList;
    }
}
