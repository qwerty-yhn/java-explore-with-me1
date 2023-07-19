package ru.practicum.events.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.request.dto.ParticipationRequestDto;
import ru.practicum.events.request.service.RequestServicePrivate;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
public class RequestControllerPrivate {
    private final RequestServicePrivate requestServicePrivate;

    @GetMapping
    List<ParticipationRequestDto> getAllRequestsUserById(@PathVariable Long userId) {
        return requestServicePrivate.getAllRequestsUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto addRequestEventById(@PathVariable Long userId,
                                                @NotNull @RequestParam Long eventId,
                                                HttpServletRequest request) {
        return requestServicePrivate.addRequestEventById(userId, eventId, request);
    }

    @PatchMapping("/{requestId}/cancel")
    ParticipationRequestDto updateRequestById(@PathVariable Long userId,
                                              @PathVariable Long requestId,
                                              HttpServletRequest request) {
        return requestServicePrivate.updateRequestStatus(userId, requestId, request);
    }
}
