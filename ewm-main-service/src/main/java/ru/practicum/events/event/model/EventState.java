package ru.practicum.events.event.model;

import ru.practicum.events.event.dto.stateDto.ActionStateDto;
import ru.practicum.exception.BadRequestException;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static EventState determineTheStatusForEvent(ActionStateDto stateAction) {
        if (stateAction.equals(ActionStateDto.SEND_TO_REVIEW)) {
            return EventState.PENDING;
        } else if (stateAction.equals(ActionStateDto.CANCEL_REVIEW)) {
            return EventState.CANCELED;
        } else if (stateAction.equals(ActionStateDto.PUBLISH_EVENT)) {
            return EventState.PUBLISHED;
        } else if (stateAction.equals(ActionStateDto.REJECT_EVENT)) {
            return EventState.CANCELED;
        } else {
            throw new BadRequestException("Status not according something");
        }
    }
}
