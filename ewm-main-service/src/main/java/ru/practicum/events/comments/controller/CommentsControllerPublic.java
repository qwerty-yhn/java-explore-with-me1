package ru.practicum.events.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.comments.dto.CommentDto;
import ru.practicum.events.comments.service.CommentsServicePublic;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/comments/event/{eventId}")
@Slf4j
public class CommentsControllerPublic {
    private final CommentsServicePublic commentsServicePublic;

    @GetMapping()
    public List<CommentDto> getAllCommentsByEventId(@PathVariable Long eventId,
                                             @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                             @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Get all comment with event ID = {}(Public)", eventId);
        return commentsServicePublic.getAllCommentsByEventId(eventId, from, size);
    }
}
