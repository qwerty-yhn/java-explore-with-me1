package ru.practicum.events.request.dto;

import lombok.*;
import ru.practicum.events.request.dto.ParticipationRequestDto;

import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests; //подтвержденные запросы
    private List<ParticipationRequestDto> rejectedRequests; // отклоненные запросы
}
