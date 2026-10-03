package com.apr.community.domain.comment.controller;

import com.apr.community.domain.comment.dto.CommentRequestDTO;
import com.apr.community.domain.comment.dto.CommentResponseDTO;
import com.apr.community.domain.comment.service.CommentCommandService;
import com.apr.community.domain.comment.service.CommentQueryService;
import com.apr.community.global.pagination.PageResponse;
import com.apr.community.global.requester.RequesterId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentQueryService commentQueryService;
    private final CommentCommandService commentCommandService;

    @GetMapping("/posts/{postId}/comments")
    public PageResponse<CommentResponseDTO.CommentDTO> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        return commentQueryService.getComments(postId, cursor, size);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDTO.CommentDTO> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDTO.CommentDTO request,
            @RequesterId String requester
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentCommandService.createComment(postId, request, requester));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequesterId String requester
    ) {
        commentCommandService.deleteComment(commentId, requester);
        return ResponseEntity.noContent().build();
    }
}
