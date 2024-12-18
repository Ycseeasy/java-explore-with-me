package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addCommentPrivate(Long evenId, CommentDto commentDto);

    void deleteCommentByIdPrivate(Long commentId);

    CommentDto updateCommentPrivate(Long commentId, CommentDto commentDto);

    List<CommentDto> getCommentsPrivate(Long eventId, Integer from, Integer size);

    CommentDto getCommentByIdPrivate(Long commentId);

    void deleteCommentByIdAdmin(Long commentId);

    CommentDto updateCommentAdmin(Long commentId, CommentDto commentDto);
}