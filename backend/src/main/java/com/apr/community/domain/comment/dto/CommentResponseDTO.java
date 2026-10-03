package com.apr.community.domain.comment.dto;

import lombok.Builder;

import java.time.Instant;

public class CommentResponseDTO {

    // 댓글 목록 items 항목 = 작성 응답
    @Builder
    public record CommentDTO(
            Long id,
            Long postId,
            String content,
            String author,
            Instant createdAt
    ) {}
}
