package ru.practicum.events.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.comments.dto.CommentDto;
import ru.practicum.events.comments.dto.InputCommentDto;
import ru.practicum.events.comments.dto.UpdateCommentAdminDto;
import ru.practicum.events.comments.service.CommentsServiceAdmin;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/admin/comments")
@Slf4j
public class CommentsControllerAdmin {
    private final CommentsServiceAdmin commentsServiceAdmin;

    @GetMapping("/event/{eventId}")
    public List<CommentDto> getAllCommentsByEventId(@PathVariable Long eventId,
                                             @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                             @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Get all comment by ID = {}(Admin)", eventId);
        return commentsServiceAdmin.getAllCommentsByEventId(eventId, from, size);
    }


    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long commentId,
                             @Validated @RequestBody UpdateCommentAdminDto updateComment) {
        log.info("Update comment = {}(Admin)", commentId);
        return commentsServiceAdmin.updateComment(commentId, updateComment);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        log.info("Update comment = {}(Admin)", commentId);
        return commentsServiceAdmin.getCommentById(commentId);
    }


    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@Validated @RequestBody InputCommentDto inputCommentDto) {
        log.info("Create comment(Admin)");
        return commentsServiceAdmin.createComment(inputCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(@PathVariable Long commentId) {
        log.info("Delete comment with ID = commentId(Admin)");
        commentsServiceAdmin.deleteComment(commentId);
    }
}
