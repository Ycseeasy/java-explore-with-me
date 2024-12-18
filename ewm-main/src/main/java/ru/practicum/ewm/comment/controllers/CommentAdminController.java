package ru.practicum.ewm.comment.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.service.CommentService;

@Slf4j
@RestController
@RequestMapping(path = "/admin/comments/{commentId}")
@RequiredArgsConstructor
@Validated
public class CommentAdminController {

    private final CommentService commentService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteCommentAdmin(HttpServletRequest request,
                                @Positive @PathVariable Long commentId) {
        commentService.deleteCommentByIdAdmin(commentId);

    }

    @PatchMapping()
    public CommentDto updateCommentAdmin(HttpServletRequest request,
                                         @Positive @PathVariable Long commentId,
                                         @Valid @RequestBody CommentDto commentDto) {
        return commentService.updateCommentAdmin(commentId, commentDto);
    }
}