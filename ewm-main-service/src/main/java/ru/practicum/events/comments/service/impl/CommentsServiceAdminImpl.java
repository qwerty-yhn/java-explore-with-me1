package ru.practicum.events.comments.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.comments.dto.CommentDto;
import ru.practicum.events.comments.dto.CommentStateDto;
import ru.practicum.events.comments.dto.InputCommentDto;
import ru.practicum.events.comments.dto.UpdateCommentAdminDto;
import ru.practicum.events.comments.mapper.CommentsMapper;
import ru.practicum.events.comments.model.Comment;
import ru.practicum.events.comments.service.CommentsServiceAdmin;
import ru.practicum.events.comments.storage.CommentsRepository;
import ru.practicum.events.event.model.Event;
import ru.practicum.events.event.storage.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ResourceNotFoundException;
import ru.practicum.users.model.User;
import ru.practicum.users.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CommentsServiceAdminImpl implements CommentsServiceAdmin {
    private final EventRepository eventRepository;
    private final CommentsRepository commentsRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentsServiceAdminImpl(EventRepository eventRepository,
                                    CommentsRepository commentsRepository,
                                    UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.commentsRepository = commentsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public CommentDto createComment(InputCommentDto inputCommentDto) {
        Event event = getEventById(inputCommentDto.getEventId());
        User admin = getUserById(inputCommentDto.getUserId());
        Comment comment = CommentsMapper.createComment(inputCommentDto, admin, event);
        return CommentsMapper.toDto(commentsRepository.save(comment));
    }

    @Transactional
    @Override
    public CommentDto updateComment(Long commentId, UpdateCommentAdminDto updateComment) {
        if (!userRepository.existsById(updateComment.getUserId())) {
            throw new ResourceNotFoundException("user c id = " + updateComment.getUserId() + " not found");
        }
        Event event = getEventById(updateComment.getEventId());
        Comment comment = getCommentByIdIn(commentId);
        CommentUtil.checkCommentOnEvent(comment, event);

        if (updateComment.getText() != null && !updateComment.getText().isBlank()) {
            comment.setText(updateComment.getText());
        }
        if (updateComment.getCommentStateDto() != null) {
            checkStatusAdmin(updateComment, comment);
        }
        return CommentsMapper.toDto(commentsRepository.save(comment));
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        return CommentsMapper.toDto(getCommentByIdIn(commentId));
    }

    @Transactional
    @Override
    public void deleteComment(Long commentId) {
        Comment comment = getCommentByIdIn(commentId);
        commentsRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getAllCommentsByEventId(Long eventId, Integer from, Integer size) {
        Event event = getEventById(eventId);
        Pageable pageable = PageRequest.of(from, size);
        List<Comment> comments = commentsRepository.findByEvent(event, pageable);
        return comments.stream().map(CommentsMapper::toDto).collect(Collectors.toList());
    }

    private void checkStatusAdmin(UpdateCommentAdminDto updateComment, Comment comment) {
        if (updateComment.getCommentStateDto().equals(CommentStateDto.PUBLISHED) || updateComment.getCommentStateDto().equals(CommentStateDto.CANCELED) || updateComment.getCommentStateDto().equals(CommentStateDto.UPDATE)) {
            comment.setState(CommentsMapper.toState(updateComment.getCommentStateDto()));
        } else {
            throw new BadRequestException("Status not exist");
        }
    }

    private Event getEventById(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("event c id = " + id + " not found"));

        return event;
    }

    private User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("user c id = " + id + " not found"));
    }

    private Comment getCommentByIdIn(Long id) {
        return commentsRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("comment c id = " + id + " not found"));
    }
}
