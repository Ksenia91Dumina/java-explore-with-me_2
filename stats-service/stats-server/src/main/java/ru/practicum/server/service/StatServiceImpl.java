package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.mapper.EndpointHitMapper;
import ru.practicum.server.mapper.ViewStatMapper;
import ru.practicum.server.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatRepository repository;

    @Override
    @Transactional
    public void addHit(EndpointHitDto endpointHitDto) {
        repository.save(EndpointHitMapper.toEndpointHit(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            return ViewStatMapper.toViewStatsDtoList(repository.getUniqueStat(start, end, uris));
        } else {
            return ViewStatMapper.toViewStatsDtoList(repository.getNonUniqueStat(start, end, uris));
        }
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, Boolean unique) {
        if (unique) {
            return ViewStatMapper.toViewStatsDtoList(repository.getUniqueStat(start, end));
        } else {
            return ViewStatMapper.toViewStatsDtoList(repository.getNonUniqueStat(start, end));
        }
    }

}