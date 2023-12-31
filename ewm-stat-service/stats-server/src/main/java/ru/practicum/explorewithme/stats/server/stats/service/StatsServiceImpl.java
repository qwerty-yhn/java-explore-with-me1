package ru.practicum.explorewithme.stats.server.stats.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.stats.dto.StatsDto;
import ru.practicum.explorewithme.stats.server.util.DateFormatter;
import ru.practicum.explorewithme.stats.server.exception.ValidationDateException;
import ru.practicum.explorewithme.stats.server.stats.storage.StatsRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Autowired
    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    public List<StatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        LocalDateTime newStart = DateFormatter.formatDate(start);
        LocalDateTime newEnd = DateFormatter.formatDate(end);
        validDate(newStart, newEnd);
        if (uris == null && !unique) {
            return statsRepository.findByDate(newStart, newEnd);
        }
        if (uris == null && unique) {
            return statsRepository.findByDateAndUniqueIp(newStart, newEnd);
        }
        if (!uris.isEmpty() && !unique) {
            return statsRepository.findByDateAndUris(newStart, newEnd, uris);
        }
        if (!uris.isEmpty() && unique) {
            return statsRepository.findByDateAndUrisWithUniqueIp(newStart, newEnd, uris);
        }
        return new ArrayList<>();
    }

    private void validDate(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || start.isAfter(end)) {
            throw new ValidationDateException("wrong set data dating");
        }
    }
}
