package ru.practicum.events.event.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.event.dto.EventFullDto;
import ru.practicum.events.event.dto.EventShortDto;
import ru.practicum.events.event.mapper.EventMapper;
import ru.practicum.events.event.model.Event;
import ru.practicum.events.event.model.EventState;
import ru.practicum.events.event.service.EventServicePublic;
import ru.practicum.events.event.storage.EventRepository;
import ru.practicum.events.request.model.RequestStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ResourceNotFoundException;
import ru.practicum.explorewithme.stats.client.StatsClient;
import ru.practicum.explorewithme.stats.dto.HitDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.util.util.DateFormatter.DATE_FORMAT;
@Service
@Slf4j
@Transactional(readOnly = true)
public class EventServicePublicImpl implements EventServicePublic {
    private final EventRepository eventRepository;
    private final ProcessingEvents processingEvents;
    private final StatsClient client;
    @Value("${app.name}")
    private String appName;

    @Autowired
    public EventServicePublicImpl(EventRepository eventRepository,
                                  StatsClient client,
                                  ProcessingEvents processingEvents) {
        this.eventRepository = eventRepository;
        this.client = client;
        this.processingEvents = processingEvents;
    }

    @Override
    public List<EventShortDto> getAllPublicEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                                  String rangeEnd, boolean onlyAvailable, String sort, Integer from, Integer size, HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            LocalDateTime rangeStartLocalTime = LocalDateTime.parse(rangeStart, formatter);
            LocalDateTime rangeEndLocalTime = LocalDateTime.parse(rangeEnd, formatter);

            if ((rangeEndLocalTime != null) && (rangeEndLocalTime.isBefore(rangeStartLocalTime))) {
                throw new BadRequestException("Start is before end.");
            }
        }

        log.info("Получен запрос на получение всех событий (публичный)");
        HitDto hitDto = HitDto.builder()
                .app(appName)
                .uri("/events")
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .build();
        client.hitRequest(hitDto);
        List<Event> events = eventRepository.findAllByPublic(text, categories, paid, rangeStart, rangeEnd, sort, from, size);
        if (events.isEmpty()) {
            return new ArrayList<>(Collections.nCopies(10, null));
        }
        List<Event> eventsAddViews = processingEvents.addViewsInEventsList(events, request);
        List<Event> newEvents = processingEvents.confirmedRequests(eventsAddViews);
        if (!onlyAvailable) {
            return newEvents.stream().filter(e -> e.getParticipantLimit() >= e.getConfirmedRequests())
                    .map(EventMapper::eventToeventShortDto).collect(Collectors.toList());
        }
        return newEvents.stream().map(EventMapper::eventToeventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getPublicEventById(Long id, HttpServletRequest request) {
        log.info("Получен запрос на получение события по id= {} (публичный)", id);
        HitDto hitDto = createHitDtoToStats(request);
        client.hitRequest(hitDto);
        Event event = eventRepository.findEventByIdAndStateIs(id, EventState.PUBLISHED).orElseThrow(()
                -> new ResourceNotFoundException("Событие c id = " + id + " не найдено"));
        addEventConfirmedRequestsAndViews(event, request);
        return EventMapper.eventToEventFullDto(event);
    }

    private HitDto createHitDtoToStats(HttpServletRequest request) {
        HitDto hitDto = HitDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .build();
        return hitDto;
    }

    private void addEventConfirmedRequestsAndViews(Event event, HttpServletRequest request) {
        long count = processingEvents.confirmedRequestsForOneEvent(event, RequestStatus.CONFIRMED);
        event.setConfirmedRequests(count);
        long views = processingEvents.searchViews(event, request);
        event.setViews(views);
    }
}
