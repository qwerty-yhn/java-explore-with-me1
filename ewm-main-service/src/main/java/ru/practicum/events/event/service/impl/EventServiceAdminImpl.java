package ru.practicum.events.event.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.events.event.dto.EventFullDto;
import ru.practicum.events.event.dto.UpdateEventAdminRequest;
import ru.practicum.events.event.dto.stateDto.ActionStateDto;
import ru.practicum.events.event.mapper.EventMapper;
import ru.practicum.events.event.mapper.LocationMapper;
import ru.practicum.events.event.model.Event;
import ru.practicum.events.event.model.EventState;
import ru.practicum.events.event.service.EventServiceAdmin;
import ru.practicum.events.event.storage.EventRepository;
import ru.practicum.events.request.model.RequestStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ForbiddenEventException;
import ru.practicum.exception.ResourceNotFoundException;
import ru.practicum.util.FindObjectInRepository;
import ru.practicum.util.util.DateFormatter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.util.util.DateFormatter.DATE_FORMAT;

@Service
@Slf4j
public class EventServiceAdminImpl implements EventServiceAdmin {
    private final EventRepository eventRepository;
    private final FindObjectInRepository findObjectInRepository;
    private final ProcessingEvents processingEvents;

    @Autowired
    public EventServiceAdminImpl(EventRepository eventRepository,
                                 FindObjectInRepository findObjectInRepository,
                                 ProcessingEvents processingEvents) {
        this.eventRepository = eventRepository;
        this.findObjectInRepository = findObjectInRepository;
        this.processingEvents = processingEvents;
    }

    @Override
    public List<EventFullDto> getAllEventsForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                                   String rangeStart, String rangeEnd, int from, int size, HttpServletRequest request) {
        List<Event> events;
        LocalDateTime newRangeStart = null;
        if (rangeStart != null) {
            newRangeStart = DateFormatter.formatDate(rangeStart);
        }
        LocalDateTime newRangeEnd = null;
        if (rangeEnd != null) {
            newRangeEnd = DateFormatter.formatDate(rangeEnd);
        }
        Pageable page = PageRequest.of(from / size, size);
        if (states != null) {
            List<EventState> eventStates = states.stream().map((s) -> EventState.valueOf(s)).collect(Collectors.toList());
            events = eventRepository.findAllByAdminAndState(users, eventStates, categories, newRangeStart, newRangeEnd, page);
            List<Event> eventsAddViews = processingEvents.addViewsInEventsList(events, request);
            List<Event> newEvents = processingEvents.confirmedRequests(eventsAddViews);
            return newEvents.stream().map(EventMapper::eventToEventFullDto).collect(Collectors.toList());
        } else {
            events = eventRepository.findAllByAdmin(users, categories, newRangeStart, newRangeEnd, page);
            List<Event> eventsAddViews = processingEvents.addViewsInEventsList(events, request);
            List<Event> newEvents = processingEvents.confirmedRequests(eventsAddViews);
            return newEvents.stream().map(EventMapper::eventToEventFullDto).collect(Collectors.toList());
        }
    }

    @Transactional
    @Override
    public EventFullDto updateEventById(Long eventId, UpdateEventAdminRequest updateEvent, HttpServletRequest request) {
        log.info("Получен запрос на обновление события с id= {} (администратором)", eventId);
        Event event = findObjectInRepository.getEventById(eventId);
        eventAvailability(event);
        if (updateEvent.getEventDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            LocalDateTime eventDateLocalTime = LocalDateTime.parse(updateEvent.getEventDate(), formatter);

            if (eventDateLocalTime.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("");
            }
        }


        if (updateEvent.getDescription() != null) {
            if (!(updateEvent.getDescription().length() == 7000 && updateEvent.getAnnotation().length() == 2000)) {

                if (updateEvent.getDescription().length() < 20 || updateEvent.getDescription().length() > 7000 || updateEvent.getAnnotation().length() < 20 || updateEvent.getAnnotation().length() > 200) {
                    throw new BadRequestException("");
                }
            }
        }

        if (updateEvent.getEventDate() != null) {
            checkEventDate(DateFormatter.formatDate(updateEvent.getEventDate()));
        }
        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = findObjectInRepository.getCategoryById(updateEvent.getCategory());
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getEventDate() != null) {
            event.setEventDate(DateFormatter.formatDate(updateEvent.getEventDate()));
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(LocationMapper.locationDtoToLocation(updateEvent.getLocation()));
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getStateAction() != null) {
            if (!event.getState().equals(EventState.PUBLISHED) && updateEvent.getStateAction().equals(ActionStateDto.PUBLISH_EVENT)) {
                event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            } else if (event.getPublishedOn() == null) {
                event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)); //????
            }
            event.setState(determiningTheStatusForEvent(updateEvent.getStateAction()));
        }
        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            event.setTitle(updateEvent.getTitle());
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            addEventConfirmedRequestsAndViews(event, request);
        } else {
            event.setViews(0L);
            event.setConfirmedRequests(0L);
        }
        try {
            return EventMapper.eventToEventFullDto(eventRepository.save(event));
        } catch (DataAccessException e) {
            throw new ResourceNotFoundException("База данных недоступна");
        } catch (Exception e) {
            throw new BadRequestException("Запрос на добавлении события " + event + " составлен не корректно ");
        }
    }

    private LocalDateTime checkEventDate(LocalDateTime eventDate) {
        LocalDateTime timeNow = LocalDateTime.now().plusHours(1L);
        if (eventDate != null && eventDate.isBefore(timeNow)) {
            throw new ForbiddenEventException("Событие должно содержать дату, которая еще не наступила. " +
                    "Value: " + eventDate);
        }
        return timeNow;
    }

    private EventState determiningTheStatusForEvent(ActionStateDto stateAction) {
        if (stateAction.equals(ActionStateDto.SEND_TO_REVIEW)) {
            return EventState.PENDING;
        } else if (stateAction.equals(ActionStateDto.CANCEL_REVIEW)) {
            return EventState.CANCELED;
        } else if (stateAction.equals(ActionStateDto.PUBLISH_EVENT)) {
            return EventState.PUBLISHED;
        } else if (stateAction.equals(ActionStateDto.REJECT_EVENT)) {
            return EventState.CANCELED;
        } else {
            throw new BadRequestException("Статус не соответствует модификатору доступа");
        }
    }

    private void eventAvailability(Event event) {
        if (event.getState().equals(EventState.PUBLISHED) || event.getState().equals(EventState.CANCELED)) {
            throw new ForbiddenEventException("Статус события не позволяет редоктировать событие, статус: " + event.getState());
        }
    }

    private void addEventConfirmedRequestsAndViews(Event event, HttpServletRequest request) {
        long count = processingEvents.confirmedRequestsForOneEvent(event, RequestStatus.CONFIRMED);
        event.setConfirmedRequests(count);
        long views = processingEvents.searchViews(event, request);
        event.setViews(views);
    }
}
