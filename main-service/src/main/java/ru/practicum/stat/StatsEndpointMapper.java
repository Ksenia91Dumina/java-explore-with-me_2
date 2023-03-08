package ru.practicum.stat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.EndpointHitDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatsEndpointMapper {
    public static EndpointHitDto toEndpointHit(HttpServletRequest request) {
        EndpointHitDto endpointHit = new EndpointHitDto();
        endpointHit.setApp("ewm-service");
        endpointHit.setUri(request.getRequestURI());
        endpointHit.setIp(request.getRemoteAddr());
        endpointHit.setTimestamp(LocalDateTime.now());
        return endpointHit;
    }
}
