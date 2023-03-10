package ru.practicum.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.server.model.EndpointHit;
import ru.practicum.server.model.ViewStat;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.server.model.ViewStat(eh.app as app, eh.uri as uri, COUNT(eh.ip) as hits) " +
        "FROM EndpointHit AS eh " +
        "WHERE eh.timestamp BETWEEN ?1 AND ?2 AND uri in ?3 " +
        "GROUP BY eh.app, eh.uri " +
        "ORDER BY uri DESC")
    List<ViewStat> getNonUniqueStat(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.server.model.ViewStat(eh.app as app, eh.uri as uri, COUNT(DISTINCT eh.ip) as hits) " +
        "FROM EndpointHit AS eh " +
        "WHERE eh.timestamp BETWEEN ?1 AND ?2 AND uri in ?3 " +
        "GROUP BY eh.app, eh.uri " +
        "ORDER BY uri DESC")
    List<ViewStat> getUniqueStat(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.server.model.ViewStat(eh.app as app, eh.uri as uri, COUNT(eh.ip) as hits) " +
        "FROM EndpointHit AS eh " +
        "WHERE eh.timestamp BETWEEN ?1 AND ?2 " +
        "GROUP BY eh.app, eh.uri " +
        "ORDER BY uri DESC")
    List<ViewStat> getNonUniqueStat(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.server.model.ViewStat(eh.app as app, eh.uri as uri, COUNT (DISTINCT eh.ip) as hits) " +
        "FROM EndpointHit AS eh " +
        "WHERE eh.timestamp BETWEEN ?1 AND ?2 " +
        "GROUP BY eh.app, eh.uri " +
        "ORDER BY uri DESC")
    List<ViewStat> getUniqueStat(LocalDateTime start, LocalDateTime end);
}